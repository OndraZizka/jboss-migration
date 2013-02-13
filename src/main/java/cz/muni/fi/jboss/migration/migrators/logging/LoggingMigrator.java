package cz.muni.fi.jboss.migration.migrators.logging;

import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.ex.*;
import cz.muni.fi.jboss.migration.migrators.logging.jaxb.*;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.collections.map.MultiValueMap;
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
import java.io.File;
import java.io.FileNotFoundException;
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
public class LoggingMigrator extends AbstractMigrator {
    
    @Override protected String getConfigPropertyModuleName() { return "logging"; }
    

    public LoggingMigrator(GlobalConfiguration globalConfig, MultiValueMap config) {
        super(globalConfig, config);
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException {
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(LoggingAS5Bean.class).createUnmarshaller();
            File file = new File(super.getGlobalConfig().getDirAS5() + super.getGlobalConfig().getProfileAS5() +
                    File.separator + "conf" + File.separator + "jboss-log4j.xml");

            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(file));

            LoggingAS5Bean loggingAS5;

            if (file.canRead()) {
                loggingAS5 = (LoggingAS5Bean) unmarshaller.unmarshal(xsr);
            } else {
                throw new LoadMigrationException("Cannot find/open file: " + file.getAbsolutePath(), new
                        FileNotFoundException());
            }

            MigrationData mData = new MigrationData();
            mData.getConfigFragment().addAll(loggingAS5.getCategories());
            mData.getConfigFragment().addAll(loggingAS5.getAppenders());
            mData.getConfigFragment().add(loggingAS5.getRootLoggerAS5());

            ctx.getMigrationData().put(LoggingMigrator.class, mData);

        } catch (JAXBException | XMLStreamException e) {
            throw new LoadMigrationException(e);
        }
    }

    @Override
    public void apply(MigrationContext ctx) throws ApplyMigrationException {
        try {
            Document doc = ctx.getStandaloneDoc();
            NodeList subsystems = doc.getElementsByTagName("subsystem");
            for (int i = 0; i < subsystems.getLength(); i++) {
                if (!(subsystems.item(i) instanceof Element)) {
                    continue;
                }
                if (((Element) subsystems.item(i)).getAttribute("xmlns").contains("logging")) {
                    Node parent = subsystems.item(i);
                    Node lastNode = parent.getLastChild();
                    Node firstNode = parent.getFirstChild();

                    while (!(lastNode instanceof Element)) {
                        lastNode = lastNode.getPreviousSibling();
                    }
                    while (!(firstNode instanceof Element)) {
                        firstNode = firstNode.getNextSibling();
                    }

                    for (Node node : generateDomElements(ctx)) {
                        Node adopted = doc.adoptNode(node.cloneNode(true));
                        if (node.getNodeName().contains("handler")) {
                            parent.insertBefore(adopted, firstNode);
                            continue;
                        }
                        if (node.getNodeName().equals("logger")) {
                            parent.insertBefore(adopted, lastNode);

                            continue;
                        }
                        // Only appending to xml what is wrong. Only for testing
                        if (node.getNodeName().equals("root-logger")) {
                            parent.appendChild(adopted);
                        }
                    }
                    break;

                }
            }
        } catch (MigrationException e) {
            throw new ApplyMigrationException(e);
        }
    }

    @Override
    public List<Node> generateDomElements(MigrationContext ctx) throws NodeGenerationException {
        try {
            JAXBContext loggerCtx = JAXBContext.newInstance(LoggerBean.class);
            JAXBContext rootLogCtx = JAXBContext.newInstance(RootLoggerAS7Bean.class);
            JAXBContext sizeHandlerCtx = JAXBContext.newInstance(SizeRotFileHandlerBean.class);
            JAXBContext asyncHandlerCtx = JAXBContext.newInstance(AsyncHandlerBean.class);
            JAXBContext perHandlerCtx = JAXBContext.newInstance(PerRotFileHandlerBean.class);
            JAXBContext consoleHandlerCtx = JAXBContext.newInstance(ConsoleHandlerBean.class);
            JAXBContext customHandlerCtx = JAXBContext.newInstance(CustomHandlerBean.class);

            List<Node> nodeList = new ArrayList();

            Marshaller logMarshaller = loggerCtx.createMarshaller();
            Marshaller rootLogMarshaller = rootLogCtx.createMarshaller();
            Marshaller perHandMarshaller = perHandlerCtx.createMarshaller();
            Marshaller cusHandMarshaller = customHandlerCtx.createMarshaller();
            Marshaller asyHandMarshaller = asyncHandlerCtx.createMarshaller();
            Marshaller sizeHandMarshaller = sizeHandlerCtx.createMarshaller();
            Marshaller conHandMarshaller = consoleHandlerCtx.createMarshaller();

            for (IConfigFragment fragment : ctx.getMigrationData().get(LoggingMigrator.class).getConfigFragment()) {
                if (fragment instanceof AppenderBean) {
                    AppenderBean appender = (AppenderBean) fragment;
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
                        case "AsyncAppender": {
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

                if (fragment instanceof CategoryBean) {
                    CategoryBean category = (CategoryBean) fragment;
                    LoggerBean logger = new LoggerBean();
                    logger.setLoggerCategory(category.getCategoryName());
                    logger.setLoggerLevelName(category.getCategoryValue());
                    logger.setHandlers(category.getAppenderRef());

                    Document doc = ctx.getDocBuilder().newDocument();
                    logMarshaller.marshal(logger, doc);
                    nodeList.add(doc.getDocumentElement());

                    continue;
                }

                if (fragment instanceof RootLoggerAS5Bean) {
                    RootLoggerAS5Bean root = (RootLoggerAS5Bean) fragment;
                    RootLoggerAS7Bean rootLoggerAS7 = new RootLoggerAS7Bean();
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

                throw new NodeGenerationException("Object is not part of Logging migration!");
            }

            return nodeList;

        } catch (JAXBException e) {
            throw new NodeGenerationException(e);
        }
    }

    @Override
    public List<String> generateCliScripts(MigrationContext ctx) throws CliScriptException {
        try {
            List<String> list = new ArrayList();

            Unmarshaller logUnmarshaller = JAXBContext.newInstance(LoggerBean.class).createUnmarshaller();
            Unmarshaller rootUnmarshaller = JAXBContext.newInstance(RootLoggerAS7Bean.class).createUnmarshaller();
            Unmarshaller perHandUnmarshaller = JAXBContext.newInstance(PerRotFileHandlerBean.class).createUnmarshaller();
            Unmarshaller sizeHandUnmarshaller = JAXBContext.newInstance(SizeRotFileHandlerBean.class).createUnmarshaller();
            Unmarshaller asyHandUnmarshaller = JAXBContext.newInstance(AsyncHandlerBean.class).createUnmarshaller();
            Unmarshaller cusHandUnmarshaller = JAXBContext.newInstance(CustomHandlerBean.class).createUnmarshaller();
            Unmarshaller conHandUnmarshaller = JAXBContext.newInstance(ConsoleHandlerBean.class).createUnmarshaller();

            for (Node node : generateDomElements(ctx)) {
                if (node.getNodeName().equals("logger")) {
                    LoggerBean log = (LoggerBean) logUnmarshaller.unmarshal(node);
                    list.add(createLoggerScript(log));
                    continue;
                }
                if(node.getNodeName().equals("root-logger")){
                    RootLoggerAS7Bean root = (RootLoggerAS7Bean) rootUnmarshaller.unmarshal(node);
                    list.add(createRootLoggerScript(root));
                    continue;
                }
                if (node.getNodeName().equals("size-rotating-handler")) {
                    SizeRotFileHandlerBean sizeHandler = (SizeRotFileHandlerBean) sizeHandUnmarshaller.unmarshal(node);
                    list.add(createSizeHandlerScript(sizeHandler));
                    continue;
                }
                if (node.getNodeName().equals("periodic-rotating-file-handler")) {
                    PerRotFileHandlerBean perHandler = (PerRotFileHandlerBean) perHandUnmarshaller.unmarshal(node);
                    list.add(createPerHandlerScript(perHandler));
                    continue;
                }
                if (node.getNodeName().equals("custom-handler")) {
                    CustomHandlerBean cusHandler = (CustomHandlerBean) cusHandUnmarshaller.unmarshal(node);
                    list.add(createCustomHandlerScript(cusHandler));
                    continue;
                }
                if (node.getNodeName().equals("async-handler")) {
                    AsyncHandlerBean asyncHandler = (AsyncHandlerBean) asyHandUnmarshaller.unmarshal(node);
                    list.add(createAsyncHandlerScript(asyncHandler));
                    continue;
                }
                if (node.getNodeName().equals("console-handler")) {
                    ConsoleHandlerBean conHandler = (ConsoleHandlerBean) conHandUnmarshaller.unmarshal(node);
                    list.add(createConsoleHandlerScript(conHandler));
                }
            }

            return list;

        } catch (NodeGenerationException | JAXBException e) {
            throw new CliScriptException(e);
        }
    }

    /**
     * Method for migrating Periodic-Rotating-File-Appender to Handler in AS7
     *
     * @param appender object representing Periodic-Rotating-File-Appender
     * @param ctx      migration context
     * @return migrated Periodic-Rotating-File-Handler object
     */
    public static PerRotFileHandlerBean createPerRotFileHandler(AppenderBean appender, MigrationContext ctx) {
        PerRotFileHandlerBean handler = new PerRotFileHandlerBean();
        handler.setName(appender.getAppenderName());

        for (ParameterBean parameter : appender.getParameters()) {
            if (parameter.getParamName().equalsIgnoreCase("Append")) {
                handler.setAppend(parameter.getParamValue());
                continue;
            }

            if (parameter.getParamName().equals("File")) {
                String value = parameter.getParamValue();


                handler.setRelativeTo("jboss.server.log.dir");
                handler.setPath(StringUtils.substringAfterLast(value, "/"));

                RollbackData rollbackData = new RollbackData();
                rollbackData.setName(StringUtils.substringAfterLast(value, "/"));
                rollbackData.setType("log");
                ctx.getRollbackDatas().add(rollbackData);
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
        handler.setFormatter(appender.getLayoutParamValue());

        return handler;
    }

    /**
     * Method for migrating Size-Rotating-File-Appender to Handler in AS7
     *
     * @param appender object representing Size-Rotating-File-Appender
     * @param ctx      migration context
     * @return migrated Size-Rotating-File-Handler object
     */
    public static SizeRotFileHandlerBean createSizeRotFileHandler(AppenderBean appender, MigrationContext ctx) {
        SizeRotFileHandlerBean handler = new SizeRotFileHandlerBean();
        handler.setName(appender.getAppenderName());

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

                RollbackData rollbackData = new RollbackData();
                rollbackData.setName(StringUtils.substringAfterLast(value, "/"));
                rollbackData.setType("log");
                ctx.getRollbackDatas().add(rollbackData);
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

        handler.setFormatter(appender.getLayoutParamValue());

        return handler;
    }

    /**
     * Method for migrating Async-Appender to Handler in AS7
     *
     * @param appender object representing Async-Appender
     * @return migrated Async-Handler object
     */
    public static AsyncHandlerBean createAsyncHandler(AppenderBean appender) {
        AsyncHandlerBean handler = new AsyncHandlerBean();
        handler.setName(appender.getAppenderName());
        for (ParameterBean parameter : appender.getParameters()) {
            if (parameter.getParamName().equalsIgnoreCase("BufferSize")) {
                handler.setQueueLength(parameter.getParamValue());
                continue;
            }

            if (parameter.getParamName().equalsIgnoreCase("Blocking")) {
                handler.setOverflowAction(parameter.getParamValue());
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
     * Method for migrating Console-Appender to Handler in AS7
     *
     * @param appender object representing Console-Appender
     * @return migrated Console-Handler object
     */
    public static ConsoleHandlerBean createConsoleHandler(AppenderBean appender) {
        ConsoleHandlerBean handler = new ConsoleHandlerBean();
        handler.setName(appender.getAppenderName());

        for (ParameterBean parameter : appender.getParameters()) {
            if (parameter.getParamName().equalsIgnoreCase("Target")) {
                handler.setTarget(parameter.getParamValue());
                continue;
            }

            if (parameter.getParamName().equalsIgnoreCase("Threshold")) {
                handler.setLevel(parameter.getParamValue());
            }
        }

        handler.setFormatter(appender.getLayoutParamValue());

        return handler;
    }

    /**
     * Method for migrating Custom-Appender to Handler in AS7
     *
     * @param appender object representing Custom-Appender
     * @return migrated Custom-Handler object
     */
    public static CustomHandlerBean createCustomHandler(AppenderBean appender) {
        CustomHandlerBean handler = new CustomHandlerBean();
        handler.setName(appender.getAppenderName());
        handler.setClassValue(appender.getAppenderClass());
        Set<PropertyBean> properties = new HashSet();

        // TODO: Required attr is module. So probably something need to be copied and set.

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

        handler.setProperties(properties);
        handler.setFormatter(appender.getLayoutParamValue());

        return handler;
    }

    /**
     * Not implemented yet. Not sure if it is necesarry..
     * Method for migrating File-Appender to Handler in AS7
     *
     * @param appender object representing Periodic-Rotating-File-Appender
     * @return migrated File-Handler object
     */
    public static FileHandlerBean createFileHandler(AppenderBean appender) {
        return null;
    }

    /**
     * Creating CLI script for adding Logger
     *
     * @param logger object of Logger
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createLoggerScript(LoggerBean logger) throws CliScriptException {
        String errMsg = " in logger(Category in AS5) must be set.";
        Utils.throwIfBlank(logger.getLoggerCategory(), errMsg, "Logger name");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=logging/logger=" + logger.getLoggerCategory() + ":add(");

        builder.addProperty("level", logger.getLoggerLevelName());
        builder.addProperty("use-parent-handlers", logger.getUseParentHandlers());

        resultScript.append(builder.asString());

        if (logger.getHandlers() != null) {
            StringBuilder handlersBuilder = new StringBuilder();

            for (String handler : logger.getHandlers()) {
                handlersBuilder.append(",\"").append(handler).append("\"");
            }

            String handlers = handlersBuilder.toString();

            if (!handlers.isEmpty()) {
                handlers = handlers.replaceFirst(",", "");
                resultScript.append(", handlers=[").append(handlers).append("])");
            } else {
                resultScript.append(")");
            }
        } else {
            resultScript.append(")");
        }

        return resultScript.toString();
    }

    /**
     * Creating CLI script for adding Periodic-Rotating-File-Handler
     *
     * @param periodic object of Periodic-Rotating-File-Handler
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createPerHandlerScript(PerRotFileHandlerBean periodic)
            throws CliScriptException {
        String errMsg = " in periodic-rotating-file-handler(Appender in AS5) must be set.";
        Utils.throwIfBlank(periodic.getName(), errMsg, "Name");
        Utils.throwIfBlank(periodic.getRelativeTo(), errMsg, "Relative-to");
        Utils.throwIfBlank(periodic.getPath(), errMsg, "Path");
        Utils.throwIfBlank(periodic.getSuffix(), errMsg, "Suffix");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=logging/periodic-rotating-file-handler=");

        resultScript.append(periodic.getName()).append(":add(");
        resultScript.append("file={\"relative-to\"=>\"").append(periodic.getRelativeTo()).append("\"");
        resultScript.append(", \"path\"=>\"").append(periodic.getPath()).append("\"}");
        resultScript.append(", suffix=").append(periodic.getSuffix()).append(", ");

        builder.addProperty("level", periodic.getLevel());
        builder.addProperty("formatter", periodic.getFormatter());
        builder.addProperty("autoflush", periodic.getAutoflush());
        builder.addProperty("append", periodic.getAppend());

        resultScript.append(builder.asString()).append(")");

        return resultScript.toString();
    }

    /**
     * Creating CLI script for adding Size-Rotating-File-Handler
     *
     * @param sizeHandler object of Size-Rotating-File-Handler
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createSizeHandlerScript(SizeRotFileHandlerBean sizeHandler)
            throws CliScriptException {
        String errMsg = " in size-rotating-file-handler(Appender in AS5) must be set.";
        Utils.throwIfBlank(sizeHandler.getName(), errMsg, "Name");
        Utils.throwIfBlank(sizeHandler.getRelativeTo(), errMsg, "Relative-to");
        Utils.throwIfBlank(sizeHandler.getPath(), errMsg, "Path");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=logging/size-rotating-file-handler=");

        resultScript.append(sizeHandler.getName()).append(":add(");
        resultScript.append("file={\"").append(sizeHandler.getRelativeTo()).append("\"=>\"").append(sizeHandler.getPath()).append("\"}");

        builder.addProperty("level", sizeHandler.getLevel());
        builder.addProperty("filter", sizeHandler.getFilter());
        builder.addProperty("formatter", sizeHandler.getFormatter());
        builder.addProperty("autoflush", sizeHandler.getAutoflush());
        builder.addProperty("append", sizeHandler.getAppend());
        builder.addProperty("rotate-size", sizeHandler.getRotateSize());
        builder.addProperty("max-backup-index", sizeHandler.getMaxBackupIndex());

        resultScript.append(builder.asString()).append(")");

        return resultScript.toString();
    }

    /**
     * Creating CLI script for adding Async-Handler
     *
     * @param asyncHandler object of Async-Handler
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createAsyncHandlerScript(AsyncHandlerBean asyncHandler)
            throws CliScriptException {
        String errMsg = " in async-handler(Appender in AS5) must be set.";
        Utils.throwIfBlank(asyncHandler.getName(), errMsg, "Name");
        Utils.throwIfBlank(asyncHandler.getQueueLength(), errMsg, "Queue length");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=logging/async-handler=");

        resultScript.append(asyncHandler.getName()).append(":add(");
        resultScript.append("queue-length=").append(asyncHandler.getQueueLength());

        builder.addProperty("level", asyncHandler.getLevel());
        builder.addProperty("filter", asyncHandler.getFilter());
        builder.addProperty("formatter", asyncHandler.getFormatter());
        builder.addProperty("overflow-action", asyncHandler.getOverflowAction());

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
     * Creating CLI script for adding Console-Handler
     *
     * @param consoleHandler object of Console-Handler
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createConsoleHandlerScript(ConsoleHandlerBean consoleHandler)
            throws CliScriptException {
        String errMsg = " in console-handler(Appender in AS5) must be set.";
        Utils.throwIfBlank(consoleHandler.getName(), errMsg, "Name");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=logging/console-handler=");

        resultScript.append(consoleHandler.getName()).append(":add(");

        builder.addProperty("level", consoleHandler.getLevel());
        builder.addProperty("filter", consoleHandler.getFilter());
        builder.addProperty("formatter", consoleHandler.getFormatter());
        builder.addProperty("autoflush", consoleHandler.getAutoflush());
        builder.addProperty("target", consoleHandler.getTarget());

        resultScript.append(builder.asString()).append(")");

        return resultScript.toString();
    }

    /**
     * Creating CLI script for adding Custom-Handler
     *
     * @param customHandler object ofCustom-Handler
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createCustomHandlerScript(CustomHandlerBean customHandler)
            throws CliScriptException {
        String errMsg = " in custom-handler must be set.";
        Utils.throwIfBlank(customHandler.getName(), errMsg, "Name");
        Utils.throwIfBlank(customHandler.getModule(), errMsg, "Module");
        Utils.throwIfBlank(customHandler.getClassValue(), errMsg, "Class-value");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=logging/custom-handler=");

        resultScript.append(customHandler.getName()).append(":add(");

        builder.addProperty("level", customHandler.getLevel());
        builder.addProperty("filter", customHandler.getFilter());
        builder.addProperty("formatter", customHandler.getFormatter());
        builder.addProperty("class", customHandler.getClassValue());
        builder.addProperty("module", customHandler.getModule());

        resultScript.append(builder.asString());

        if (customHandler.getProperties() != null) {
            StringBuilder propertiesBuilder = new StringBuilder();
            for (PropertyBean property : customHandler.getProperties()) {
                propertiesBuilder.append(", \"").append(property.getName()).append("\"=>");
                propertiesBuilder.append("\"").append(property.getValue()).append("\"");
            }

            String properties = propertiesBuilder.toString();

            if (!properties.isEmpty()) {
                properties = properties.replaceFirst(", ", "");
                resultScript.append(", properties={").append(properties).append("}");
            }
        }

        resultScript.append(")");

        return resultScript.toString();
    }

    /**
     *  Method for creating Cli script for root-logger. Root-logger can be only modified with Cli not added. So this method
     *  creates script for change level, filter, handlers. Required attribute is level.
     *  (Changes possible in future)
     *
     * @param rootLogger object representing root-logger in AS7
     * @return  String containing scripts
     * @throws CliScriptException if attribute level is missing.
     */
    public static String createRootLoggerScript(RootLoggerAS7Bean rootLogger) throws CliScriptException{
        String errMsg = " in root-logger must be set.";
        Utils.throwIfBlank(rootLogger.getRootLoggerLevel(), errMsg, "Level");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        StringBuilder resultScript = new StringBuilder();
        if(rootLogger.getRootLogFilValue() != null){
            resultScript.append("/subsystem=logging/root-logger=ROOT:write-attribute(");
            builder.addProperty("filter", rootLogger.getRootLogFilValue());
            resultScript.append(builder.asString()).append(")\n");
        }

        resultScript.append("/subsystem=logging/root-logger=ROOT:write-attribute(");
        builder.addProperty("level", rootLogger.getRootLoggerLevel());
        resultScript.append(builder.asString()).append(")\n");

        for(String handler : rootLogger.getRootLoggerHandlers()){
            resultScript.append("/subsystem=logging/root-logger=ROOT:root-logger-assign-handler(name=");
            resultScript.append(handler).append(")\n");
        }

        return resultScript.toString();
    }

}

