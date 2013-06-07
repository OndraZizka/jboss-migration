/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom;

import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.actions.CliCommandAction;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.conf.AS7Config;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.ActionException;
import org.jboss.loom.ex.CliBatchException;
import org.jboss.loom.ex.InitMigratorsExceptions;
import org.jboss.loom.ex.LoadMigrationException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.connectionFactories.ResAdapterMigrator;
import org.jboss.loom.migrators.dataSources.DatasourceMigrator;
import org.jboss.loom.migrators.logging.LoggingMigrator;
import org.jboss.loom.migrators.security.SecurityMigrator;
import org.jboss.loom.migrators.server.ServerMigrator;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.utils.as7.AS7CliUtils;
import org.jboss.loom.utils.Utils;
import org.jboss.loom.utils.as7.BatchFailure;
import org.jboss.loom.utils.as7.BatchedCommandWithAction;
import org.eclipse.persistence.exceptions.JAXBException;
import org.jboss.as.cli.batch.BatchedCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.*;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.loom.actions.ActionDependencySorter;
import org.jboss.loom.actions.ManualAction;
import org.jboss.loom.actions.review.BeansXmlReview;
import org.jboss.loom.actions.review.IActionReview;
import org.jboss.loom.ctx.DeploymentInfo;
import org.jboss.loom.migrators.classloading.ClassloadingMigrator;
import org.jboss.loom.migrators.ejb3.Ejb3Migrator;
import org.jboss.loom.migrators.mail.MailMigrator;
import org.jboss.loom.migrators.jaxr.JaxrMigrator;
import org.jboss.loom.migrators.messaging.MessagingMigrator;
import org.jboss.loom.migrators.remoting.RemotingMigrator;
import org.jboss.loom.recog.ServerInfo;
import org.jboss.loom.recog.ServerRecognizer;
import org.jboss.loom.tools.report.Reporter;
import org.jboss.loom.utils.XmlUtils;
import org.jboss.loom.utils.compar.FileHashComparer;

/**
 *  Controls the core migration processes.
 * 
 *  TODO: Perhaps leave init() and doMigration() in here 
 *        and separate the other methods to a MigrationService{} ?
 *
 *  @author Roman Jakubco
 */
public class MigrationEngine {
    private static final Logger log = LoggerFactory.getLogger(MigrationEngine.class);
        

    private Configuration config;

    private MigrationContext ctx;

    private List<IMigrator> migrators;
    
    

    public MigrationEngine( Configuration config ) throws InitMigratorsExceptions {
        this.config = config;
        this.init();
        this.resetContext( config );
    }
    
    /**  Creates a brand new fresh clear context. */
    private void resetContext( Configuration config ) {
        this.ctx = new MigrationContext( config );
    }

    
    /**
     *  Initializes this Migrator, especially instantiates the IMigrators.
     */
    private void init() throws InitMigratorsExceptions {
        
        // Find IMigrator implementations.
        List<Class<? extends IMigrator>> migratorClasses = findMigratorClasses();

        // Initialize migrator instances. 
        Map<Class<? extends IMigrator>, IMigrator> migratorsMap = 
                createMigrators( migratorClasses, config.getGlobal() );
        
        this.migrators = new ArrayList(migratorsMap.values());
        
        // For each migrator...
        for( IMigrator mig : this.migrators ){
            
            // Supply some references.
            mig.setGlobalConfig( this.config.getGlobal() );
            
            // Let migrators process module-specific args.
            for( Configuration.ModuleSpecificProperty moduleOption : config.getModuleConfigs() ){
                mig.examineConfigProperty( moduleOption );
            }
        }
        
    }// init()
    
    
    
