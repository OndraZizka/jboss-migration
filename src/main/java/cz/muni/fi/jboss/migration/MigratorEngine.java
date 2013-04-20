package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.actions.CliCommandAction;
import cz.muni.fi.jboss.migration.actions.IMigrationAction;
import cz.muni.fi.jboss.migration.conf.AS7Config;
import cz.muni.fi.jboss.migration.conf.Configuration;
import cz.muni.fi.jboss.migration.conf.GlobalConfiguration;
import cz.muni.fi.jboss.migration.ex.ActionException;
import cz.muni.fi.jboss.migration.ex.CliBatchException;
import cz.muni.fi.jboss.migration.ex.InitMigratorsExceptions;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.migrators.connectionFactories.ResAdapterMigrator;
import cz.muni.fi.jboss.migration.migrators.dataSources.DatasourceMigrator;
import cz.muni.fi.jboss.migration.migrators.logging.LoggingMigrator;
import cz.muni.fi.jboss.migration.migrators.security.SecurityMigrator;
import cz.muni.fi.jboss.migration.migrators.server.ServerMigrator;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import cz.muni.fi.jboss.migration.utils.AS7CliUtils;
import cz.muni.fi.jboss.migration.utils.Utils;
import cz.muni.fi.jboss.migration.utils.as7.BatchFailure;
import cz.muni.fi.jboss.migration.utils.as7.BatchedCommandWithAction;
import org.apache.commons.collections.map.MultiValueMap;
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
import java.util.*;
import org.jboss.as.controller.client.ModelControllerClient;


/**
 *  Controls the core migration processes.
 * 
 *  TODO: Perhaps leave init() and doMigration() in here 
 *        and separate the other methods to a MigrationService{} ?
 *
 *  @author Roman Jakubco
 */
public class MigratorEngine {
    
    private static final Logger log = LoggerFactory.getLogger(MigratorEngine.class);
    

    private Configuration config;

    private MigrationContext ctx;

    private List<IMigrator> migrators;
    
    

    public MigratorEngine( Configuration config ) throws InitMigratorsExceptions {
        this.config = config;
        this.init();
        this.resetContext();
    }
    
    private void resetContext() {
        this.ctx = new MigrationContext();
    }

