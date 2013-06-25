/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.logging;

import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang.StringUtils;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.loom.actions.CliCommandAction;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.actions.ModuleCreationAction;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.Configuration.IfExists;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ctx.MigratorData;
import org.jboss.loom.ex.CliScriptException;
import org.jboss.loom.ex.LoadMigrationException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.migrators.logging.jaxb.*;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;
import org.jboss.loom.utils.Utils;
import org.jboss.loom.utils.UtilsAS5;
import org.jboss.loom.utils.as7.AS7CliUtils;
import org.jboss.loom.utils.as7.CliAddScriptBuilder;
import org.jboss.loom.utils.as7.CliApiCommandBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Migrator of logging subsystem implementing IMigrator.
 * 
    <appender name="CLUSTER" class="org.jboss.logging.appender.RollingFileAppender">
        <errorHandler class="org.jboss.logging.util.OnlyOnceErrorHandler"/>
        <param name="File" value="${jboss.server.log.dir}/cluster.log"/>
        <param name="Append" value="false"/>
        <param name="MaxFileSize" value="500KB"/>
        <param name="MaxBackupIndex" value="1"/>

        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%d %-5p [%c] %m%n"/>
        </layout>
    </appender>
    <category name="org.jgroups">
        <priority value="WARN" />
        <appender-ref ref="CLUSTER"/>
    </category>
    <category name="org.jboss.ha">
        <priority value="INFO" />
        <appender-ref ref="CLUSTER"/>
    </category>
 
 * 
 * Conf docs: https://docs.jboss.org/author/display/AS72/Logging+Configuration
 *
 * @author Roman Jakubco
 */
@ConfigPartDescriptor(
    name = "JBoss Logging configuration", 
    docLink = "https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/5/html-single/Administration_And_Configuration_Guide/index.html#idm2915376"
)
public class LoggingMigrator extends AbstractMigrator {
    private static final Logger log = LoggerFactory.getLogger(LoggingMigrator.class);
    
    private final static String CLI_PROP__LOG_DIR      = "jboss.server.log.dir";
    private final static String AS5_PROP__LOG_TRESHOLD = "jboss.server.log.threshold";
    private final static String DEFAULT_QUEUE_LENGTH = "50";

    
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
    
    
    

    public LoggingMigrator(GlobalConfiguration globalConfig) {
        super(globalConfig);
    }