    /**
     *  Instantiate the plugins.
     */
    private static Map<Class<? extends IMigrator>, IMigrator> createMigrators(
            List<Class<? extends IMigrator>> migratorClasses,
            GlobalConfiguration globalConfig
    ) throws InitMigratorsExceptions {
        
        Map<Class<? extends IMigrator>, IMigrator> migs = new LinkedHashMap();
        List<Exception> exs  = new LinkedList<>();
        
        for( Class<? extends IMigrator> cls : migratorClasses ){
            try {
                //IMigrator mig = cls.newInstance();
                //GlobalConfiguration globalConfig, MultiValueMap config
                Constructor<? extends IMigrator> ctor = cls.getConstructor(GlobalConfiguration.class);
                IMigrator mig = ctor.newInstance(globalConfig);
                migs.put(cls, mig);
            }
            catch( NoSuchMethodException ex ){
                String msg = cls.getName() + " doesn't have constructor ...(GlobalConfiguration globalConfig).";
                log.error( msg );
                exs.add( new MigrationException(msg) );
            }
            catch( InvocationTargetException | InstantiationException | IllegalAccessException ex) {
                log.error("Failed instantiating " + cls.getSimpleName() + ": " + ex.toString());
                log.debug("Stack trace: ", ex);
                exs.add(ex);
            }
        }
        
        if( ! exs.isEmpty() ){
            throw new InitMigratorsExceptions(exs);
        }
        
        return migs;
    }// createMigrators()
    
    
    
    
    /**
     *  Finds the implementations of the IMigrator.
     *  TODO: Implement scanning for classes.
     */
    private static List<Class<? extends IMigrator>> findMigratorClasses() {
        
        LinkedList<Class<? extends IMigrator>> migratorClasses = new LinkedList();
        migratorClasses.add( SecurityMigrator.class );
        migratorClasses.add( ServerMigrator.class );
        migratorClasses.add( DatasourceMigrator.class );
        migratorClasses.add( ResAdapterMigrator.class );
        migratorClasses.add( LoggingMigrator.class );
        //migratorClasses.add( DeploymentScannerMigrator.class );
        migratorClasses.add( ClassloadingMigrator.class );  // Warn-only impl.
        migratorClasses.add( MailMigrator.class );          // Warn-only impl.
        migratorClasses.add( JaxrMigrator.class );          // Warn-only impl.
        migratorClasses.add( RemotingMigrator.class );      // Warn-only impl.
        migratorClasses.add( Ejb3Migrator.class );          // Warn-only impl.
        migratorClasses.add( MessagingMigrator.class );     // Warn-only impl.
        return migratorClasses;
    }
    
    private static List<Class<? extends IActionReview>> findActionReviewers(){
        LinkedList<Class<? extends IActionReview>> reviewers = new LinkedList();
        reviewers.add( BeansXmlReview.class );
        return reviewers;
    }

    
    
    
    
    /**
     *  Performs the migration.
     * 
     *      1) Parse AS 7 config into context.
            2) Let the migrators gather the data into the context.
            3) Let them prepare the actions.
                  An action should include what caused it to be created. IMigrationAction.getOriginMessage()
            ==== From now on, don't use the scanned data, only actions. ===
            4) reviewActions
            5) preValidate
            6) backup
            7) perform
            8) postValidate
            9] rollback
     */
    public void doMigration() throws MigrationException {
        
        log.info("Commencing migration.");
        
        boolean dryRun = config.getGlobal().isDryRun();

        this.resetContext( config );
        
        
        // Recognize version of the source server.
        this.recognizeSourceServer();
        
        

        // Parse AS 7 config. Not needed anymore - we use CLI.
        this.parseAS7Config();
        
        // Unzip the deployments.
        this.unzipDeployments();
        
        
        // MIGR-31 - The new way.
        String message = null;
        try {
            // Load the source server config.
            message = "Failed loading source server config.";
            this.loadASourceServerConfig();

            // Open an AS 7 management client connection.
            openManagementClient();
            
            // Ask all the migrators to create the actions to be performed.
            message = "Failed preparing the migration actions.";
            this.prepareActions();
            message = "Actions review failed.";
            this.reviewActions();
            message = "Migration actions validation failed.";
            this.preValidateActions();
            message = "Failed creating backups for the migration actions.";
            this.backupActions();
            
            rollback:
            try {
                message = "Failed performing the migration actions.";
                this.performActions();

                if( ! dryRun ){
                    message = "Verification of migration actions results failed.";
                    this.postValidateActions();
                }
            }
            finally {
                message = "Cleaning backups of migration actions failed.";
                this.cleanBackupsIfAny();
            }
            
            // Close the AS 7 management client connection.
            closeManagementClient();
            
            // Inform the user about necessary manual actions
            this.announceManualActions();
            
        }
        catch( MigrationException ex ) {
            this.rollbackActionsWhichWerePerformed();
            
            // Build up a description.
            String description = "";
            if( ex instanceof ActionException ){
                IMigrationAction action = ((ActionException)ex).getAction();
                // Header
                description = 
                          "\n    Migration action which caused the failure: "
                        + "  (from " + action.getFromMigrator().getSimpleName() + ")";
                // StackTraceElement
                if( action.getOriginStackTrace() != null )
                    description += "\n\tat " + action.getOriginStackTrace().toString();
                // Description
                description += "\n    " + action.toDescription();
                // Origin message
                if( action.getOriginMessage() != null )
                    description += "\n    Purpose of the action: " + action.getOriginMessage();
            }
            this.ctx.setFinalException( new MigrationException( message
                  + "\n    " + ex.getMessage() 
                  + description, ex ) );
        }
        
        this.createReport();
        
        if( this.ctx.getFinalException() != null)
            throw this.ctx.getFinalException();

    }// migrate()


    
    /**
     *  Ask all the migrators to create the actions to be performed; stores them in the context.
     */
    private void prepareActions() throws MigrationException {
        log.debug("====== prepareActions() ========");
                
        // Call all migrators to create their actions.
        try {
            for (IMigrator mig : this.migrators) {
                log.debug("    Preparing actions with " + mig.getClass().getSimpleName());
                mig.createActions(this.ctx);
            }
        } catch (JAXBException e) {
            throw new MigrationException(e);
        }
        // TODO: Additional logic to filter out duplicated file copying etc.
    }
    

