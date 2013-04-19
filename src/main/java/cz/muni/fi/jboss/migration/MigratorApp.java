package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.conf.AS7Config;
import cz.muni.fi.jboss.migration.conf.Configuration;
import cz.muni.fi.jboss.migration.conf.Configuration.ModuleSpecificProperty;
import cz.muni.fi.jboss.migration.conf.GlobalConfiguration;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.ex.RollbackMigrationException;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import org.jboss.as.controller.client.ModelControllerClient;


/**
 * Main class of the application.
 * Contains app-specific code, like arguments parsing, instantiating the Migrator etc.
 *
 * @author Roman Jakubco
 */
public class MigratorApp {
    
    private static final Logger log = LoggerFactory.getLogger(MigratorApp.class);
    
    
    public static void main(String[] args) {
        if (args.length == 0) {
            Utils.writeHelp();
            return;
        }
                        
        // Parse arguments.
        Configuration configuration = parseArguments( args );
        if( null == configuration )
            System.exit(1);
        
        // Apply defaults.
        applyDefaults( configuration );
        
        // Validate config.
        List<String> problems = validateConfiguration( configuration );
        if( !problems.isEmpty() ){
            for( String problem : problems )
                log.error(problem);
            System.exit(1);
        }
        
        // Migrate.
        try {
            migrate( configuration );
        } catch (MigrationException ex) {
            log.error("Migration failed (details at DEBUG level): " + ex.getMessage());
            log.debug("Migration failed: ", ex);
        }

        
    }// main()


    // TODO: Some files are declared for example in standard profile in AS5 but files which they reference are not? security web-console*

    
    
