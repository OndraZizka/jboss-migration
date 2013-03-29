package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.actions.IMigrationAction;
import cz.muni.fi.jboss.migration.ex.InitMigratorsExceptions;
import cz.muni.fi.jboss.migration.conf.Configuration;
import cz.muni.fi.jboss.migration.conf.GlobalConfiguration;
import cz.muni.fi.jboss.migration.ex.*;
import cz.muni.fi.jboss.migration.migrators.connectionFactories.ResAdapterMigrator;
import cz.muni.fi.jboss.migration.migrators.dataSources.DatasourceMigrator;
import cz.muni.fi.jboss.migration.migrators.logging.LoggingMigrator;
import cz.muni.fi.jboss.migration.migrators.security.SecurityMigrator;
import cz.muni.fi.jboss.migration.migrators.server.ServerMigrator;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import cz.muni.fi.jboss.migration.utils.AS7ModuleUtils;
import cz.muni.fi.jboss.migration.utils.RollbackUtils;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.io.FileUtils;
import org.eclipse.persistence.exceptions.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


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
     * TODO:  MIGR-31
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
        
        // Parse AS 7 config. MIGR-31 OK
        File as7configFile = new File(config.getGlobal().getAS7Config().getConfigFilePath());
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
            
            // Ask all the migrators to create the actions to be performed.
            message = "Failed preparing the migration actions.";
            this.prepareActions();
            message = "Actions validation failed.";
            this.preValidateActions();
            message = "Failed creating backups for the migration actions.";
            this.backupActions();
            message = "Failed performing the migration actions.";
            this.performActions();
            message = "Verification of migration actions results failed.";
            this.postValidateActions();
        }
        catch( MigrationException ex ) {
            this.rollbackActionsWhichWerePerformed();
            throw new MigrationException( message, ex );
        }
        finally {
            this.cleanBackupsIfAny();
        }

        //<editor-fold defaultstate="collapsed" desc="Old stuff">
        if( false ) {
            // Currently ignored?  MIGR-31 TODO: Replace with prepareActions()
            try {
                this.getDOMElements(); // ??? Ignores the results?
            }
            catch( MigrationException e ) {
                throw new MigrationException(e);
            }
            
            
            // CLI scripts.  MIGR-31 TODO: Replace with prepareActions()
            try {
                StringBuilder sb = new StringBuilder("Generated Cli scripts:\n");
                for (String script : this.getCLIScripts()) {
                    sb.append("        ").append(script).append("\n");
                }
                log.info( sb.toString() );
            }
            catch( CliScriptException ex ) {
                throw ex;
            }
            
            
            // MIGR-31 TODO: Replace with prepareActions().
            try {
                this.copyItems();
            }
            catch (CopyException ex) {
                // TODO: Move this procedure into some rollback() method.
                RollbackUtils.removeData(ctx.getFileTransfers());
                // TODO: Can't just blindly delete, we need to keep info if we really created it!
                // TODO: Create some dedicated module dir manager.
                FileUtils.deleteQuietly( Utils.createPath(config.getGlobal().getAS7Config().getDir(), "modules", "jdbc"));
                throw new MigrationException(ex);
            }
            
            // MIGR-31 TODO: Replace with applyActions() to perform the actions.
            try {
                this.apply();
            }
            catch( Throwable ex ) {
                log.error("Applying the results to the target server failed: " + ex.toString(), ex);
                log.error("Rolling back the changes.");
                
                // MIGR-31 TODO: Replace with rollbackActions()
                try {
                    this.rollback_old( config );
                    throw ex;
                } catch( Throwable ex2 ){
                    log.error("Rollback failed: " + ex.toString(), ex2);
                    throw new RollbackMigrationException(ex, ex2);
                }
            }
        }// false
        //</editor-fold>
        
    }// migrate()
   
    
    
    /**
     *  Ask all the migrators to create the actions to be performed; stores them in the context.
     */
    private void prepareActions() throws MigrationException {
        log.debug("prepareActions()");
        try {
            for (IMigrator mig : this.migrators) {
                log.debug("    Preparing actions with " + mig.getClass().getSimpleName());
                mig.createActions(this.ctx);
            }
        } catch (JAXBException e) {
            throw new LoadMigrationException(e);
        }
        // TODO: Additional logic to filter out duplicated file copying etc.
    }
    
    
    /**
     *  Actions with the actions.
     */
    private void preValidateActions() throws MigrationException {
        List<IMigrationAction> actions = ctx.getActions();
        for( IMigrationAction action : actions ) {
            action.preValidate();
        }
    }
    private void backupActions() throws MigrationException {
        List<IMigrationAction> actions = ctx.getActions();
        for( IMigrationAction action : actions ) {
            action.backup();
        }
    }
    private void performActions() throws MigrationException {
        List<IMigrationAction> actions = ctx.getActions();
        for( IMigrationAction action : actions ) {
            action.perform();
        }
    }
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
    
    
    
    
    
    
    private void rollback_old( Configuration config ) throws Exception {
        RollbackUtils.rollbackAS7ConfigFile(ctx.getAs7ConfigXmlDocOriginal(), config);
        RollbackUtils.removeData(ctx.getFileTransfers());
        FileUtils.deleteQuietly( Utils.createPath(config.getGlobal().getAS7Config().getDir(), "modules", "jdbc"));
    }
    
    
    

    /**
     * Calls all migrators' callback for loading configuration data from the source server.
     *
     * @throws LoadMigrationException
     */
    public void loadAS5Data() throws LoadMigrationException {
        log.debug("loadAS5Data()");
        try {
            for (IMigrator mig : this.migrators) {
                log.debug("    Scanning with " + mig.getClass().getSimpleName());
                mig.loadAS5Data(this.ctx);
            }
        } catch (JAXBException e) {
            throw new LoadMigrationException(e);
        }
    }
    
    /**
     * Calls all migrators' callback for applying migrated configuration.
     * 
     * @throws ApplyMigrationException if inserting of generated nodes fails.
     */
    public void apply() throws ApplyMigrationException {
        log.debug("apply()");
        // Call the callbacks.
        for (IMigrator mig : this.migrators) {
            log.debug("    Applying with " + mig.getClass().getSimpleName());
            mig.apply(this.ctx);
        }
        // Put the resulting DOM to AS 7 config file.
        // TODO: This could alternatively send CLI commands over Management API. MIGR-28.
        try {
            // TODO: Isn't Transformer for XSLT? Use some normal XML output.
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            File targetConfigFile = new File(this.config.getGlobal().getAS7Config().getConfigFilePath());
            StreamResult result = new StreamResult(targetConfigFile);
            DOMSource source = new DOMSource(this.ctx.getAS7ConfigXmlDoc());
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            throw new ApplyMigrationException(ex);
        }
    }

    
    /**
     * Calls all migrators' callback for generating Dom Nodes.
     *
     * @return List containing all generated Nodes
     * @throws MigrationException if migrating of file or generating of nodes fails.
     */
    public List<Node> getDOMElements() throws MigrationException {
        log.debug("getDOMElements()");
        List<Node> elements = new LinkedList();
        for (IMigrator mig : this.migrators) {
            log.debug("    From " + mig.getClass().getSimpleName());
            elements.addAll(mig.generateDomElements(this.ctx));
        }
        return elements;
    }

    
    /**
     * Calls all migrators' callback for generating CLI scripts.
     *
     * @return List containing generated scripts from all migrated subsystems
     * @throws CliScriptException if creation of scripts fail
     */
    public List<String> getCLIScripts() throws CliScriptException {
        log.debug("getCLIScripts()");
        List<String> scripts = new LinkedList();
        for (IMigrator mig : this.migrators) {
            log.debug("    From " + mig.getClass().getSimpleName());
            scripts.addAll(mig.generateCliScripts(this.ctx));
        }

        return scripts;
    }

    
    /**
     * Copies all necessary files for migration from AS5 to their place in the AS7 home folder.
     *
     * @throws CopyException if copying of files fails.
     */
    public void copyItems() throws CopyException {
        log.debug("copyItems()");
        
        String targetServerDir = this.config.getGlobal().getAS7Config().getDir();
        File as5ProfileDir = this.config.getGlobal().getAS5Config().getProfileDir();
        File as5commonLibDir = Utils.createPath(this.config.getGlobal().getAS5Config().getDir(), "common", "lib");

        for (FileTransferInfo copyItem : this.ctx.getFileTransfers()) {
            log.debug("    Processing copy item: " + copyItem);

            if (copyItem.getName() == null || copyItem.getName().isEmpty()) {
                throw new IllegalStateException("Rollback data name is not set for " + copyItem);
            }

            Collection<File> files = Utils.searchForFile(copyItem, as5ProfileDir);

            // TODO:     This pulls IMigrator implementations details into generic class.
            // MIGR-23   Must be either generalized or moved to those implementations.
            
            switch( copyItem.getType() ) {
                case DRIVER:
                case LOGMODULE:
                    // For now only expecting one jar for driver. Pick the first one.
                    if( files.isEmpty() ) {
                        Collection<File> altList = Utils.searchForFile( copyItem, as5commonLibDir );
                        RollbackUtils.setRollbackData( copyItem, new ArrayList( altList ), targetServerDir );
                    } else {
                        RollbackUtils.setRollbackData( copyItem, new ArrayList( files ), targetServerDir );
                    }
                    break;
                    
                //case LOG: // We really don't want to migrate logs.
                case SECURITY:
                case RESOURCE:
                    RollbackUtils.setRollbackData( copyItem, files, targetServerDir );
                    break;
            }
        }// for each rollData

        log.debug("  2nd phase.");
        try {
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

            for( FileTransferInfo cp : this.ctx.getFileTransfers() ) {
                log.debug("    Processing copy item: " + cp);
                
                FileTransferInfo.Type type = cp.getType();
                if( type.equals(FileTransferInfo.Type.DRIVER) || type.equals(FileTransferInfo.Type.LOGMODULE) ) {
                    File directories = new File(cp.getTargetPath());
                    FileUtils.forceMkdir(directories);
                    File moduleXml = new File(directories.getAbsolutePath(), "module.xml");

                    if( ! moduleXml.createNewFile() )
                        throw new CopyException("File already exists: " + moduleXml.getPath());
                    
                    Document doc = FileTransferInfo.Type.DRIVER.equals(type)
                            ? AS7ModuleUtils.createModuleXML(cp)
                            : AS7ModuleUtils.createLogModuleXML(cp);
                    
                    transformer.transform( new DOMSource(doc), new StreamResult(moduleXml));
                }

                FileUtils.copyFileToDirectory(new File(cp.getHomePath()), new File(cp.getTargetPath()));
            }
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            throw new CopyException(e);
        }
    }// copyItems()


}// class
