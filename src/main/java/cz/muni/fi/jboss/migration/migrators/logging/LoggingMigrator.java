package cz.muni.fi.jboss.migration.migrators.logging;

import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.ex.ApplyMigrationException;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.MigrationException;

import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import javafx.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Migrator of logging subsystem implementing IMigrator
 *
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
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException{
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(LoggingAS5.class).createUnmarshaller();
            File file = new File(globalConfig.getDirAS5() + globalConfig.getProfileAS5() + File.separator +
                    "conf" + File.separator + "jboss-log4j.xml");

            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(file));



            LoggingAS5 loggingAS5;

            if(file.canRead()){
               loggingAS5 = (LoggingAS5)unmarshaller.unmarshal(xsr);
            }else{
                throw new LoadMigrationException("Cannot find/open file: " + file.getAbsolutePath(), new
                        FileNotFoundException());
            }

            MigrationData mData = new MigrationData();
            mData.getConfigFragment().addAll(loggingAS5.getCategories());
            mData.getConfigFragment().addAll(loggingAS5.getAppenders());
            mData.getConfigFragment().add(loggingAS5.getRootLoggerAS5());

            ctx.getMigrationData().put(LoggingMigrator.class, mData);



        } catch (JAXBException e) {
            throw new LoadMigrationException(e);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void apply(MigrationContext ctx) throws ApplyMigrationException{
        try {
            Document doc = ctx.getStandaloneDoc();
            NodeList subsystems = doc.getElementsByTagName("subsystem");
            for(int i = 0; i < subsystems.getLength(); i++){
                if(!(subsystems.item(i) instanceof Element)){
                    continue;
                }
                if(((Element) subsystems.item(i)).getAttribute("xmlns").contains("logging")){
                    Node parent = subsystems.item(i);
                    Node lastNode = parent.getLastChild();
                    Node firstNode = parent.getFirstChild();

                    while(!(lastNode instanceof Element)){
                        lastNode = lastNode.getPreviousSibling();
                    }
                    while(!(firstNode instanceof Element)){
                        firstNode = firstNode.getNextSibling();
                    }

                    for(Node node : generateDomElements(ctx)){
                        Node adopted = doc.adoptNode(node.cloneNode(true));
                        if(node.getNodeName().contains("handler")){
                            parent.insertBefore(adopted, firstNode);
                            continue;
                        }
                        if(node.getNodeName().equals("logger")){
                            parent.insertBefore(adopted, lastNode);
                            continue;
                        }
                        // TODO: Only for testing. Need change!
                        if(node.getNodeName().equals("root-logger")){
                            parent.appendChild(adopted);
                        }
                    }
                    break;

                }
            }
        } catch (MigrationException e){
            throw new ApplyMigrationException(e);
        }
    }

    @Override
    public List<Node> generateDomElements(MigrationContext ctx) throws MigrationException{
        try {
            JAXBContext loggerCtx = JAXBContext.newInstance(Logger.class);
            JAXBContext rootLogCtx = JAXBContext.newInstance(RootLoggerAS7.class);
            JAXBContext sizeHandlerCtx = JAXBContext.newInstance(SizeRotFileHandler.class);
            JAXBContext asyncHandlerCtx = JAXBContext.newInstance(AsyncHandler.class);
            JAXBContext perHandlerCtx = JAXBContext.newInstance(PerRotFileHandler.class);
            JAXBContext consoleHandlerCtx = JAXBContext.newInstance(ConsoleHandler.class);
            JAXBContext customHandlerCtx = JAXBContext.newInstance(CustomHandler.class);
            List<Node> nodeList = new ArrayList();
            Marshaller logMarshaller = loggerCtx.createMarshaller();
            Marshaller rootLogMarshaller = rootLogCtx.createMarshaller();
            Marshaller perHandMarshaller = perHandlerCtx.createMarshaller();
            Marshaller cusHandMarshaller = customHandlerCtx.createMarshaller();
            Marshaller asyHandMarshaller = asyncHandlerCtx.createMarshaller();
            Marshaller sizeHandMarshaller = sizeHandlerCtx.createMarshaller();
            Marshaller conHandMarshaller = consoleHandlerCtx.createMarshaller();

            for (IConfigFragment fragment : ctx.getMigrationData().get(LoggingMigrator.class).getConfigFragment()) {
                if(fragment instanceof Appender){
                    Appender appender = (Appender) fragment;
                    String type = appender.getAppenderClass();

                    switch (StringUtils.substringAfterLast(type, ".")) {
                        case "DailyRollingFileAppender": {
                            Document doc = ctx.getDocBuilder().newDocument();
                            perHandMarshaller.marshal(createPerRotFileHandler(appender, ctx), doc);
                            nodeList.add(doc.getDocumentElement());
                        }
                        break;
                        case "RollingFileAppender": {
                            Document doc = ctx.getDocBuilder().newDocument();
                            sizeHandMarshaller.marshal(createSizeRotFileHandler(appender, ctx), doc);
                            nodeList.add(doc.getDocumentElement());
                        }
                        break;
                        case "ConsoleAppender": {
                            Document doc = ctx.getDocBuilder().newDocument();
                            conHandMarshaller.marshal(createConsoleHandler(appender), doc);
                            nodeList.add(doc.getDocumentElement());
                        }
                        break;
                        case "AsyncAppender":{
                            Document doc = ctx.getDocBuilder().newDocument();
                            asyHandMarshaller.marshal(createAsyncHandler(appender), doc);
                            nodeList.add(doc.getDocumentElement());
                        }
                        break;
                        // TODO: There is not such thing as FileAppender in AS5. Only sizeRotating or dailyRotating
                        // TODO: So i think that FileAppender in AS7 is then useless?
                        // THINK !!

                        //case "FileAppender" :

                        // Basic implementation of Custom Handler
                        //TODO: Problem with module
                        default: {
                            Document doc = ctx.getDocBuilder().newDocument();
                            cusHandMarshaller.marshal(createCustomHandler(appender), doc);
                            nodeList.add(doc.getDocumentElement());
                        }
                        break;
                    }
                    continue;
                }

                if(fragment instanceof Category){
                    Category category = (Category) fragment;
                    Logger logger = new Logger();
                    logger.setLoggerCategory(category.getCategoryName());
                    logger.setLoggerLevelName(category.getCategoryValue());
                    logger.setHandlers(category.getAppenderRef());

                    Document doc = ctx.getDocBuilder().newDocument();
                    logMarshaller.marshal(logger, doc);
                    nodeList.add(doc.getDocumentElement());

                    continue;
                }

                if(fragment instanceof RootLoggerAS5){
                    RootLoggerAS5 root =  (RootLoggerAS5) fragment;
                    RootLoggerAS7 rootLoggerAS7 = new RootLoggerAS7();
                    /*
                    TODO: Problem with level, because there is relative path in AS:<priority value="${jboss.server.log.threshold}"/>
                    for now only default INFO
                    */
                    rootLoggerAS7.setRootLoggerLevel("INFO");
                    rootLoggerAS7.setRootLoggerHandlers(root.getRootAppenderRefs());

                    Document doc = ctx.getDocBuilder().newDocument();
                    rootLogMarshaller.marshal(rootLoggerAS7, doc);
                    nodeList.add(doc.getDocumentElement());

                    continue;
                }

                throw new MigrationException("Error: Object is not part of Logging migration!");
            }

            return nodeList;

        } catch (Exception e) {
            throw new MigrationException(e);
        }
    }

    @Override
    public List<String> generateCliScripts(MigrationContext ctx) throws CliScriptException{
        try {
            List<String> list = new ArrayList();
            Unmarshaller logUnmarshaller = JAXBContext.newInstance(Logger.class).createUnmarshaller();
            Unmarshaller rootUnmarshaller = JAXBContext.newInstance(RootLoggerAS7.class).createUnmarshaller();
            Unmarshaller perHandUnmarshaller = JAXBContext.newInstance(PerRotFileHandler.class).createUnmarshaller();
            Unmarshaller sizeHandUnmarshaller = JAXBContext.newInstance(SizeRotFileHandler.class).createUnmarshaller();
            Unmarshaller asyHandUnmarshaller = JAXBContext.newInstance(AsyncHandler.class).createUnmarshaller();
            Unmarshaller cusHandUnmarshaller = JAXBContext.newInstance(CustomHandler.class).createUnmarshaller();
            Unmarshaller conHandUnmarshaller = JAXBContext.newInstance(ConsoleHandler.class).createUnmarshaller();

            for(Node node : generateDomElements(ctx)){
                if(node.getNodeName().equals("logger")){
                    Logger log = (Logger) logUnmarshaller.unmarshal(node);
                    list.add(createLoggerScript(log, ctx));
                    continue;
                }
                // TODO: Check how add root-logger or change with CLI then implement
//                if(node.getNodeName().equals("root-logger")){
//                    RootLoggerAS7 root = (RootLoggerAS7) rootUnmarshaller.unmarshal(node);
//                    list.add(cr(virtual, ctx));
//                    continue;
//                }
                if(node.getNodeName().equals("size-rotating-handler")){
                    SizeRotFileHandler sizeHandler = (SizeRotFileHandler) sizeHandUnmarshaller.unmarshal(node);
                    list.add(createSizeHandlerScript(sizeHandler, ctx));
                    continue;
                }
                if(node.getNodeName().equals("periodic-rotating-file-handler")){
                    PerRotFileHandler perHandler = (PerRotFileHandler) perHandUnmarshaller.unmarshal(node);
                    list.add(createPerHandlerScript(perHandler, ctx));
                    continue;
                }
                if(node.getNodeName().equals("custom-handler")){
                    CustomHandler cusHandler = (CustomHandler) cusHandUnmarshaller.unmarshal(node);
                    list.add(createCustomHandlerScript(cusHandler, ctx));
                    continue;
                }
                if(node.getNodeName().equals("async-handler")){
                    AsyncHandler asyncHandler = (AsyncHandler) asyHandUnmarshaller.unmarshal(node);
                    list.add(createAsyncHandlerScript(asyncHandler, ctx));
                    continue;
                }
                if(node.getNodeName().equals("console-handler")){
                    ConsoleHandler conHandler = (ConsoleHandler) conHandUnmarshaller.unmarshal(node);
                    list.add(createConsoleHandlerScript(conHandler, ctx));
                    continue;
                }
            }

            return list;
        } catch (MigrationException e) {
            throw new CliScriptException(e);
        } catch (JAXBException e) {
            throw new CliScriptException(e);
        }
    }

    /**
     *  Method for migrating Periodic-Rotating-File-Appender to Handler in AS7
     *
     * @param appender object representing Periodic-Rotating-File-Appender
     * @param ctx  migration context
     * @return migrated Periodic-Rotating-File-Handler object
     */
    public PerRotFileHandler createPerRotFileHandler(Appender appender, MigrationContext ctx){
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

    /**
     *  Method for migrating Size-Rotating-File-Appender to Handler in AS7
     *
     * @param appender object representing Size-Rotating-File-Appender
     * @param ctx  migration context
     * @return migrated Size-Rotating-File-Handler object
     */
    public SizeRotFileHandler createSizeRotFileHandler(Appender appender, MigrationContext ctx){
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

    /**
     *  Method for migrating Async-Appender to Handler in AS7
     *
     * @param appender object representing Async-Appender
     * @return migrated Async-Handler object
     */
    public AsyncHandler createAsyncHandler(Appender appender){
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

    /**
     *  Method for migrating Console-Appender to Handler in AS7
     *
     * @param appender object representing Console-Appender
     * @return migrated Console-Handler object
     */
    public ConsoleHandler createConsoleHandler(Appender appender){
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

    /**
     *  Method for migrating Custom-Appender to Handler in AS7
     *
     * @param appender object representing Custom-Appender
     * @return migrated Custom-Handler object
     */
    public CustomHandler createCustomHandler(Appender appender){
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

    /**
     * Not implemented yet. Not sure if it is necesarry..
     *  Method for migrating File-Appender to Handler in AS7
     *
     * @param appender object representing Periodic-Rotating-File-Appender
     * @return migrated File-Handler object
     */
    public FileHandler createFileHandler(Appender appender){
        return null;
    }

    /**
     * Creating CLI script for adding Logger
     *
     * @param logger object of Logger
     * @param ctx  migration context
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public String createLoggerScript(Logger logger, MigrationContext ctx) throws CliScriptException{
        if((logger.getLoggerLevelName() == null) || (logger.getLoggerLevelName().isEmpty())){
            throw new CliScriptException("Error:name of the logger cannot be null of empty", new NullPointerException());
        }

        String script = "/subsystem=logging/logger=" + logger.getLoggerCategory() + ":add(";
        script = ctx.checkingMethod(script, "level", logger.getLoggerLevelName());
        script = ctx.checkingMethod(script, ", use-parent-handlers", logger.getUseParentHandlers());

        if(logger.getHandlers() != null){
            String handlers = "";
            for(String handler : logger.getHandlers()){
                handlers = handlers.concat(",\"" + handler + "\"");
            }
            if(!handlers.isEmpty()){
                handlers = handlers.replaceFirst("\\,","");
                script = script.concat(", handlers=[" + handlers +"])");
            } else{
                script = script.concat(")");
            }
        } else {
            script = script.concat(")");
        }
        return script;

    }

    /**
     * Creating CLI script for adding Periodic-Rotating-File-Handler
     *
     * @param periodic object of Periodic-Rotating-File-Handler
     * @param ctx  migration context
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public String createPerHandlerScript(PerRotFileHandler periodic, MigrationContext ctx) throws CliScriptException{
        if((periodic.getName() ==  null) || (periodic.getName().isEmpty())){
            throw new CliScriptException("Error: name of the periodic rotating handler cannot be null or empty",
                    new NullPointerException());
        }

        if((periodic.getSuffix() == null) || (periodic.getSuffix().isEmpty())){
            throw new CliScriptException("Error: suffix in periodic rotating handler cannot be null or empty",
                    new NullPointerException());
        }

        if((periodic.getFileRelativeTo() == null) || (periodic.getFileRelativeTo().isEmpty())){
            throw new CliScriptException("Error: relative-to in <file> in periodic rotating handler"
                    +"cannot be null or empty",
                    new NullPointerException());
        }

        if((periodic.getPath() == null) || (periodic.getPath().isEmpty())){
            throw new CliScriptException("Error:  path in <file> in periodic rotating handler cannot"
                    +" be null or empty", new NullPointerException());
        }

        String script = "/subsystem=logging/periodic-rotating-file-handler=";
        script = script.concat(periodic.getName() + ":add(");
        script = script.concat("file={\"relative-to\"=>\"" + periodic.getFileRelativeTo()+"\"");
        script = script.concat(", \"path\"=>\"" + periodic.getPath() + "\"}");
        script = script.concat(", suffix=" + periodic.getSuffix());
        script = ctx.checkingMethod(script, ", level", periodic.getLevel());
        script = ctx.checkingMethod(script, ", formatter", periodic.getFormatter());
        script = ctx.checkingMethod(script, ", autoflush", periodic.getAutoflush());
        script = ctx.checkingMethod(script, ", append", periodic.getAppend());
        script = script.concat(")");

        return script;
    }

    /**
     * Creating CLI script for adding Size-Rotating-File-Handler
     *
     * @param sizeHandler object of Size-Rotating-File-Handler
     * @param ctx  migration context
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public String createSizeHandlerScript(SizeRotFileHandler sizeHandler, MigrationContext ctx) throws CliScriptException{
        if((sizeHandler.getName() == null) || (sizeHandler.getName().isEmpty())){
            throw new CliScriptException("Error: name of the size rotating handler cannot be null or empty",
                    new NullPointerException());
        }

        if((sizeHandler.getRelativeTo() == null) || (sizeHandler.getPath().isEmpty())){
            throw new CliScriptException("Error: relative-to in <file> in size rotating handler cannot be null or empty",
                    new NullPointerException());
        }

        if((sizeHandler.getPath() ==  null) || (sizeHandler.getPath().isEmpty())){
            throw new CliScriptException("Error: path in <file> in size rotating handler cannot be null or empty",
                    new NullPointerException());
        }

        String script = "/subsystem=logging/size-rotating-file-handler=";
        script = script.concat(sizeHandler.getName() + ":add(");
        script = script.concat("file={\"" + sizeHandler.getRelativeTo() + "\"=>\"" + sizeHandler.getPath() + "\"}");
        script = ctx.checkingMethod(script, "level", sizeHandler.getLevel());
        script = ctx.checkingMethod(script, ", filter", sizeHandler.getFilter());
        script = ctx.checkingMethod(script, ", formatter", sizeHandler.getFormatter());
        script = ctx.checkingMethod(script, ", autoflush", sizeHandler.getAutoflush());
        script = ctx.checkingMethod(script, ", append", sizeHandler.getAppend());
        script = ctx.checkingMethod(script, ", rotate-size", sizeHandler.getRotateSize());
        script = ctx.checkingMethod(script, ", max-backup-index", sizeHandler.getMaxBackupIndex());
        script = script.concat(")");

        return script;

    }

    /**
     * Creating CLI script for adding Async-Handler
     *
     * @param asyncHandler object of Async-Handler
     * @param ctx  migration context
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public String createAsyncHandlerScript(AsyncHandler asyncHandler, MigrationContext ctx) throws  CliScriptException{
        if((asyncHandler.getQueueLength() == null) || (asyncHandler.getQueueLength().isEmpty())){
            throw new CliScriptException("Error: queue-length in async handler cannot be null or empty",
                    new NullPointerException());
        }

        if((asyncHandler.getName() == null) || (asyncHandler.getName().isEmpty())){
            throw new CliScriptException("Error: name of the async handler cannot be null or empty",
                    new NullPointerException());
        }

        String script = "/subsystem=logging/async-handler=";
        script = script.concat(asyncHandler.getName() + ":add(");
        script = script.concat("queue-length=" + asyncHandler.getQueueLength());
        script = ctx.checkingMethod(script, ", level", asyncHandler.getLevel());
        script = ctx.checkingMethod(script, ", filter", asyncHandler.getFilter());
        script = ctx.checkingMethod(script, ", formatter", asyncHandler.getFormatter());
        script = ctx.checkingMethod(script, ", overflow-action", asyncHandler.getOverflowAction());

        if(asyncHandler.getSubhandlers() != null){
            String handlers = "";
            for(String subhandler  : asyncHandler.getSubhandlers()){
                handlers=", \"" + subhandler + "\"";
            }
            handlers = handlers.replaceFirst("\\, ", "");
            if(!handlers.isEmpty()){
                script = script.concat(", subhandlers=[" + handlers +"]");
            }
        }

        script = script.concat(")");

        return script;

    }

    /**
     * Creating CLI script for adding Console-Handler
     *
     * @param consoleHandler object of Console-Handler
     * @param ctx  migration context
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public String createConsoleHandlerScript(ConsoleHandler consoleHandler, MigrationContext ctx) throws CliScriptException{
        if((consoleHandler.getName() == null) || (consoleHandler.getName().isEmpty())){
            throw new CliScriptException("Error: name of the console handler cannot be null or empty",
                    new NullPointerException());
        }

        String script = "/subsystem=logging/console-handler=";
        script = script.concat(consoleHandler.getName() + ":add(");
        script = ctx.checkingMethod(script, "level", consoleHandler.getLevel());
        script = ctx.checkingMethod(script, ", filter", consoleHandler.getFilter());
        script = ctx.checkingMethod(script, ", formatter", consoleHandler.getFormatter());
        script = ctx.checkingMethod(script, ", autoflush", consoleHandler.getAutoflush());
        script = ctx.checkingMethod(script, ", target", consoleHandler.getTarget());
        script = script.concat(")");

        return script;
    }

    /**
     * Creating CLI script for adding Custom-Handler
     *
     * @param customHandler object ofCustom-Handler
     * @param ctx  migration context
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public String createCustomHandlerScript (CustomHandler customHandler, MigrationContext ctx) throws  CliScriptException{
        if((customHandler.getName() == null) || (customHandler.getName().isEmpty())){
            throw new CliScriptException("Error: name of the custom handler cannot be null or empty",
                    new NullPointerException());
        }

        if((customHandler.getModule() == null) || (customHandler.getModule().isEmpty())){
            throw new CliScriptException("Error: module in the custom handler cannot be null or empty",
                    new NullPointerException());
        }

        if((customHandler.getClassValue() == null) || (customHandler.getClassValue().isEmpty())){
            throw new CliScriptException("Error: class in the custom handler cannot be null or empty",
                    new NullPointerException());
        }

        String script = "/subsystem=logging/custom-handler=";
        script = script.concat(customHandler.getName() + ":add(");
        script = ctx.checkingMethod(script, "level", customHandler.getLevel());
        script = ctx.checkingMethod(script, ", filter", customHandler.getFilter());
        script = ctx.checkingMethod(script, ", formatter", customHandler.getFormatter());
        script = ctx.checkingMethod(script, ", class", customHandler.getClassValue());
        script = ctx.checkingMethod(script, ", module", customHandler.getModule());

        if(customHandler.getProperties() != null){
            String properties = "";
            for(Property property : customHandler.getProperties()){
                properties = properties.concat(", \"" + property.getName() + "\"=>");
                properties = properties.concat("\"" + property.getValue() + "\"");
            }

            if(!properties.isEmpty()){
                properties = properties.replaceFirst("\\, ", "");
                script = script.concat(", properties={" + properties + "}");
            }
        }

        script = script.concat(")");

        return script;
    }

}