    /**
     *  Parses app's arguments.
     *  @returns  Configuration initialized according to args.
     */
    private static Configuration parseArguments(String[] args) {
    
        // Global config
        GlobalConfiguration globalConfig = new GlobalConfiguration();
        
        // Module-specific options.
        List<ModuleSpecificProperty> moduleConfigs = new LinkedList<>();
        
        
        // For each argument...
        for (String arg : args) {
            if( arg.startsWith("--help") ){
                Utils.writeHelp();
                return null;
            }
            if( arg.startsWith("--as5.dir=") || arg.startsWith("as5.dir=") ) {
                globalConfig.getAS5Config().setDir(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("--as7.dir=") || arg.startsWith("as7.dir=")) {
                globalConfig.getAS7Config().setDir(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("--as5.profile=") ) {
                globalConfig.getAS5Config().setProfileName(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("--as7.confPath=") ) {
                globalConfig.getAS7Config().setConfigPath(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("--as7.mgmt=") ) {
                parseMgmtConn( StringUtils.substringAfter(arg, "="), globalConfig.getAS7Config() );
                continue;
            }

            if( arg.startsWith("--app.path=") ) {
                globalConfig.setAppPath(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("--valid.skip") ) {
                globalConfig.setSkipValidation(true);
                continue;
            }

            // Module-specific configurations.
            // TODO: Process by calling IMigrator instances' callback.
            if (arg.startsWith("--conf.")) {
                
                // --conf.<module>.<property.name>[=<value>]
                String conf = StringUtils.substringAfter(arg, ".");
                String module = StringUtils.substringBefore(conf, ".");
                String propName = StringUtils.substringAfter(conf, ".");
                int pos = propName.indexOf('=');
                String value = null;
                if( pos == -1 ){
                    value = propName.substring(pos+1);
                    propName = propName.substring(0, pos);
                }
                
                moduleConfigs.add( new ModuleSpecificProperty(module, propName, value));
            }

            System.err.println("Warning: Unknown argument: " + arg + " !");
            Utils.writeHelp();
            continue;
        }
        //globalConfig.setStandaloneFilePath();

        Configuration configuration = new Configuration();
        configuration.setModuleConfigs(moduleConfigs);
        configuration.setGlobalConfig(globalConfig);
        
        return configuration;
        
    }// parseArguments()

    

    /**
     *  Sets the default values.
     */
    private static void applyDefaults(Configuration configuration) {
        // TODO
    }

    
    
    /**
     *  Validates the config - checks if the paths exist, contain the expected files etc.
     * 
     *  @returns  True if everything is OK.
     */
    public static List<String> validateConfiguration(Configuration config) {
        LinkedList<String> problems = new LinkedList<>();
        
        // AS 5
        String path = config.getGlobal().getAS5Config().getDir();
        if( null == path )
            problems.add("as5.dir was not set.");
        else if( ! new File(path).isDirectory() )
            problems.add("as5.dir is not a directory: " + path);
        else {
            String profileName = config.getGlobal().getAS5Config().getProfileName();
            if( null == profileName )
                ;
            else {
                File profileDir = config.getGlobal().getAS5Config().getProfileDir();
                if( ! profileDir.exists() )
                    problems.add("as5.profile is not a subdirectory in AS 5 dir: " + profileDir.getPath());
            }
        }
        // AS 7
        AS7Config as7Config = config.getGlobal().getAS7Config();
        path = as7Config.getDir();
        if( null == path )
            problems.add("as7.dir was not set.");
        else if( ! new File(path).isDirectory() )
            problems.add("as7.dir is not a directory: " + path);
        else {
            String configPath = as7Config.getConfigFilePath();
            if( null == configPath )
                ; //problems.add("as7.confPath was not set."); // TODO: Put defaults to the config.
            else{
                File configFile = new File(path, configPath);
                if( ! configFile.exists() )
                //    problems.add(
                    log.warn("as7.confPath is not a subpath under AS 7 dir: " + configFile.getPath() );
            }
        }
        
        // Management host and port
        mgmt: {
            if( as7Config.getManagementPort() == -1 ){
                problems.add("as7.mgmt doesn't contain valid port after ':'.");
                break mgmt;
            }
        
            ModelControllerClient client = null;
            try {
                client = ModelControllerClient.Factory.create(as7Config.getHost(), as7Config.getManagementPort());
                client.close();
            }
            catch( UnknownHostException ex ){
                problems.add("Can't connect to AS 7 management: " + as7Config.getHost() + ":" + as7Config.getManagementPort());
            }
            catch( IOException ex ){ } // Happens on close().
        }

        
        // App (deployment)
        path = config.getGlobal().getAppPath();
        if( null != path && ! new File(path).exists())
            problems.add("App path was set but does not exist: " + path);
        
        return problems;
    }



    
    /**
     *  Performs the migration.
     */
    public static void migrate( Configuration conf ) throws MigrationException {
        
        log.info("Commencing migration.");
        
        MigratorEngine migrator = new MigratorEngine(conf);

        try {
            migrator.doMigration();
            log.info("");
            log.info("Migration was successful.");
        }
        catch( Throwable ex ) {
            log.error("Migration failed. See previous messages for progress. ");
            
            if( ex instanceof RollbackMigrationException ){
                log.error("Yet, the rollback attempt failed as well. "
                        + "The server configuration may have ended up in an inconsistent state!");
                log.error("");
                RollbackMigrationException rollEx = (RollbackMigrationException) ex;
                log.error("Rollback failure cause: " + rollEx.getRollbackCause(), rollEx.getRollbackCause());
                ex = rollEx.getCause();
            }
            
            log.error("");
            log.error("Migration failure cause: " + ex, ex);
        }
        
    }// migrate()


    /**
     * @param mgmtConn   localhost:9999
     */
    private static void parseMgmtConn( String mgmtConn, AS7Config aS7Config ) {
        String host = StringUtils.substringBefore(mgmtConn, ":");
        if( ! mgmtConn.contains(":"))  return;
        
        String port = StringUtils.substringAfter(mgmtConn, ":");
        aS7Config.setHost( host );
        try {
            aS7Config.setManagementPort( Integer.parseInt( port ) );
        } catch( NumberFormatException ex ){
            aS7Config.setManagementPort( -1 );
        }
    }

    
}// class