    @Override
    public void loadSourceServerConfig(MigrationContext ctx) throws LoadMigrationException {
        try {
            File log4jConfFile = Utils.createPath(
                    //super.getGlobalConfig().getAS5Config().getDir(),  "server",
                    //super.getGlobalConfig().getAS5Config().getProfileName(),
                    //"conf", "jboss-log4j.xml");
                    super.getGlobalConfig().getAS5Config().getConfDir(), "jboss-log4j.xml");

            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(log4jConfFile));

            //if( ! log4jConfFile.canRead())
            //    throw new LoadMigrationException("Cannot find/open file: " + log4jConfFile.getAbsolutePath());
            
            Unmarshaller unmarshaller = JAXBContext.newInstance(LoggingAS5Bean.class).createUnmarshaller();
            LoggingAS5Bean loggingAS5 = (LoggingAS5Bean) unmarshaller.unmarshal(xsr);

            MigratorData mData = new MigratorData();

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
    
    
    
    /**
     *  TODO: customHandlers are transformed at the end into actions.
     *        Use a map and action dependencies instead.
     */
    @Override
    public void createActions(MigrationContext ctx) throws MigrationException {
        
        List<AppenderBean>      appenders   = new LinkedList();
        List<CategoryBean>      categs      = new LinkedList();
        List<RootLoggerAS5Bean> rootLoggers = new LinkedList();
        
        // Sort out the fragments.
        for( IConfigFragment fragment : ctx.getMigrationData().get(LoggingMigrator.class).getConfigFragments() ){
                 if( fragment instanceof AppenderBean )      appenders.  add( (AppenderBean) fragment);
            else if( fragment instanceof CategoryBean )      categs.     add( (CategoryBean) fragment);
            else if( fragment instanceof RootLoggerAS5Bean ) rootLoggers.add( (RootLoggerAS5Bean) fragment);
            else throw new MigrationException("Config fragment unrecognized by " + this.getClass().getSimpleName() + ": " + fragment );
        }
        
        // For categories with appender-ref - use Action dependencies.
        Map<String, IMigrationAction> appenderNamesToActions = new HashMap();

        // Prevent duplicate categories.
        Map<CategoryBean, IMigrationAction> categoryToAction = new HashMap();

        // Appenders.
        HashMap<File, String> tempModules = new HashMap();
        for( AppenderBean appender : appenders) {
            List<? extends IMigrationAction> actions = createAppenderAction( appender, tempModules );
            for( IMigrationAction action : actions ) {
                ctx.getActions().add( action );
                appenderNamesToActions.put( appender.getAppenderName(), action );
            }
        }

        // Categories
        IfExists loggerIfExists = parseIfExistsParam("logger."+IfExists.PARAM_NAME, IfExists.OVERWRITE);
        for( CategoryBean catBean : categs ) {

            // Skip those which already exist.
            if( categoryToAction.containsKey( catBean ) ){
                categoryToAction.get( catBean ).getWarnings().add("Duplicate category found: " + catBean);
                continue;
            }

            try {
                LoggerBean categoryBean = migrateCategory( catBean );
                CliCommandAction action = createLoggerCliAction( categoryBean, loggerIfExists );

                // category/appender-ref/@ref
                for( String appenRef : catBean.getAppenderRefs() ) {
                    IMigrationAction appenAction = appenderNamesToActions.get( appenRef );
                    if( null == appenAction ){
                        action.addWarning("Unknown appender referenced in category " + catBean.getCategoryName() + ": " + appenRef);
                        continue;
                    }
                    action.addDependency( appenAction );
                }
                
                ctx.getActions().add( action );
                categoryToAction.put( catBean, action ); // Prevent duplicates.
            }
            catch( CliScriptException ex ) {
                throw new MigrationException("Migration of the Category failed: " + ex.getMessage(), ex);
            }
        }

        // Root logger
        for( RootLoggerAS5Bean rootLogger : rootLoggers) {
            List<CliCommandAction> actions = createRootLoggerCliAction( migrateRootLogger(rootLogger) );
            
            // appender-ref/@ref
            for( String appenRef : rootLogger.getRootAppenderRefs() ) {
                IMigrationAction appenAction = appenderNamesToActions.get( appenRef );
                if( null == appenAction ){
                    actions.get(0).addWarning("Unknown appender referenced in root logger: " + appenRef);
                    continue;
                }
                actions.get(0).addDependency( appenAction );
            }
            ctx.getActions().addAll( actions );           
        }
        
    }// createActions()

    
    
    
    /**
     * Creates Custom-Handler CliCommandAction along with ModuleCreationAction if needed
     *
     * @param handler Custom-Handler with custom class, which must be deployed into AS7
     * @param tempModules Map containing names of the jar files and their created modules, which were already migrated
     * @return  list containing CliCommandAction for adding Custom-Handler and ModuleCreationAction for adding module if
     *          needed
     * @throws MigrationException if class cannot be found in jars in AS5 structure
     */
    private List<IMigrationAction> createCustomHandlerActions(CustomHandlerBean handler, HashMap<File, String> tempModules)
            throws MigrationException {
        
        File fileJar;
        try {
            fileJar = UtilsAS5.findJarFileWithClass(handler.getClassValue(), getGlobalConfig().getAS5Config().getDir(),
                    getGlobalConfig().getAS5Config().getProfileName());
        } catch (IOException ex) {
            throw new MigrationException("Failed finding jar with class " + handler.getClassValue() + ": " + ex.getMessage(), ex);
        }

        List<IMigrationAction> actions = new LinkedList();

        if (tempModules.containsKey(fileJar)) {
            // ModuleCreationAction is already set. No need for another one => just create CLI for CustomHandler
            try {
                handler.setModule( tempModules.get( fileJar ) );
                actions.add( createCustomHandlerCliAction( handler ) );
            }
            catch( CliScriptException ex ) {
                throw new MigrationException("Failed creating a CLI command for appeneder " + handler.getName() + ": " + ex.getMessage(), ex);
            }

            return actions;
        }

        
        // Handler jar is new => create ModuleCreationAction, new module and CLI script
        try {
            String moduleName = "logging.customHandler" + number;
            number++;
            handler.setModule( moduleName );
            tempModules.put( fileJar, moduleName );

            actions.add( createCustomHandlerCliAction( handler ) );

            String[] deps = new String[]{"javax.api", "org.jboss.logging", null, "org.apache.log4j"};

            ModuleCreationAction moduleAction = new ModuleCreationAction(
                    this.getClass(), moduleName, deps, fileJar, 
                    this.parseIfExistsParam("logger." + IfExists.PARAM_NAME, IfExists.OVERWRITE));
            actions.add(moduleAction);
        }
        catch( CliScriptException e ) {
            throw new MigrationException("Migration of the appeneder " + handler.getName() + " failed (CLI command): " + e.getMessage(), e);
        }

        return actions;
    }

    

    /**
     *  Processes AppenderBean. Adds actions to context!
     *  TODO: Refactor to return the action.
     */
    private List<? extends IMigrationAction> createAppenderAction( AppenderBean appenderBean, HashMap<File, String> tempModules ) throws MigrationException {
        
        // Selection of classes which are stored in log4j or jboss logging jars.
        String cls = appenderBean.getAppenderClass();
        if( ! (cls.startsWith("org.apache.log4j") || cls.startsWith("org.jboss.logging.appender")) ){
            // Selection of classes which are created by the user
            // In situation that the user creates own class with same name as classes in log4j or jboss logging => CustomHandler
            // Module for these handlers must be set with creation of ModuleCreationAction
            CustomHandlerBean handler = createCustomHandler(appenderBean, true);
            return createCustomHandlerActions(handler, tempModules);
        }
            

        try {
            String appenderType = StringUtils.substringAfterLast(cls, ".");
            CliCommandAction action;

            switch( appenderType ) {
                case "DailyRollingFileAppender":{
                    PerRotFileHandlerBean handler = createPerRotFileHandler(appenderBean);
                    action = createPerRotHandlerCliAction(handler);
                } break;
                case "RollingFileAppender":{
                    SizeRotFileHandlerBean handler = createSizeRotFileHandler(appenderBean);
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

            return Collections.singletonList( action );
        }
        catch (CliScriptException e) {
            throw new MigrationException("Migration of the appender " + appenderBean.getAppenderName() + " failed: " + e.getMessage(), e);
        }

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
        logger.setHandlers(category.getAppenderRefs());

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
    static PerRotFileHandlerBean createPerRotFileHandler(AppenderBean appender) {
        
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
                    handler.setPath( new File( value ).getName() );
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
    static SizeRotFileHandlerBean createSizeRotFileHandler(AppenderBean appender) {
        
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
                    handler.setRelativeTo(CLI_PROP__LOG_DIR);
                    handler.setPath( new File(value).getName() );
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
    static CustomHandlerBean createCustomHandler( AppenderBean appender, boolean custom ) {
        
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
     * Migrates File-Appender to Handler in AS7
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
    static CliCommandAction createLoggerCliAction( LoggerBean logger, IfExists ifExists) throws CliScriptException {
        String errMsg = " in logger(Category in AS5) must be set.";
        Utils.throwIfBlank(logger.getLoggerCategory(), errMsg, "Logger name");
        
        // First, check if it exists. If so, delete first.
        switch( ifExists ){
            case OVERWRITE:
            case MERGE:
            /* TODO: Actually, this is handled by the CliCommandAction itself - setIfExists().
             *       Check why it doesn't work.
            // TODO: MIGR-61 Merge resources instead of skipping or replacing
            try {
                //log.debug("Removing resource if exists: " + loggerCmd);
                //AS7CliUtils.removeResourceIfExists( loggerCmd, ctx.getAS7Client() );
                if( AS7CliUtils.exists( loggerCmd, ctx.getAS7Client() ) ){
                    new CliCommandAction( LoggingMigrator.class, AS7CliUtils.formatCommand( loggerCmd ), loggerCmd );
                }
            } catch( CliBatchException | IOException ex ) {
                throw new CliScriptException("Failed removing resource '"+AS7CliUtils.formatCommand( loggerCmd )+"': " + ex.getMessage(), ex );
            }/**/
        }
        
        return new CliCommandAction( LoggingMigrator.class, createLoggerScript(logger), createLoggerCommand(logger, ifExists))
                .setIfExists( ifExists );
    }
    
    private static ModelNode createLoggerCommand( LoggerBean logger, IfExists ifExists ){
        // ModelNode
        ModelNode loggerCmd = new ModelNode();
        loggerCmd.get(ClientConstants.OP_ADDR).add("subsystem","logging");
        loggerCmd.get(ClientConstants.OP_ADDR).add("logger", logger.getLoggerCategory());
        
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
        builder.addPropertyIfSet("level", logger.getLoggerLevelName());
        builder.addPropertyIfSet("use-parent-handlers", logger.getUseParentHandlers());
        
        return builder.getCommand();
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
        String errMsg = " in periodic-rotating-file-handler (~Appender in source server) must be set.";
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
        builder.addPropertyIfSet("suffix", handler.getSuffix());
        builder.addPropertyIfSet("level", handler.getLevel());
        builder.addPropertyIfSet("formatter", handler.getFormatter());
        builder.addPropertyIfSet("autoflush", handler.getAutoflush());
        builder.addPropertyIfSet("append", handler.getAppend());
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
        String errMsg = " in size-rotating-file-handler (~Appender in source server) must be set.";
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
        builder.addPropertyIfSet("level", handler.getLevel());
        builder.addPropertyIfSet("filter", handler.getFilter());
        builder.addPropertyIfSet("formatter", handler.getFormatter());
        builder.addPropertyIfSet("autoflush", handler.getAutoflush());
        builder.addPropertyIfSet("append", handler.getAppend());
        String size = handler.getRotateSize();
        if( size.endsWith("KB") )  size = StringUtils.replace(size, "KB", "K");
        if( size.endsWith("MB") )  size = StringUtils.replace(size, "MB", "M");
        if( size.endsWith("GB") )  size = StringUtils.replace(size, "GB", "G");
        builder.addPropertyIfSet("rotate-size", size);
        builder.addPropertyIfSet("max-backup-index", handler.getMaxBackupIndex());
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
     * 
     * Example from EAP 5 production:
     * 
        <!-- Buffer events and log them asynchronously -->
        <appender name="ASYNC" class="org.apache.log4j.AsyncAppender">
          <errorHandler class="org.jboss.logging.util.OnlyOnceErrorHandler"/>
          <appender-ref ref="FILE"/>
          <!--
          <appender-ref ref="CONSOLE"/>
          <appender-ref ref="SMTP"/>
          -->
        </appender>
     */
    static CliCommandAction createAsyncHandleCliAction(AsyncHandlerBean handler) throws CliScriptException, MigrationException{
        String errMsg = " in async-handler (AsyncAppender) must be set.";
        Utils.throwIfBlank(handler.getName(), errMsg, "Name");
        //Utils.throwIfBlank(handler.getQueueLength(), errMsg, "Queue length"); // It doesn't have to, in AS 5.

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

        builder.addPropertyIfSet("queue-length", handler.getQueueLength(), DEFAULT_QUEUE_LENGTH);
        builder.addPropertyIfSet("level", handler.getLevel());
        builder.addPropertyIfSet("filter", handler.getFilter());
        builder.addPropertyIfSet("formatter", handler.getFormatter());
        builder.addPropertyIfSet("overflow-action", handler.getOverflowAction());
        // TODO:
        //builder.setPropsFromObject(handler, "level filter formatter overflow-action");

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
        String errMsg = " in console-handler (~Appender in source server) must be set.";
        Utils.throwIfBlank(handler.getName(), errMsg, "Name");

        ModelNode handlerCmd = new ModelNode();
        handlerCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        handlerCmd.get(ClientConstants.OP_ADDR).add("subsystem", "logging");
        handlerCmd.get(ClientConstants.OP_ADDR).add("console-handler", handler.getName());

        CliApiCommandBuilder builder = new CliApiCommandBuilder(handlerCmd);
        builder.addPropertyIfSet("level", handler.getLevel());
        builder.addPropertyIfSet("filter", handler.getFilter());
        builder.addPropertyIfSet("formatter", handler.getFormatter());
        builder.addPropertyIfSet("autoflush", handler.getAutoflush());
        builder.addPropertyIfSet("target", handler.getTarget());
        
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

        return new CliCommandAction( LoggingMigrator.class, createCustomHandlerScript(handler), createCustomHandlerModelNode(handler) );
    }
    
    static ModelNode createCustomHandlerModelNode(CustomHandlerBean handler) throws CliScriptException {
        
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
        builder.addPropertyIfSet("level", handler.getLevel());
        builder.addPropertyIfSet("filter", handler.getFilter());
        builder.addPropertyIfSet("formatter", handler.getFormatter());
        builder.addPropertyIfSet("class", handler.getClassValue());
        builder.addPropertyIfSet("module", handler.getModule());
        // TODO: AS7CliUtils.copyProperties(handler, builder, "level filter formatter class module");

        return builder.getCommand();
    }

    
    /**
     * Creates a list of CliCommandActions for modifying a root-logger
     *
     * @param root object representing root-logger
     * @return list of created CliCommandActions
     * @throws CliScriptException
     */
      List<CliCommandAction> createRootLoggerCliAction(RootLoggerAS7Bean root) throws CliScriptException{
        List<CliCommandAction> actions = new ArrayList();
        if(root.getRootLoggerLevel() != null){
            String level ="/subsystem=logging/root-logger=ROOT:write-attribute(name=level, value=" + root.getRootLoggerLevel() + ")";

            ModelNode levelNode = new ModelNode();
            levelNode.get(ClientConstants.OP).set(ClientConstants.WRITE_ATTRIBUTE_OPERATION);
            levelNode.get(ClientConstants.OP_ADDR).add("subsystem","logging");
            levelNode.get(ClientConstants.OP_ADDR).add("root-logger", "ROOT");
            levelNode.get("name").set("level");
            levelNode.get("value").set(root.getRootLoggerLevel());

            actions.add(new CliCommandAction(this.getClass(), level, levelNode));
        }

        if( (root.getRootLoggerHandlers() != null) || !(root.getRootLoggerHandlers().isEmpty())){
            StringBuilder handlerTemp = new StringBuilder();
            for(String handler : root.getRootLoggerHandlers()){
                handlerTemp.append('"').append(handler).append('"');
            }

            String temp = handlerTemp.toString();
            String handlers ="/subsystem=logging/root-logger=ROOT:write-attribute(name=handlers, value=[" +
                    StringUtils.substringBeforeLast(temp, ",") + "])";

            ModelNode handlersNode = new ModelNode();
            handlersNode.get(ClientConstants.OP).set(ClientConstants.WRITE_ATTRIBUTE_OPERATION);
            handlersNode.get(ClientConstants.OP_ADDR).add("subsystem","logging");
            handlersNode.get(ClientConstants.OP_ADDR).add("root-logger", "ROOT");
            handlersNode.get("name").set("handlers");

            ModelNode list = new ModelNode();
            for(String test : root.getRootLoggerHandlers()){
                list.add(test);
            }
            handlersNode.get("value").set(list);

            actions.add(new CliCommandAction(this.getClass(), handlers, handlersNode));

        }
        return actions;
    }
    
    
    
    
    /*  ============= Script stuff - TODO: Get rid of it. Generate from ModelNode. ============== */
    
    
      
    /**
     * Creates a CLI script for adding a Logger
     *
     * @param logger object of Logger
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     * @deprecated  Generate this out of ModelNode.
     */
    static String createLoggerScript(LoggerBean logger) throws CliScriptException {
        String errMsg = " in logger (Category in source server) must be set.";
        Utils.throwIfBlank(logger.getLoggerCategory(), errMsg, "Logger name");

        StringBuilder resultScript = new StringBuilder("/subsystem=logging/logger=" + logger.getLoggerCategory() + ":add(");
        
        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        builder.addProperty("level", logger.getLoggerLevelName());
        builder.addProperty("use-parent-handlers", logger.getUseParentHandlers());
        resultScript.append(builder.formatAndClearProps());

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
     * @deprecated  Generate this out of ModelNode.
     */
    static String createPerHandlerScript(PerRotFileHandlerBean periodic)
            throws CliScriptException {
        String errMsg = " in periodic-rotating-file-handler (~Appender in source server) must be set.";
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

        resultScript.append(builder.formatAndClearProps()).append(")");

        return resultScript.toString();
    }
    

    /**
     * Creates a CLI script for adding a Size-Rotating-File-Handler
     *
     * @param sizeHandler object of Size-Rotating-File-Handler
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     * @deprecated  Generate this out of ModelNode.
     */
    static String createSizeHandlerScript(SizeRotFileHandlerBean sizeHandler)
            throws CliScriptException {
        String errMsg = " in size-rotating-file-handler (~Appender in source server) must be set.";
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

        resultScript.append(builder.formatAndClearProps()).append(")");

        return resultScript.toString();
    }
    

    /**
     * Creates a CLI script for adding a Async-Handler
     *
     * @param asyncHandler object of Async-Handler
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     * @deprecated  Generate this out of ModelNode.
     */
    static String createAsyncHandlerScript(AsyncHandlerBean asyncHandler)
            throws CliScriptException {
        String errMsg = " in async-handler (AsyncAppender in AS5) must be set.";
        Utils.throwIfBlank(asyncHandler.getName(), errMsg, "Name");
        //Utils.throwIfBlank(asyncHandler.getQueueLength(), errMsg, "Queue length"); // It doesn't have to, in AS 5.

        StringBuilder resultScript = new StringBuilder("/subsystem=logging/async-handler=");
        resultScript.append(asyncHandler.getName()).append(":add(");
        //resultScript.append("queue-length=").append( String.defaultIfNull( asyncHandler.getQueueLength(), 100) );

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        builder.addProperty("queue-length", StringUtils.defaultIfBlank( asyncHandler.getQueueLength(), DEFAULT_QUEUE_LENGTH) );
        builder.addProperty("level", asyncHandler.getLevel());
        builder.addProperty("filter", asyncHandler.getFilter());
        builder.addProperty("formatter", asyncHandler.getFormatter());
        builder.addProperty("overflow-action", asyncHandler.getOverflowAction());
        // TODO: AS7CliUtils.copyProperties(handler, builder, "level filter formatter ...");

        resultScript.append(builder.formatAndClearProps());

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
     * @deprecated  Generate this out of ModelNode.
     */
    static String createConsoleHandlerScript(ConsoleHandlerBean consoleHandler)
            throws CliScriptException {
        String errMsg = " in console-handler (~Appender in source server) must be set.";
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

        resultScript.append(builder.formatAndClearProps()).append(")");

        return resultScript.toString();
    }

    /**
     * Creates a CLI script for adding a Custom-Handler
     *
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     * @deprecated  Generate this out of ModelNode.
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

        resultScript.append(builder.formatAndClearProps());

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
