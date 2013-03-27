package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.conf.Configuration;
import cz.muni.fi.jboss.migration.conf.GlobalConfiguration;
import cz.muni.fi.jboss.migration.conf.Configuration.ModuleSpecificProperty;
import cz.muni.fi.jboss.migration.ex.*;
import cz.muni.fi.jboss.migration.utils.RollbackUtils;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;


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
    private static List<String> validateConfiguration(Configuration config) {
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
        path = config.getGlobal().getAS7Config().getDir();
        if( null == path )
            problems.add("as7.dir was not set.");
        else if( ! new File(path).isDirectory() )
            problems.add("as7.dir is not a directory: " + path);
        else {
            String configPath = config.getGlobal().getAS7Config().getConfigFilePath();
            if( null == configPath )
                ; //problems.add("as7.confPath was not set."); // TODO: Put defaults to the config.
            else{
                File configFile = new File(path, configPath);
                if( ! configFile.exists() )
                problems.add("as7.confPath is not a subpath under AS 5 dir: " + configFile.getPath());
            }
        }
        
        // App (deployment)
        path = config.getGlobal().getAppPath();
        if( null != path && ! new File(path).exists())
            problems.add("App path was set but does not exist: " + path);
        
        return problems;
    }


    
    
    /**
     *  Performs the migration.
     *  TODO: Should probably be in Migrator{}.
     */
    private static void migrate( Configuration conf ) throws MigrationException {
        
        MigrationContext ctx = new MigrationContext();

        log.info("Commencing migration.");
        
        File as7configFile = new File(conf.getGlobal().getAS7Config().getConfigFilePath());
        // Parse AS 7 config.
        try {
            DocumentBuilder db = Utils.createXmlDocumentBuilder();
            Document doc = db.parse(as7configFile);
            ctx.setAS7ConfigXmlDoc(doc);
            // TODO: Do backup at file level, instead of parsing and writing back.
            doc = db.parse(as7configFile);
            ctx.setAs7ConfigXmlDocOriginal(doc);
        } 
        catch ( SAXException | IOException ex ) {
            throw new MigrationException("Failed loading AS 7 config from " + as7configFile, ex );
        }
        // Create Migrator & load AS 5 data.
        MigratorEngine migrator = new MigratorEngine(conf, ctx);
        try {
            migrator.loadAS5Data();
        } 
        catch ( LoadMigrationException ex ) {
            throw new MigrationException("Failed loading AS 7 config from " + as7configFile, ex );
        }

        
        try {
            migrator.getDOMElements(); // ??? Ignores the results?
        }
        catch( MigrationException e ) {
            throw new MigrationException(e);
        }

        try {
            StringBuilder sb = new StringBuilder("Generated Cli scripts:\n");
            for (String script : migrator.getCLIScripts()) {
                sb.append("        ").append(script).append("\n");
            }
            log.info( sb.toString() );
        } 
        catch( CliScriptException ex ) {
            throw ex;
        }

        try {
            migrator.copyItems();
        }
        catch (CopyException ex) {
            // TODO: Move this procedure into some rollback() method.
            RollbackUtils.removeData(ctx.getRollbackData());
            // TODO: Can't just blindly delete, we need to keep info if we really created it!
            // TODO: Create some dedicated module dir manager.
            FileUtils.deleteQuietly( Utils.createPath(conf.getGlobal().getAS7Config().getDir(), "modules", "jdbc"));
            throw new MigrationException(ex);
        }

        try {
            migrator.apply();
            log.info("");
            log.info("Migration was successful.");
        }
        catch( Throwable ex ) {
            log.error("Applying the results to the target server failed: " + ex.toString(), ex);
            log.error("Rolling back the changes.");
            
            try {
                // TODO: MIGR-24: Rollback handling needs to be wrapped behind an abstract API.
                //                Calls such like this have to be encapsulated in some RollbackManager.
                RollbackUtils.rollbackAS7ConfigFile(ctx.getAs7ConfigXmlDocOriginal(), conf);
                RollbackUtils.removeData(ctx.getRollbackData());
                FileUtils.deleteQuietly( Utils.createPath(conf.getGlobal().getAS7Config().getDir(), "modules", "jdbc"));
            } catch( Throwable ex2 ){
                log.error("Rollback failed: " + ex.toString(), ex2);
            }
        }
        
    }// migrate()

    
}// class
