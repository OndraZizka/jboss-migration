package cz.muni.fi.jboss.migration.migrators.logging;

import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import javafx.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:42 AM
 */
public class LoggingMigrator implements IMigrator {

    private GlobalConfiguration globalConfig;

    private List<Pair<String,String>> config;

    public LoggingMigrator(GlobalConfiguration globalConfig, List<Pair<String,String>> config){
        this.globalConfig = globalConfig;
        this.config =  config;
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException, FileNotFoundException{
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(LoggingAS5.class).createUnmarshaller();

            File file = new File(globalConfig.getDirAS5() + File.separator + "conf" + File.separator + "jboss-log4j.xml");

            LoggingAS5 loggingAS5;

            if(file.canRead()){
               loggingAS5 = (LoggingAS5)unmarshaller.unmarshal(file);
            }else{
                throw new FileNotFoundException("Cannot find/open file: " + file.getAbsolutePath());
            }

            MigrationData mData = new MigrationData();
            mData.getConfigFragment().addAll(loggingAS5.getCategories());
            mData.getConfigFragment().addAll(loggingAS5.getAppenders());
            mData.getConfigFragment().add(loggingAS5.getRootLoggerAS5());

            ctx.getMigrationData().put(LoggingMigrator.class, mData);



        } catch (JAXBException e) {
            throw new LoadMigrationException(e);
        }
    }

    @Override
    public void apply(MigrationContext ctx) {

    }

    @Override
    public List<Node> generateDomElements(MigrationContext ctx) {
        return null;
    }

    @Override
    public List<String> generateCliScripts(MigrationContext ctx) {
        return null;
    }

//    public void migrate(MigrationContext ctx) throws MigrationException{
//        MigratedData migratedData = new MigratedData();
//
//        for (IConfigFragment fragment : ctx.getMigrationData().get(LoggingMigrator.class).getConfigFragment()) {
//            if(fragment instanceof Appender){
//                Appender appender = (Appender) fragment;
//                String type = appender.getAppenderClass();
//
//                switch (StringUtils.substringAfterLast(type, ".")) {
//                    case "DailyRollingFileAppender": {
//                        migratedData.getMigratedData().add(createPerRotFileHandler(appender,ctx));
//                    }
//                    break;
//                    case "RollingFileAppender": {
//                        migratedData.getMigratedData().add(createSizeRotFileHandler(appender,ctx));
//                    }
//                    break;
//                    case "ConsoleAppender": {
//                          migratedData.getMigratedData().add(createConsoleHandler(appender));
//                    }
//                    break;
//                    case "AsyncAppender":{
//                          migratedData.getMigratedData().add(createAsyncHandler(appender));
//                    }
//                    break;
//                    // TODO: There is not such thing as FileAppender in AS5. Only sizeRotating or dailyRotating
//                    // TODO: So i think that FileAppender in AS7 is then useless?
//                    // THINK !!
//
//                    //case "FileAppender" :
//
//                    // Basic implementation of Custom Handler
//                    //TODO: Problem with module
//                    default: {
//                       migratedData.getMigratedData().add(createCustomHandler(appender));
//                    }
//                    break;
//                }
//                continue;
//            }
//
//            if(fragment instanceof Category){
//                Category category = (Category) fragment;
//                Logger logger = new Logger();
//                logger.setLoggerCategory(category.getCategoryName());
//                logger.setLoggerLevelName(category.getCategoryValue());
//                logger.setHandlers(category.getAppenderRef());
//
//                migratedData.getMigratedData().add(logger);
//                continue;
//            }
//
//            if(fragment instanceof RootLoggerAS5){
//                RootLoggerAS5 root =  (RootLoggerAS5) fragment;
//                RootLoggerAS7 rootLoggerAS7 = new RootLoggerAS7();
//                /*
//                TODO: Problem with level, because there is relative path in AS:<priority value="${jboss.server.log.threshold}"/>
//                for now only default INFO
//                */
//                rootLoggerAS7.setRootLoggerLevel("INFO");
//                rootLoggerAS7.setRootLoggerHandlers(root.getRootAppenderRefs());
//                migratedData.getMigratedData().add(rootLoggerAS7);
//                continue;
//            }
//
//            throw new MigrationException("Error: Object is not part of Logging migration!");
//        }
//
//        ctx.getMigratedData().put(LoggingMigrator.class, migratedData);
//    }