    /*
     *  ------------ Actions methods. ----------------
     */
    
    private void reviewActions() throws MigrationException {
        log.debug("======== reviewActions() ========");
        List<IMigrationAction> actions = ctx.getActions();
        for( Class<? extends IActionReview> arClass : findActionReviewers() ){
            IActionReview ar;
            try {
                ar = arClass.newInstance();
            } catch( InstantiationException | IllegalAccessException ex ) {
                throw new MigrationException("Can't instantiate action reviewer " + arClass.getSimpleName() + ": " + ex, ex);
            }
            ar.setContext(ctx);
            ar.setConfig(config);
            for( IMigrationAction action : actions ) {
                ar.review( action );
            }
        }
    }
    
    
    private void preValidateActions() throws MigrationException {
        log.debug("======== preValidateActions() ========");
        List<IMigrationAction> actions = ctx.getActions();
        for( IMigrationAction action : actions ) {
            action.setMigrationContext(ctx);
            action.preValidate();
        }
    }
    
    private void backupActions() throws MigrationException {
        log.debug("======== backupActions() ========");
        List<IMigrationAction> actions = ctx.getActions();
        for( IMigrationAction action : actions ) {
            action.backup();
        }
    }
    
    /**
     *  Performs the actions.
     *  Should do all the active steps: File manipulation, AS CLI commands etc.
     * @throws MigrationException 
     */
    private void performActions() throws MigrationException {
        log.debug("======== performActions() ========");
        
        boolean dryRun = config.getGlobal().isDryRun();
        if(dryRun)
            log.info("\n** This is a DRY RUN, operations are not really performed, only prepared and listed. **\n");
        String dryPrefix = dryRun ? "(DRY RUN) " : "";
        
        // Clear CLI commands, should there be any.
        ctx.getBatch().clear();
        
        // Sort the actions according to dependencies. MIGR-104
        List<IMigrationAction> actions = ctx.getActions();
        List<IMigrationAction> sorted = ActionDependencySorter.sort( actions );
        
        // Store CLI actions into an ordered list.
        // In perform(), they are just put into a batch. Using this, we can tell which one failed.
        List<CliCommandAction> cliActions = new LinkedList();

        // Perform the actions.
        log.info(dryPrefix + "Performing actions:");
        for( IMigrationAction action : sorted ) {
            if( action instanceof CliCommandAction )
                cliActions.add((CliCommandAction) action);
        
            log.info("    " + action.toDescription());
            action.setMigrationContext(ctx); // Again. To be sure.
            
            // On dry run, CliCommandActions can still be performed as they only add to the batch.
            if( (action instanceof CliCommandAction) ||  ! dryRun )
                action.perform();
        }
        
        /// DEBUG: Dump created CLI operations
        log.debug(dryPrefix + "Management operations in batch:");
        int i = 1;
        for( BatchedCommand command : ctx.getBatch().getCommands() ){
            log.debug("    " + i++ + ": " + command.getCommand());
        }

        // Execution
        log.debug(dryPrefix + "Executing CLI batch:");
        try {
            if( ! dryRun )
                AS7CliUtils.executeRequest( ctx.getBatch().toRequest(), config.getGlobal().getAS7Config() );
        }
        catch( CliBatchException ex ){
            //Integer index = AS7CliUtils.parseFailedOperationIndex( ex.getResponseNode() );
            BatchFailure failure = AS7CliUtils.extractFailedOperationNode( ex.getResponseNode() );
            if( null == failure ){
                log.warn("Unable to parse CLI batch operation index: " + ex.getResponseNode());
                throw new MigrationException("Executing a CLI batch failed: " + ex, ex);
            }
            
            IMigrationAction causeAction;
                    
            // First, try if it's a BatchedCommandWithAction, and get the action if so.
            BatchedCommand cmd = ctx.getBatch().getCommands().get( failure.getIndex() - 1 );
            if( cmd instanceof BatchedCommandWithAction )
                causeAction = ((BatchedCommandWithAction)cmd).getAction();
            // Then shoot blindly into cliActions. May be wrong offset - some actions create multiple CLI commands! TODO.
            else
                causeAction = cliActions.get( failure.getIndex() - 1 );
            
            throw new ActionException( causeAction, "Executing a CLI batch failed: " + failure.getMessage());
        }
        catch( IOException ex ) {
            throw new MigrationException("Executing a CLI batch failed: " + ex, ex);
        }
        
    }// performActions()
    
    
    private void postValidateActions() throws MigrationException {
        log.debug("======== postValidateActions() ========");
        List<IMigrationAction> actions = ctx.getActions();
        for( IMigrationAction action : actions ) {
            action.postValidate();
        }
    }
    