    /**
     *  Initializes this Migrator, especially instantiates the IMigrators.
     */
    private void init() throws InitMigratorsExceptions {
        
        // Find IMigrator implementations.
        List<Class<? extends IMigrator>> migratorClasses = findMigratorClasses();

        // Initialize migrator instances. 
        Map<Class<? extends IMigrator>, IMigrator> migratorsMap = 
                createMigrators( migratorClasses, config.getGlobal(), null);
        
        this.migrators = new ArrayList(migratorsMap.values());
        
        // For each migrator (AKA module, AKA plugin)...
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
            GlobalConfiguration globalConfig,
            MultiValueMap config
    ) throws InitMigratorsExceptions {
        
        Map<Class<? extends IMigrator>, IMigrator> migs = new HashMap<>();
        List<Exception> exs  = new LinkedList<>();
        
        for( Class<? extends IMigrator> cls : migratorClasses ){
            try {
                //IMigrator mig = cls.newInstance();
                //GlobalConfiguration globalConfig, MultiValueMap config
                Constructor<? extends IMigrator> ctor = cls.getConstructor(GlobalConfiguration.class, MultiValueMap.class);
                IMigrator mig = ctor.newInstance(globalConfig, config);
                migs.put(cls, mig);
            }
            catch( NoSuchMethodException ex ){
                String msg = cls.getName() + " doesn't have constructor ...(GlobalConfiguration globalConfig, MultiValueMap config).";
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
        
        return migratorClasses;
    }
    

    
    
    
    
    /**
     *  Performs the migration.
     * 
     *      1) Parse AS 7 config into context.
            2) Let the migrators gather the data into the context.
            3) Let them prepare the actions.
                  An action should include what caused it to be created. IMigrationAction.getOriginMessage()
            ==== From now on, don't use the scanned data, only actions. ===
            So instead of getDOMElements(), getCLICommand and apply()
            will be List<IMigrationAction> prepareActions().
            4) preValidate
            5) backup
            6) perform
            7) postValidate
            8] rollback
     */
    public void doMigration() throws MigrationException {
        
        log.info("Commencing migration.");
        
        this.resetContext();
        
        AS7Config as7Config = config.getGlobal().getAS7Config();
        
        // Parse AS 7 config. MIGR-31 OK
        File as7configFile = new File(as7Config.getConfigFilePath());
        try {
            DocumentBuilder db = Utils.createXmlDocumentBuilder();
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
        
        
        
        
        // MIGR-31 - The new way.
        String message = null;
        try {
            // Load the source server config.
            message = "Failed loading AS 5 config from " + as7configFile;
            this.loadAS5Data();

            // Open an AS 7 management client connection.
            openManagementClient();
            
            // Ask all the migrators to create the actions to be performed.
            message = "Failed preparing the migration actions.";
            this.prepareActions();
            message = "Migration actions validation failed.";
            this.preValidateActions();
            message = "Failed creating backups for the migration actions.";
            this.backupActions();
            message = "Failed performing the migration actions.";
            this.performActions();

            message = "Verification of migration actions results failed.";
            this.postValidateActions();
            
            // Close the AS 7 management client connection.
            closeManagementClient();
        }
        catch( MigrationException ex ) {
            this.rollbackActionsWhichWerePerformed();
            
            // Build up a description.
            String description = "";
            if( ex instanceof ActionException ){
                IMigrationAction action = ((ActionException)ex).getAction();
                description = 
                          "\n    Migration action which caused the failure: "
                        + "  (from " + action.getFromMigrator().getSimpleName() + ")"
                        + "\n    " + action.toDescription();
                if( action.getOriginMessage() != null )
                    description += "\n    Purpose of the action: " + action.getOriginMessage();
            }
            throw new MigrationException( message
                  + "\n    " + ex.getMessage() 
                  + description, ex );
        }
        finally {
            this.cleanBackupsIfAny();
        }

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
     *  Actions methods.
     */
    
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
        
        // Clear CLI commands, should there be any.
        ctx.getBatch().clear();
        
        // Store CLI actions into an ordered list.
        List<CliCommandAction> cliActions = new LinkedList();
        
        // Perform the actions.
        log.info("Performing actions:");
        List<IMigrationAction> actions = ctx.getActions();
        for( IMigrationAction action : actions ) {
            if( action instanceof CliCommandAction )
                cliActions.add((CliCommandAction) action);
            
            log.info("    " + action.toDescription());
            action.setMigrationContext(ctx);
            action.perform();
        }
        
        /// DEBUG: Dump created CLI scripts
        log.debug("CLI scripts in batch:");
        int i = 1;
        for( BatchedCommand command : ctx.getBatch().getCommands() ){
            log.debug("    " + i++ + ": " + command.getCommand());
        }

        // Execution
        log.debug("Executing CLI batch:");
        try {
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
            BatchedCommand cmd = ctx.getBatch().getCommands().get( failure.getIndex() );
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
        List<IMigrationAction> actions = ctx.getActions();
        for( IMigrationAction action : actions ) {
            action.postValidate();
        }
    }
    
    private void cleanBackupsIfAny() throws MigrationException {
        List<IMigrationAction> actions = ctx.getActions();
        for( IMigrationAction action : actions ) {
            //if( action.isAfterBackup())  // Checked in cleanBackup() itself.
            action.cleanBackup();
        }
    }
    
    private void rollbackActionsWhichWerePerformed() throws MigrationException {
        List<IMigrationAction> actions = ctx.getActions();
        for( IMigrationAction action : actions ) {
            //if( action.isAfterPerform()) // Checked in rollback() itself.
            action.rollback();
        }
    }
    
    
    /**
     * Calls all migrators' callback for loading configuration data from the source server.
     *
     * @throws LoadMigrationException
     */
    public void loadAS5Data() throws LoadMigrationException {
        log.debug("======== loadAS5Data() ========");
        try {
            for (IMigrator mig : this.migrators) {
                log.debug("    Scanning with " + mig.getClass().getSimpleName());
                mig.loadAS5Data(this.ctx);
            }
        } catch (JAXBException e) {
            throw new LoadMigrationException(e);
        }
    }


    // AS 7 management client connection.
    
    private void openManagementClient() throws MigrationException {
        ModelControllerClient as7Client = null;
        AS7Config as7Config = config.getGlobal().getAS7Config();
        try {
            as7Client = ModelControllerClient.Factory.create( as7Config.getHost(), as7Config.getManagementPort() );
        }
        catch( UnknownHostException ex ){
            throw new MigrationException("Unknown AS 7 host.", ex);
        }
        ctx.setAS7ManagementClient( as7Client );
    }

    private void closeManagementClient(){
        AS7CliUtils.safeClose( ctx.getAS7Client() );
        ctx.setAS7ManagementClient( null );
    }

}// class