    private PerRotFileHandler createPerRotFileHandler(Appender appender, MigrationContext ctx){
        PerRotFileHandler handler = new PerRotFileHandler();
        handler.setName(appender.getAppenderName());

        for (Parameter parameter : appender.getParameters()) {
            if (parameter.getParamName().equalsIgnoreCase("Append")) {
                handler.setAppend(parameter.getParamValue());
                continue;
            }

            if (parameter.getParamName().equals("File")) {
                String value = parameter.getParamValue();


                handler.setFileRelativeTo("jboss.server.log.dir");
                handler.setPath(StringUtils.substringAfterLast(value, "/"));

                CopyMemory copyMemory = new CopyMemory();
                copyMemory.setName(StringUtils.substringAfterLast(value, "/"));
                copyMemory.setType("log");
                ctx.getCopyMemories().add(copyMemory);
            }

            if (parameter.getParamName().equalsIgnoreCase("DatePattern")) {
                // TODO: Basic for now. Don't know what to do with apostrophes
                handler.setSuffix(parameter.getParamValue());
                continue;
            }

            if (parameter.getParamName().equalsIgnoreCase("Threshold")) {
                handler.setLevel(parameter.getParamValue());
                continue;
            }
        }
        handler.setFormatter(appender.getLayoutParamValue());
        return handler;
    }

    private SizeRotFileHandler createSizeRotFileHandler(Appender appender, MigrationContext ctx){
        SizeRotFileHandler handler = new SizeRotFileHandler();
        handler.setName(appender.getAppenderName());

        for (Parameter parameter : appender.getParameters()) {
            if (parameter.getParamName().equalsIgnoreCase("Append")) {
                handler.setAppend(parameter.getParamValue());
                continue;
            }

            if (parameter.getParamName().equals("File")) {
                String value = parameter.getParamValue();

                //TODO: Problem with bad parse? same thing in DailyRotating
                handler.setRelativeTo("jboss.server.log.dir");
                handler.setPath(StringUtils.substringAfterLast(value, "/"));

                CopyMemory copyMemory = new CopyMemory();
                copyMemory.setName(StringUtils.substringAfterLast(value, "/"));
                copyMemory.setType("log");
                ctx.getCopyMemories().add(copyMemory);
            }

            if (parameter.getParamName().equalsIgnoreCase("MaxFileSize")) {
                handler.setRotateSize(parameter.getParamValue());
                continue;
            }

            if (parameter.getParamName().equalsIgnoreCase("MaxBackupIndex")) {
                handler.setMaxBackupIndex(parameter.getParamValue());
                continue;
            }

            if (parameter.getParamName().equalsIgnoreCase("Threshold")) {
                handler.setLevel(parameter.getParamValue());
                continue;
            }
        }

        handler.setFormatter(appender.getLayoutParamValue());
        return handler;
    }

    private AsyncHandler createAsyncHandler(Appender appender){
        AsyncHandler handler = new AsyncHandler();
        handler.setName(appender.getAppenderName());
        for (Parameter parameter : appender.getParameters()) {
            if (parameter.getParamName().equalsIgnoreCase("BufferSize")) {
                handler.setQueueLength(parameter.getParamValue());
                continue;
            }

            if (parameter.getParamName().equalsIgnoreCase("Blocking")) {
                handler.setOverflowAction(parameter.getParamValue());
                continue;
            }
        }

        Set<String> appendersRef = new HashSet();

        for (String ref : appender.getAppenderRefs()) {
            appendersRef.add(ref);
        }

        handler.setSubhandlers(appendersRef);
        handler.setFormatter(appender.getLayoutParamValue());
        return handler;
    }

    private ConsoleHandler createConsoleHandler(Appender appender){
        ConsoleHandler handler = new ConsoleHandler();
        handler.setName(appender.getAppenderName());

        for (Parameter parameter : appender.getParameters()) {
            if (parameter.getParamName().equalsIgnoreCase("Target")) {
                handler.setTarget(parameter.getParamValue());
                continue;
            }

            if (parameter.getParamName().equalsIgnoreCase("Threshold")) {
                handler.setLevel(parameter.getParamValue());
                continue;
            }
        }

        handler.setFormatter(appender.getLayoutParamValue());
        return handler;
    }

    private CustomHandler createCustomHandler(Appender appender){
        CustomHandler handler = new CustomHandler();
        handler.setName(appender.getAppenderName());
        handler.setClassValue(appender.getAppenderClass());
        Set<Property> properties = new HashSet();

        for (Parameter parameter : appender.getParameters()) {
            if (parameter.getParamName().equalsIgnoreCase("Threshold")) {
                handler.setLevel(parameter.getParamValue());
                continue;
            }

            Property property = new Property();
            property.setName(parameter.getParamName());
            property.setValue(parameter.getParamValue());
            properties.add(property);
        }

        handler.setProperties(properties);
        handler.setFormatter(appender.getLayoutParamValue());
        return handler;
    }

    private FileHandler createFileHandler(Appender appender){
        return null;
    }
}

