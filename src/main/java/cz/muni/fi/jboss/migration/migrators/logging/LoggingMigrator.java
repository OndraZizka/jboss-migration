package cz.muni.fi.jboss.migration.migrators.logging;

import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.actions.CliCommandAction;
import cz.muni.fi.jboss.migration.actions.IMigrationAction;
import cz.muni.fi.jboss.migration.actions.ModuleCreationAction;
import cz.muni.fi.jboss.migration.conf.Configuration;
import cz.muni.fi.jboss.migration.conf.GlobalConfiguration;
import cz.muni.fi.jboss.migration.ex.CliBatchException;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.migrators.logging.jaxb.*;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.utils.AS7CliUtils;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Migrator of logging subsystem implementing IMigrator
 *
 * @author Roman Jakubco
 */
public class LoggingMigrator extends AbstractMigrator {
    
    private final static String CLI_PROP__LOG_DIR      = "jboss.server.log.dir";
    private final static String AS5_PROP__LOG_TRESHOLD = "jboss.server.log.threshold";

    
    // Configurables
    @Override protected String getConfigPropertyModuleName() { return "logging"; }
    
    private String rootLoggerTreshold = "INFO";
    private String getRootLoggerTreshold() { return rootLoggerTreshold; }

    @Override
    public int examineConfigProperty( Configuration.ModuleSpecificProperty prop ) {
        if( ! getConfigPropertyModuleName().equals(  prop.getModuleId() )) return 0;
        switch( prop.getPropName() ){
            case "rootLoggerTreshold":
            case AS5_PROP__LOG_TRESHOLD:
                this.rootLoggerTreshold = prop.getValue();
                return 1;
        }
        return 0;
    }
    
    
    // Sequence number for driver names.
    // TODO: Perhaps move this property to migration context.
    private int number = 1;
    
    
    

    public LoggingMigrator(GlobalConfiguration globalConfig, MultiValueMap config) {
        super(globalConfig, config);
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException {
        try {
            File log4jConfFile = Utils.createPath( 
                    super.getGlobalConfig().getAS5Config().getDir(),  "server",
                    super.getGlobalConfig().getAS5Config().getProfileName(),
                    "conf", "jboss-log4j.xml");

            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(log4jConfFile));

            //if( ! log4jConfFile.canRead())
            //    throw new LoadMigrationException("Cannot find/open file: " + log4jConfFile.getAbsolutePath());
            
            Unmarshaller unmarshaller = JAXBContext.newInstance(LoggingAS5Bean.class).createUnmarshaller();
            LoggingAS5Bean loggingAS5 = (LoggingAS5Bean) unmarshaller.unmarshal(xsr);

            MigrationData mData = new MigrationData();

            if(loggingAS5.getCategories() != null){
                mData.getConfigFragments().addAll(loggingAS5.getCategories());
            }

            if(loggingAS5.getLoggers() != null){
                mData.getConfigFragments().addAll(loggingAS5.getLoggers());
            }

            mData.getConfigFragments().addAll(loggingAS5.getAppenders());
            mData.getConfigFragments().add(loggingAS5.getRootLoggerAS5());

            ctx.getMigrationData().put(LoggingMigrator.class, mData);
        }
        catch (JAXBException | XMLStreamException e) {
            throw new LoadMigrationException(e);
        }
    }
    
    
    