    private void cleanBackupsIfAny() throws MigrationException {
        log.debug("======== cleanBackupsIfAny() ========");
        List<IMigrationAction> actions = ctx.getActions();
        for( IMigrationAction action : actions ) {
            //if( action.isAfterBackup())  // Checked in cleanBackup() itself.
            action.cleanBackup();
        }
    }
    
    private void rollbackActionsWhichWerePerformed() throws MigrationException {
        log.debug("======== rollbackActionsWhichWerePerformed() ========");
        List<IMigrationAction> actions = ctx.getActions();
        for( IMigrationAction action : actions ) {
            //if( action.isAfterPerform()) // Checked in rollback() itself.
            action.rollback();
        }
    }
    
    private void announceManualActions(){
        log.debug("======== announceManualActions() ========");
        boolean bannerShown = false;
        List<IMigrationAction> actions = ctx.getActions();
        for( IMigrationAction action : actions ) {
            if( ! ( action instanceof ManualAction ) )
                continue;
            List<String> warns = ((ManualAction)action).getWarnings();
            for( String warn : warns ) {
                if( ! bannerShown )  bannerShown = showBanner();
                log.warn( warn );
            }
        }
        if( bannerShown ){
            log.warn("\n"
                    + "\n===================================================================="
                    + "\n  End of manual actions."
                    + "\n====================================================================\n");
        }
    }
    
    private boolean showBanner(){
        log.warn("\n"
                + "\n===================================================================="
                + "\n  Some parts of the source server configuration are not supported   "
                + "\n  and need to be done manually. See the messages bellow."
                + "\n====================================================================\n");
        return true;
    }
    
    
    /**
     * Calls all migrators' callback for loading configuration data from the source server.
     *
     * @throws LoadMigrationException
     */
    private void loadASourceServerConfig() throws MigrationException {
        log.debug("======== loadASourceServerConfig() ========");
        try {
            for (IMigrator mig : this.migrators) {
                log.debug("    Scanning with " + mig.getClass().getSimpleName());
                mig.loadSourceServerConfig(this.ctx);
            }
        } catch (JAXBException e) {
            throw new LoadMigrationException(e);
        }
    }
    
    
    /**
     *  Recognize the source server version (and type, in the future).
     */
    private void recognizeSourceServer() throws MigrationException {
        log.debug("======== recognizeSourceServer() ========");
        File serverDir = new File(config.getGlobal().getAS5Config().getDir());
        try {
            // Recognize
            ServerInfo serverInfo = ServerRecognizer.recognize( serverDir );
            log.info("Source server recognized as " + serverInfo.format());
            
            // Compute files hashes
            try {
                serverInfo.compareHashes();
                log.info("Hash comparison against distribution files: " + serverInfo.getHashesComparisonResult().formatStats());
                announceHashComparisonResults( serverInfo, config.getGlobal().isTestRun() );
            } catch( Exception ex ){
                log.error("Failed comparing files hashes for " + serverInfo.format() + ":\n    " + ex.getMessage(), ex);
            }
            
            this.ctx.setSourceServer( serverInfo );
        }
        catch( Exception ex ) {
            throw new MigrationException("Failed recognizing the source server in " + serverDir + ":\n    " + ex.getMessage(), ex);
        }
    }