    @Override
    public void createActions(MigrationContext ctx) throws MigrationException {
        
        List<CustomHandlerBean> customHandlers = new LinkedList();

        for( IConfigFragment fragment : ctx.getMigrationData().get(LoggingMigrator.class).getConfigFragments() ){
            if (fragment instanceof AppenderBean) {
                AppenderBean appender = (AppenderBean) fragment;
                CustomHandlerBean customHandler = processAppenderBean( appender, ctx );
                if( null != customHandler )
                    customHandlers.add( customHandler );
                continue;
            }

            if (fragment instanceof CategoryBean) {
                try {
                    CliCommandAction action = createLoggerCliAction( ctx, migrateCategory((CategoryBean) fragment), this.getIfExists());
                    ctx.getActions().add( action );
                } catch (CliScriptException e) {
                    throw new MigrationException("Migration of the Category failed: " + e.getMessage(), e);
                }
                continue;
            }

            if (fragment instanceof RootLoggerAS5Bean) {
                RootLoggerAS5Bean root = (RootLoggerAS5Bean) fragment;
                // TODO: Find way to create CLI API command for root-logger
                continue;
            }

            throw new MigrationException("Config fragment unrecognized by " + this.getClass().getSimpleName() + ": " + fragment );
        }

        HashMap<File, String> tempModules = new HashMap();
        for (CustomHandlerBean handler : customHandlers) {
            ctx.getActions().addAll(createCustomHandlerActions(handler, tempModules));
        }
    }

    
    private List<IMigrationAction> createCustomHandlerActions(CustomHandlerBean handler,
                                                              HashMap<File, String> tempModules)
            throws MigrationException {
        File src;
        try {
            src = Utils.findJarFileWithClass(handler.getClassValue(), getGlobalConfig().getAS5Config().getDir(),
                    getGlobalConfig().getAS5Config().getProfileName());
        } catch (IOException ex) {
            throw new MigrationException("Failed finding jar with class " + handler.getClassValue() + ": " + ex.getMessage(), ex);
        }

        List<IMigrationAction> actions = new LinkedList();

        if (tempModules.containsKey(src)) {
            // It means that moduleAction is already set. No need for another one => create CLI for CustomHandler and
            // continue on the next iteration
            try {
                handler.setModule(tempModules.get(src));
                actions.add(createCustomHandlerCliAction(handler));
            }
            catch (CliScriptException ex) {
                throw new MigrationException("Failed creating a CLI command for appeneder " + handler.getName() + ": " + ex.getMessage(), ex);
            }

            return actions;
        }

        
        // Handler jar is new => create ModuleCreationAction, new module and CLI script
        try {
            
            handler.setModule("logging.customHandler" + number);
            tempModules.put(src, handler.getModule());

            actions.add(createCustomHandlerCliAction(handler));

            // TODO: MIGR-60 Rewrite ModuleCreationAction to be about module metadata, not paths and XML doc 
            File targetDir = Utils.createPath(getGlobalConfig().getAS7Config().getModulesDir(), 
                    "logging/customHandler" + number, "main", src.getName());

            Document doc  =  LoggingUtils.createLoggingModuleXML(handler.getModule(), src.getName());

            // Default for now => false
            ModuleCreationAction moduleAction = new ModuleCreationAction( this.getClass(), src, targetDir, doc, false);
            actions.add(moduleAction);
            number++;
        }
        catch (ParserConfigurationException e) {
            throw new MigrationException("Failed creating Custom-Handler module.xml: " + e.getMessage(), e);
        }
        catch (CliScriptException e) {
            throw new MigrationException("Migration of the appeneder " + handler.getName() + " failed (CLI command): " + e.getMessage(), e);
        }

        return actions;
    }

    

    /**
     *  Processes AppenderBean. Only used above.
     */
    private CustomHandlerBean processAppenderBean( AppenderBean appenderBean, MigrationContext ctx ) throws MigrationException {
        
        // Selection of classes which are stored in log4j or jboss logging jars.
        String cls = appenderBean.getAppenderClass();
        if( ! (cls.startsWith("org.apache.log4j") || cls.startsWith("org.jboss.logging.appender")) ){
            
            // Selection of classes which are created by the user
            // In situation that the user creates own class with same name as classes in log4j or jboss logging => CustomHandler
            // Module for these handlers must be set with creation of ModuleCreationAction
            return createCustomHandler(appenderBean, true);
        }
            

        try {
            String appenderType = StringUtils.substringAfterLast(cls, ".");
            CliCommandAction action;

            switch( appenderType ) {
                case "DailyRollingFileAppender":{
                    PerRotFileHandlerBean handler = createPerRotFileHandler(appenderBean, ctx);
                    action = createPerRotHandlerCliAction(handler);
                } break;
                case "RollingFileAppender":{
                    SizeRotFileHandlerBean handler = createSizeRotFileHandler(appenderBean, ctx);
                    action = createSizeRotHandlerCliAction(handler);
                } break;
                case "ConsoleAppender":{
                    ConsoleHandlerBean handler = createConsoleHandler(appenderBean);
                    action = createConsoleHandlerCliAction(handler);
                } break;
                case "AsyncAppender":{
                    AsyncHandlerBean handler = createAsyncHandler(appenderBean);
                    action = createAsyncHandleCliAction(handler);
                } break;

                //  If the class don't correspond to any type of AS7 handler => CustomHandler
                default:{
                    // Module of these handler will be set in the method. Module log4j.
                    CustomHandlerBean handler = createCustomHandler(appenderBean, false);
                    action = createCustomHandlerCliAction(handler);
                }
            }

            ctx.getActions().add( action );
        } catch (CliScriptException e) {
            throw new MigrationException("Migration of the appender " + appenderBean.getAppenderName() + " failed: " + e.getMessage(), e);
        }
        return null;

    }// processAppenderBean()

    /**
     * Migrates a Category from AS5 into Logger in AS7
     *
     * @param category object representing category from AS5
     * @return created object of Logger in AS7
     */
    private static LoggerBean migrateCategory(CategoryBean category){
        LoggerBean logger = new LoggerBean();

        logger.setLoggerCategory(category.getCategoryName());
        logger.setLoggerLevelName(category.getCategoryValue());
        logger.setHandlers(category.getAppenderRef());

        return logger;
    }

    
    /**
     * Migrates a root-logger from AS5 into a root-logger in AS7
     *
     * @param loggerAS5 object representing root-logger from AS5
     * @return created object of root-logger from AS7
     */
    private RootLoggerAS7Bean migrateRootLogger(RootLoggerAS5Bean loggerAS5){
        RootLoggerAS7Bean rootLoggerAS7 = new RootLoggerAS7Bean();
        // Defined as reference to sys prop in AS 5: <priority value="${jboss.server.log.threshold}"/>
        if(loggerAS5.getRootPriorityValue().equals("${" + AS5_PROP__LOG_TRESHOLD + "}")) {
            rootLoggerAS7.setRootLoggerLevel( this.getRootLoggerTreshold() );
        } else{
            rootLoggerAS7.setRootLoggerLevel(loggerAS5.getRootPriorityValue());
        }

        rootLoggerAS7.setRootLoggerHandlers(loggerAS5.getRootAppenderRefs());

        return rootLoggerAS7;
    }

    /**
     * Migrates a Periodic-Rotating-File-Appender to a Handler in AS7
     *
     * @param appender object representing Periodic-Rotating-File-Appender
     * @param ctx      migration context
     * @return migrated Periodic-Rotating-File-Handler object
     */
    static PerRotFileHandlerBean createPerRotFileHandler(AppenderBean appender, MigrationContext ctx) {
        
        PerRotFileHandlerBean handler = new PerRotFileHandlerBean();
        handler.setName(appender.getAppenderName());
        if(appender.getParameters() != null){
            for (ParameterBean parameter : appender.getParameters()) {
                if (parameter.getParamName().equalsIgnoreCase("Append")) {
                    handler.setAppend(parameter.getParamValue());
                    continue;
                }

                if (parameter.getParamName().equals("File")) {
                    String value = parameter.getParamValue();
                    handler.setRelativeTo(CLI_PROP__LOG_DIR);
                    handler.setPath(StringUtils.substringAfterLast(value, "/"));
                }

                if (parameter.getParamName().equalsIgnoreCase("DatePattern")) {
                    // TODO: Basic for now. Don't know what to do with apostrophes
                    handler.setSuffix(parameter.getParamValue());
                    continue;
                }

                if (parameter.getParamName().equalsIgnoreCase("Threshold")) {
                    handler.setLevel(parameter.getParamValue());
                }
            }
        }

        handler.setFormatter(appender.getLayoutParamValue());

        return handler;
    }

    /**
     * Migrates a Size-Rotating-File-Appender to a Handler in AS7
     *
     * @param appender object representing Size-Rotating-File-Appender
     * @param ctx      migration context
     * @return migrated Size-Rotating-File-Handler object
     */
    static SizeRotFileHandlerBean createSizeRotFileHandler(AppenderBean appender, MigrationContext ctx) {
        
        SizeRotFileHandlerBean handler = new SizeRotFileHandlerBean();
        handler.setName(appender.getAppenderName());
        if(appender.getParameters() != null){
            for (ParameterBean parameter : appender.getParameters()) {
                if (parameter.getParamName().equalsIgnoreCase("Append")) {
                    handler.setAppend(parameter.getParamValue());
                    continue;
                }

                if (parameter.getParamName().equals("File")) {
                    String value = parameter.getParamValue();

                    //TODO: Problem with bad parse? same thing in DailyRotating
                    handler.setRelativeTo("jboss.server.log.dir");
                    handler.setPath(StringUtils.substringAfterLast(value, "/"));
                    continue;
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
                }
            }
        }

        handler.setFormatter(appender.getLayoutParamValue());

        return handler;
    }