    // Helper for the above method.
    private static void announceHashComparisonResults( ServerInfo serverInfo, boolean noMissOrEmpty ) {
        Map<Path, FileHashComparer.MatchResult> matches = serverInfo.getHashesComparisonResult().getMatches();
        for( Map.Entry<Path, FileHashComparer.MatchResult> entry : matches.entrySet() ) {
            if( entry.getValue() == FileHashComparer.MatchResult.MATCH )  continue;
            if( entry.getValue() == FileHashComparer.MatchResult.MISSING && noMissOrEmpty )  continue;
            if( entry.getValue() == FileHashComparer.MatchResult.EMPTY   && noMissOrEmpty )  continue;
            log.info("    " + entry.getValue().rightPad() + ": " + entry.getKey());
        }
        if( noMissOrEmpty )  log.info("This is a test run, MISSING and EMPTY files aren't printed.");
    }

    
    
    /**
     *  Unzips the apps specified in config to temp dirs, to be deleted at the end.
     */
    private void unzipDeployments() throws MigrationException {
        Set<String> deplPaths = this.config.getGlobal().getDeploymentsPaths();
        List<DeploymentInfo> depls = new ArrayList( deplPaths.size() );

        for( String path : deplPaths ) {
            
            File deplZip = new File( path );
            if( !deplZip.exists() ){
                log.warn( "Application not found: " + path );
                continue;
            }
            
            DeploymentInfo depl = new DeploymentInfo( path );
            
            // It's a dir - no need to unzip.
            if( deplZip.isDirectory() ){
                depls.add( depl );
                continue;
            }
            
            // It's a file - try to unzip.
            //AppConfigUtils.unzipDeployment( deplZip )
            depl.unzipToTmpDir();
            
            depls.add( depl );
        }
        
        ctx.setDeployments( depls );
    }
        

    // AS 7 management client connection.
    
    private void openManagementClient() throws MigrationException {
        ModelControllerClient as7Client = null;
        AS7Config as7Config = config.getGlobal().getAS7Config();
        try {
            as7Client = ModelControllerClient.Factory.create( as7Config.getHost(), as7Config.getManagementPort() );
        }
        catch( UnknownHostException ex ){
            throw new MigrationException("Unknown AS 7 host: " + as7Config.getHost(), ex);
        }
        ctx.setAS7ManagementClient( as7Client );
    }

    private void closeManagementClient(){
        AS7CliUtils.safeClose( ctx.getAS7Client() );
        ctx.setAS7ManagementClient( null );
    }


    /**
     *  Parses AS 7 config.
     *  @deprecated  Not needed anymore - we use CLI.
     */
    private void parseAS7Config() throws MigrationException {
        File as7configFile = new File( this.config.getGlobal().getAS7Config().getConfigFilePath() );
        try {
            DocumentBuilder db = XmlUtils.createXmlDocumentBuilder();
            Document doc = db.parse(as7configFile);
            ctx.setAS7ConfigXmlDoc(doc);
            
            // TODO: Do backup at file level, instead of parsing and writing back.
            //       And rework it in general. MIGR-23.
            doc = db.parse(as7configFile);
            ctx.setAs7ConfigXmlDocOriginal(doc);
        } 
        catch ( SAXException | IOException ex ) {
            throw new MigrationException("Failed loading AS 7 config from " + as7configFile, ex );
        }
    }


    /**
     *  Creates a migration report.
     *  Can't throw.
     */
    private void createReport() {
        try {
            Reporter.createReport( ctx, new File(config.getGlobal().getReportDir()) );
        }
        catch( Throwable ex ){
            log.error("Failed creating migration report:\n    " + ex.getMessage(), ex);
            
            // Only throw if it's a test run; Only log on normal run.
            if( config.getGlobal().isTestRun() )
                throw new RuntimeException(ex);
        }
    }

}// class