    /**
     * Migrates a Async-Appender to a Handler in AS7
     *
     * @param appender object representing Async-Appender
     * @return migrated Async-Handler object
     */
    static AsyncHandlerBean createAsyncHandler(AppenderBean appender) {
        
        AsyncHandlerBean handler = new AsyncHandlerBean();
        handler.setName(appender.getAppenderName());
        if(appender.getParameters() != null){
            // TODO: Problem with queue-length in Async
            for (ParameterBean parameter : appender.getParameters()) {
                if (parameter.getParamName().equalsIgnoreCase("BufferSize")) {
                    handler.setQueueLength(parameter.getParamValue());
                    continue;
                }

                if (parameter.getParamName().equalsIgnoreCase("Blocking")) {
                    handler.setOverflowAction(parameter.getParamValue());
                }
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

    /**
     * Migrates a Console-Appender to a Handler in AS7
     *
     * @param appender object representing Console-Appender
     * @return migrated Console-Handler object
     */
    static ConsoleHandlerBean createConsoleHandler(AppenderBean appender) {
        
        ConsoleHandlerBean handler = new ConsoleHandlerBean();
        handler.setName(appender.getAppenderName());
        if(appender.getParameters() != null){
            for (ParameterBean parameter : appender.getParameters()) {
                if (parameter.getParamName().equalsIgnoreCase("Target")) {
                    handler.setTarget(parameter.getParamValue());
                    continue;
                }

                if (parameter.getParamName().equalsIgnoreCase("Threshold")) {
                    handler.setLevel(parameter.getParamValue());
                }
            }
        }

        handler.setFormatter(appender.getLayoutParamValue());

        return handler;
    }

    /**
     * Migrates a Custom-Appender to a Handler in AS7
     *
     * @param appender object representing Custom-Appender
     * @param custom  true if appender class is created by user, false if it is declared in log4j or jboss logging
     * @return migrated Custom-Handler object
     */
    static CustomHandlerBean createCustomHandler(AppenderBean appender, Boolean custom) {
        
        CustomHandlerBean handler = new CustomHandlerBean();
        handler.setName(appender.getAppenderName());
        handler.setClassValue(appender.getAppenderClass());

        if( ! custom ){
            // Only possibility is class in log4j=> log4j module in AS7
            handler.setModule("org.apache.log4j");
        }

        Set<PropertyBean> properties = new HashSet();
        if(appender.getParameters() != null){
            for (ParameterBean parameter : appender.getParameters()) {
                if (parameter.getParamName().equalsIgnoreCase("Threshold")) {
                    handler.setLevel(parameter.getParamValue());
                    continue;
                }

                PropertyBean property = new PropertyBean();
                property.setName(parameter.getParamName());
                property.setValue(parameter.getParamValue());
                properties.add(property);
            }
        }

        handler.setProperties(properties);
        handler.setFormatter(appender.getLayoutParamValue());

        return handler;
    }

    
    /**
     * Not implemented yet. Not sure if it is necessary..
     * Method for migrating File-Appender to Handler in AS7
     *
     * @return migrated File-Handler object
     */
    static FileHandlerBean createFileHandler(AppenderBean appender) throws CliScriptException{
        return null;
    }

    
    
    /**
     * Creates CliCommandAction for adding a Logger
     *
     * @param logger object representing Logger
     * @return created CliCommandAction for adding the Logger
     * @throws CliScriptException if required attributes for a creation of the CLI command of the logger are missing or
     *                            are empty (loggerCategory)
     */
    static CliCommandAction createLoggerCliAction( MigrationContext ctx, LoggerBean logger, Configuration.IfExists ifExists) throws CliScriptException {
        String errMsg = " in logger(Category in AS5) must be set.";
        Utils.throwIfBlank(logger.getLoggerCategory(), errMsg, "Logger name");

        // ModelNode
        ModelNode loggerCmd = new ModelNode();
        loggerCmd.get(ClientConstants.OP_ADDR).add("subsystem","logging");
        loggerCmd.get(ClientConstants.OP_ADDR).add("logger", logger.getLoggerCategory());
        
        // First, check if it exists. If so, delete first.
        // TODO: MIGR-61 Merge resources instead of skipping or replacing
        /*try {
            AS7CliUtils.removeResourceIfExists( loggerCmd, ctx.getAS7Client() );
        } catch( IOException | CliBatchException ex ) {
            throw new CliScriptException("Failed removing resource: " + ex.getMessage(), ex );
        }*/

        
        // ADD
        loggerCmd.get(ClientConstants.OP).set(ClientConstants.ADD);

        if (logger.getHandlers() != null) {
            ModelNode handlersNode = new ModelNode();
            for (String handler : logger.getHandlers()) {
                ModelNode handlerNode = new ModelNode();
                handlerNode.set(handler);
                handlersNode.add(handlerNode);
            }
            loggerCmd.get("handlers").set(handlersNode);
        }

        CliApiCommandBuilder builder = new CliApiCommandBuilder(loggerCmd);
        builder.addProperty("level", logger.getLoggerLevelName());
        builder.addProperty("use-parent-handlers", logger.getUseParentHandlers());

        return new CliCommandAction( LoggingMigrator.class, createLoggerScript(logger), builder.getCommand())
                .setIfExists( ifExists );
    }

    
    /**
     * Creates CliCommandAction for adding a Periodic-Rotating-File-Handler
     *
     * @param handler object representing Periodic-Rotating-File-Handler
     * @return  created CliCommandAction for adding the Periodic-Rotating-File-Handler
     * @throws CliScriptException if required attributes for a creation of CLI command of the handler are missing or
     *                            are empty (name, relativeTo, path, suffix)
     */
    static CliCommandAction createPerRotHandlerCliAction(PerRotFileHandlerBean handler)
            throws CliScriptException{
        String errMsg = " in periodic-rotating-file-handler(Appender in AS5) must be set.";
        Utils.throwIfBlank(handler.getName(), errMsg, "Name");
        Utils.throwIfBlank(handler.getRelativeTo(), errMsg, "Relative-to");
        Utils.throwIfBlank(handler.getPath(), errMsg, "Path");
        Utils.throwIfBlank(handler.getSuffix(), errMsg, "Suffix");

        ModelNode handlerCmd = new ModelNode();
        handlerCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        handlerCmd.get(ClientConstants.OP_ADDR).add("subsystem","logging");
        handlerCmd.get(ClientConstants.OP_ADDR).add("periodic-rotating-file-handler", handler.getName());

        ModelNode temp = new ModelNode();
        temp.get("relative-to").set(handler.getRelativeTo());
        temp.get("path").set(handler.getPath());

        handlerCmd.get("file").set(temp);

        CliApiCommandBuilder builder = new CliApiCommandBuilder(handlerCmd);
        builder.addProperty("suffix", handler.getSuffix());
        builder.addProperty("level", handler.getLevel());
        builder.addProperty("formatter", handler.getFormatter());
        builder.addProperty("autoflush", handler.getAutoflush());
        builder.addProperty("append", handler.getAppend());
        // TODO: AS7CliUtils.copyProperties(handler, builder, "level filter formatter ...");


        return new CliCommandAction( LoggingMigrator.class, createPerHandlerScript(handler), builder.getCommand());
    }
    

    /**
     * Creates CliCommandAction for adding a Size-Rotating-File-Handler
     *
     * @param handler object representing Size-Rotating-File-Handler
     * @return  created CliCommandAction for adding Size-Rotating-File-Handler
     * @throws CliScriptException if required attributes for a creation of the CLI command of the handler are missing or
     *                            are empty (name, relativeTo, path)
     */
    static CliCommandAction createSizeRotHandlerCliAction(SizeRotFileHandlerBean handler)
            throws CliScriptException{
        String errMsg = " in size-rotating-file-handler(Appender in AS5) must be set.";
        Utils.throwIfBlank(handler.getName(), errMsg, "Name");
        Utils.throwIfBlank(handler.getRelativeTo(), errMsg, "Relative-to");
        Utils.throwIfBlank(handler.getPath(), errMsg, "Path");

        ModelNode handlerCmd = new ModelNode();
        handlerCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        handlerCmd.get(ClientConstants.OP_ADDR).add("subsystem", "logging");
        handlerCmd.get(ClientConstants.OP_ADDR).add("size-rotating-file-handler", handler.getName());

        ModelNode temp = new ModelNode();
        temp.get("relative-to").set(handler.getRelativeTo());
        temp.get("path").set(handler.getPath());

        handlerCmd.get("file").set(temp);

        CliApiCommandBuilder builder = new CliApiCommandBuilder(handlerCmd);
        builder.addProperty("level", handler.getLevel());
        builder.addProperty("filter", handler.getFilter());
        builder.addProperty("formatter", handler.getFormatter());
        builder.addProperty("autoflush", handler.getAutoflush());
        builder.addProperty("append", handler.getAppend());
        builder.addProperty("rotate-size", handler.getRotateSize());
        builder.addProperty("max-backup-index", handler.getMaxBackupIndex());
        // TODO: AS7CliUtils.copyProperties(handler, builder, "level filter formatter ...");

        return new CliCommandAction( LoggingMigrator.class, createSizeHandlerScript(handler), builder.getCommand());
    }

    
    /**
     * Creates CliCommandAction for adding a Async-Handler
     *
     * @param handler object representing Async-Handler
     * @return  created CliCommandAction for adding the Async-Handler
     * @throws CliScriptException if required attributes for a creation of the CLI command of the handler are missing
     *                            or are empty (name, queueLength)
     */
    static CliCommandAction createAsyncHandleCliAction(AsyncHandlerBean handler) throws CliScriptException{
        String errMsg = " in async-handler(Appender in AS5) must be set.";
        Utils.throwIfBlank(handler.getName(), errMsg, "Name");
        Utils.throwIfBlank(handler.getQueueLength(), errMsg, "Queue length");

        ModelNode handlerCmd = new ModelNode();
        handlerCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        handlerCmd.get(ClientConstants.OP_ADDR).add("subsystem", "logging");
        handlerCmd.get(ClientConstants.OP_ADDR).add("async-handler", handler.getName());

        if (handler.getSubhandlers() != null) {
            ModelNode handlersNode = new ModelNode();
            for (String subHandler : handler.getSubhandlers()) {
                ModelNode handlerNode = new ModelNode();

                handlerNode.set(subHandler);
                handlersNode.add(handlerNode);
            }
            handlerCmd.get("handlers").set(handlersNode);
        }

        CliApiCommandBuilder builder = new CliApiCommandBuilder(handlerCmd);

        builder.addProperty("queue-length", handler.getQueueLength());
        builder.addProperty("level", handler.getLevel());
        builder.addProperty("filter", handler.getFilter());
        builder.addProperty("formatter", handler.getFormatter());
        builder.addProperty("overflow-action", handler.getOverflowAction());
        // TODO: AS7CliUtils.copyProperties(handler, builder, "... level filter formatter ...");

        return new CliCommandAction( LoggingMigrator.class, createAsyncHandlerScript(handler), builder.getCommand());
    }

    
    /**
     * Creates CliCommandAction for adding a Console-Handler
     *
     * @param handler object representing Console-Handler
     * @return  created CliCommandAction for adding the Console-Handler
     * @throws CliScriptException if required attributes for a creation of the CLI command of the handler are missing or
     *                            are empty (name)
     */
    static CliCommandAction createConsoleHandlerCliAction(ConsoleHandlerBean handler) throws CliScriptException{
        String errMsg = " in console-handler(Appender in AS5) must be set.";
        Utils.throwIfBlank(handler.getName(), errMsg, "Name");

        ModelNode handlerCmd = new ModelNode();
        handlerCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        handlerCmd.get(ClientConstants.OP_ADDR).add("subsystem", "logging");
        handlerCmd.get(ClientConstants.OP_ADDR).add("console-handler", handler.getName());

        CliApiCommandBuilder builder = new CliApiCommandBuilder(handlerCmd);
        builder.addProperty("level", handler.getLevel());
        builder.addProperty("filter", handler.getFilter());
        builder.addProperty("formatter", handler.getFormatter());
        builder.addProperty("autoflush", handler.getAutoflush());
        builder.addProperty("target", handler.getTarget());
        
        // TODO: AS7CliUtils.copyProperties(handler, builder, "level filter formatter autoflush target");

        return new CliCommandAction( LoggingMigrator.class, createConsoleHandlerScript(handler), builder.getCommand());
    }

    
    /**
     * Creates CliCommandAction for adding a CustomHandler
     *
     * @param handler object representing Custom-Handler
     * @return  created CliCommandAction for adding the Custom-Handler
     * @throws CliScriptException if required attributes for a creation of the CLI command of the handler are missing or
     *                            are empty(name, module, classValue)
     */
    static CliCommandAction createCustomHandlerCliAction(CustomHandlerBean handler) throws CliScriptException{
        String errMsg = " in custom-handler must be set.";
        Utils.throwIfBlank(handler.getName(), errMsg, "Name");
        Utils.throwIfBlank(handler.getModule(), errMsg, "Module");
        Utils.throwIfBlank(handler.getClassValue(), errMsg, "Class-value");

        ModelNode handlerCmd = new ModelNode();
        handlerCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        handlerCmd.get(ClientConstants.OP_ADDR).add("subsystem","logging");
        handlerCmd.get(ClientConstants.OP_ADDR).add("custom-handler", handler.getName());

        if (handler.getProperties() != null) {
            ModelNode propertyNode = new ModelNode();
            for (PropertyBean property : handler.getProperties()) {
                 propertyNode.get(property.getName()).set(property.getValue());
            }
            handlerCmd.get("properties").set(propertyNode);
        }

        CliApiCommandBuilder builder = new CliApiCommandBuilder(handlerCmd);
        builder.addProperty("level", handler.getLevel());
        builder.addProperty("filter", handler.getFilter());
        builder.addProperty("formatter", handler.getFormatter());
        builder.addProperty("class", handler.getClassValue());
        builder.addProperty("module", handler.getModule());
        // TODO: AS7CliUtils.copyProperties(handler, builder, "level filter formatter class module");

        return new CliCommandAction( LoggingMigrator.class, createCustomHandlerScript(handler), builder.getCommand());
    }

    
    /**
     * Creates a list of CliCommandActions for modifying a root-logger
     *
     * @param root object representing root-logger
     * @return list of created CliCommandActions
     * @throws CliScriptException
     */
    static List<CliCommandAction> createRootLoggerCliAction(RootLoggerAS7Bean root) throws CliScriptException{
        // TODO: Not sure how set handlers in root-logger. Same thing for filter attribute. For now empty
        return null;
    }

    
    /**
     * Creates a CLI script for adding a Logger
     *
     * @param logger object of Logger
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    static String createLoggerScript(LoggerBean logger) throws CliScriptException {
        String errMsg = " in logger(Category in AS5) must be set.";
        Utils.throwIfBlank(logger.getLoggerCategory(), errMsg, "Logger name");

        StringBuilder resultScript = new StringBuilder("/subsystem=logging/logger=" + logger.getLoggerCategory() + ":add(");
        
        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        builder.addProperty("level", logger.getLoggerLevelName());
        builder.addProperty("use-parent-handlers", logger.getUseParentHandlers());
        resultScript.append(builder.asString());

        if (logger.getHandlers() != null) {
            /*StringBuilder handlersBuilder = new StringBuilder();
            for (String handler : logger.getHandlers()) {
                handlersBuilder.append(",\"").append(handler).append("\"");
            }
            String handlers = handlersBuilder.toString();
            if( ! handlers.isEmpty() ) {
                handlers = handlers.replaceFirst(",", "");
                resultScript.append(", handlers=[").append(handlers).append("]");
            }
             */
            String handlersStr = AS7CliUtils.joinQuoted(logger.getHandlers());
            if( ! handlersStr.isEmpty() )
                resultScript.append(", handlers=[").append(handlersStr).append("]");
        }
        
        resultScript.append(")");
        return resultScript.toString();
    }

    
    /**
     * Creates a CLI script for adding a Periodic-Rotating-File-Handler
     *
     * @param periodic object of Periodic-Rotating-File-Handler
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    static String createPerHandlerScript(PerRotFileHandlerBean periodic)
            throws CliScriptException {
        String errMsg = " in periodic-rotating-file-handler(Appender in AS5) must be set.";
        Utils.throwIfBlank(periodic.getName(), errMsg, "Name");
        Utils.throwIfBlank(periodic.getRelativeTo(), errMsg, "Relative-to");
        Utils.throwIfBlank(periodic.getPath(), errMsg, "Path");
        Utils.throwIfBlank(periodic.getSuffix(), errMsg, "Suffix");

        StringBuilder resultScript = new StringBuilder("/subsystem=logging/periodic-rotating-file-handler=");
        resultScript.append(periodic.getName()).append(":add(");
        resultScript.append("file={\"relative-to\"=>\"").append(periodic.getRelativeTo()).append("\"");
        resultScript.append(", \"path\"=>\"").append(periodic.getPath()).append("\"}");
        resultScript.append(", suffix=").append(periodic.getSuffix()).append(", ");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        builder.addProperty("level", periodic.getLevel());
        builder.addProperty("formatter", periodic.getFormatter());
        builder.addProperty("autoflush", periodic.getAutoflush());
        builder.addProperty("append", periodic.getAppend());
        // TODO: AS7CliUtils.copyProperties(handler, builder, "level formatter autoflush append");

        resultScript.append(builder.asString()).append(")");

        return resultScript.toString();
    }
    

    /**
     * Creates a CLI script for adding a Size-Rotating-File-Handler
     *
     * @param sizeHandler object of Size-Rotating-File-Handler
     * @return string containing created CLI script
     * @throws cz.muni.fi.jboss.migration.ex.CliScriptException if required attributes are missing
     */
    static String createSizeHandlerScript(SizeRotFileHandlerBean sizeHandler)
            throws CliScriptException {
        String errMsg = " in size-rotating-file-handler(Appender in AS5) must be set.";
        Utils.throwIfBlank(sizeHandler.getName(), errMsg, "Name");
        Utils.throwIfBlank(sizeHandler.getRelativeTo(), errMsg, "Relative-to");
        Utils.throwIfBlank(sizeHandler.getPath(), errMsg, "Path");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=logging/size-rotating-file-handler=");

        resultScript.append(sizeHandler.getName()).append(":add(");
        resultScript.append("file={\"relative-to\"=>\"").append(sizeHandler.getRelativeTo()).append("\"");
        resultScript.append(", \"path\"=>\"").append(sizeHandler.getPath()).append("\"},");

        builder.addProperty("level", sizeHandler.getLevel());
        builder.addProperty("filter", sizeHandler.getFilter());
        builder.addProperty("formatter", sizeHandler.getFormatter());
        builder.addProperty("autoflush", sizeHandler.getAutoflush());
        builder.addProperty("append", sizeHandler.getAppend());
        builder.addProperty("rotate-size", sizeHandler.getRotateSize());
        builder.addProperty("max-backup-index", sizeHandler.getMaxBackupIndex());
        // TODO: AS7CliUtils.copyProperties(handler, builder, "level filter formatter autoflush ...");

        resultScript.append(builder.asString()).append(")");

        return resultScript.toString();
    }
    

    /**
     * Creates a CLI script for adding a Async-Handler
     *
     * @param asyncHandler object of Async-Handler
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    static String createAsyncHandlerScript(AsyncHandlerBean asyncHandler)
            throws CliScriptException {
        String errMsg = " in async-handler(Appender in AS5) must be set.";
        Utils.throwIfBlank(asyncHandler.getName(), errMsg, "Name");
        Utils.throwIfBlank(asyncHandler.getQueueLength(), errMsg, "Queue length");

        StringBuilder resultScript = new StringBuilder("/subsystem=logging/async-handler=");
        resultScript.append(asyncHandler.getName()).append(":add(");
        resultScript.append("queue-length=").append(asyncHandler.getQueueLength());

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        builder.addProperty("level", asyncHandler.getLevel());
        builder.addProperty("filter", asyncHandler.getFilter());
        builder.addProperty("formatter", asyncHandler.getFormatter());
        builder.addProperty("overflow-action", asyncHandler.getOverflowAction());
        // TODO: AS7CliUtils.copyProperties(handler, builder, "level filter formatter ...");

        resultScript.append(builder.asString());

        if (asyncHandler.getSubhandlers() != null) {
            StringBuilder handlersBuilder = new StringBuilder();
            for (String subHandler : asyncHandler.getSubhandlers()) {
                handlersBuilder.append(", \"").append(subHandler).append("\"");
            }

            String handlers = handlersBuilder.toString().replaceFirst(", ", "");
            if (!handlers.isEmpty()) {
                resultScript.append(", subhandlers=[").append(handlers).append("]");
            }
        }

        resultScript.append(")");

        return resultScript.toString();
    }

    
    /**
     * Creates a CLI script for adding a Console-Handler
     *
     * @param consoleHandler object of Console-Handler
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    static String createConsoleHandlerScript(ConsoleHandlerBean consoleHandler)
            throws CliScriptException {
        String errMsg = " in console-handler(Appender in AS5) must be set.";
        Utils.throwIfBlank(consoleHandler.getName(), errMsg, "Name");

        StringBuilder resultScript = new StringBuilder("/subsystem=logging/console-handler=");
        resultScript.append(consoleHandler.getName()).append(":add(");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        builder.addProperty("level", consoleHandler.getLevel());
        builder.addProperty("filter", consoleHandler.getFilter());
        builder.addProperty("formatter", consoleHandler.getFormatter());
        builder.addProperty("autoflush", consoleHandler.getAutoflush());
        builder.addProperty("target", consoleHandler.getTarget());
        // TODO: AS7CliUtils.copyProperties(handler, builder, "level filter formatter autoflush target");

        resultScript.append(builder.asString()).append(")");

        return resultScript.toString();
    }

    /**
     * Creates a CLI script for adding a Custom-Handler
     *
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    static String createCustomHandlerScript(CustomHandlerBean customHandler)
            throws CliScriptException {
        String errMsg = " in custom-handler must be set.";
        Utils.throwIfBlank(customHandler.getName(), errMsg, "Name");
        Utils.throwIfBlank(customHandler.getModule(), errMsg, "Module");
        Utils.throwIfBlank(customHandler.getClassValue(), errMsg, "Class-value");

        StringBuilder resultScript = new StringBuilder("/subsystem=logging/custom-handler=");
        resultScript.append(customHandler.getName()).append(":add(");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        builder.addProperty("level", customHandler.getLevel());
        builder.addProperty("filter", customHandler.getFilter());
        builder.addProperty("formatter", customHandler.getFormatter());
        builder.addProperty("class", customHandler.getClassValue());
        builder.addProperty("module", customHandler.getModule());
        // TODO: AS7CliUtils.copyProperties(handler, builder, "level filter formatter class module");

        resultScript.append(builder.asString());

        if( customHandler.getProperties() != null && ! customHandler.getProperties().isEmpty() ) {
            StringBuilder propertiesBuilder = new StringBuilder();
            for (PropertyBean property : customHandler.getProperties()) {
                propertiesBuilder.append(", \"").append(property.getName()).append("\"=>");
                propertiesBuilder.append('"').append(property.getValue()).append('"');
            }
            String properties = propertiesBuilder.toString();
            properties = properties.replaceFirst(", ", "");
            resultScript.append(", properties={").append(properties).append('}');
        }

        resultScript.append(")");

        return resultScript.toString();
    }

    
    /**
     *  Creates a CLI script for changing a root-logger. The Root-logger can be only modified with the CLI not added.
     *  So this method creates a script for changing the level, filter and handlers. Required attribute is level.
     *  (Changes possible in future)
     *
     * @param rootLogger object representing root-logger in AS7
     * @return  String containing scripts
     * @throws CliScriptException if attribute level is missing.
     */
    static String createRootLoggerScript(RootLoggerAS7Bean rootLogger) throws CliScriptException{
        String errMsg = " in root-logger must be set.";
        Utils.throwIfBlank(rootLogger.getRootLoggerLevel(), errMsg, "Level");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder();

        // TODO: Is filter attribute even existing in CLI and JBoss conf? Because it is impossible to set filter via CLI
        if(rootLogger.getRootLogFilValue() != null){
            resultScript.append("/subsystem=logging/root-logger=ROOT:write-attribute(");
            resultScript.append("name=filter, value=").append(rootLogger.getRootLogFilValue());
            resultScript.append(")\n");
        }

        resultScript.append("/subsystem=logging/root-logger=ROOT:write-attribute(");
        resultScript.append("name=level, value=").append(rootLogger.getRootLoggerLevel());
        resultScript.append(")\n");

        for(String handler : rootLogger.getRootLoggerHandlers()){
            resultScript.append("/subsystem=logging/root-logger=ROOT:root-logger-assign-handler(name=");
            resultScript.append(handler).append(")\n");
        }

        return resultScript.toString();
    }

}// class
