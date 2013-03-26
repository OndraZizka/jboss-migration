package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.Configuration.ModuleSpecificProperty;
import cz.muni.fi.jboss.migration.ex.*;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Main class of the application
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
                globalConfig.setAS5Dir(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("--as7.dir=") || arg.startsWith("as7.dir=")) {
                globalConfig.setAS7Dir(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("--as5.profile=") ) {
                globalConfig.setAS5ProfileName(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("--as7.confPath=") ) {
                globalConfig.setAS7ConfigPath(StringUtils.substringAfter(arg, "="));
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
                
                /*// Let all migrator instances process the property.
                int pickedUp = 0;
                for( IMigrator mig : migrators.values() ){
                    pickedUp += mig.examineConfigProperty(module, property, value);
                }
                if( pickedUp == 0 )
                    System.err.println("Warning: No module recognized the argument: " + arg + " !");
                */
                // TODO: Move this to Migrator{}.
                
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
        String path = config.getGlobal().getAS5Dir();
        if( null == path )
            problems.add("as5.dir was not set.");
        else if( ! new File(path).isDirectory() )
            problems.add("as5.dir is not a directory: " + path);
        else {
            String profileName = config.getGlobal().getAS5ProfileName();
            if( null == profileName )
                ;
            else {
                File profileDir = new File(path, GlobalConfiguration.AS5_PROFILES_DIR + profileName);
                if( ! profileDir.exists() )
                    problems.add("as5.profile is not a subdirectory in AS 5 dir: " + path);
            }
        }
        
        // AS 7
        path = config.getGlobal().getAS7Dir();
        if( null == path )
            problems.add("as7.dir was not set.");
        else if( ! new File(path).isDirectory() )
            problems.add("as7.dir is not a directory: " + path);
        else {
            String configPath = config.getGlobal().getAs7ConfigFilePath();
            if( null == configPath )
                ; //problems.add("as7.confPath was not set."); // TODO: Put defaults to the config.
            else if( ! new File(path, configPath).exists() )
                problems.add("as7.confPath is not a subpath under AS 5 dir: " + path);
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
        
        Migrator migrator;
        MigrationContext ctx = new MigrationContext();
        Document nonAlteredStandalone;

        log.info("Commencing migration.");
        
        File as7configFile = new File(conf.getGlobal().getAs7ConfigFilePath());
        try {
            ctx.createBuilder();

            Document doc = ctx.getDocBuilder().parse(as7configFile);
            nonAlteredStandalone = ctx.getDocBuilder().parse(as7configFile);
            ctx.setStandaloneDoc(doc);

            // Create Migrator // TODO: Move above try{}.
            migrator = new Migrator(conf, ctx);

            migrator.loadAS5Data();
        } 
        catch (ParserConfigurationException | LoadMigrationException | SAXException | IOException e) {
            throw new MigrationException("Failed loading AS 7 config from " + as7configFile, e);
        }

        try {
            migrator.getDOMElements();
        }
        catch (MigrationException e) {
            throw new MigrationException(e);
        }

        try {
            StringBuilder sb = new StringBuilder("Generated Cli scripts:\n");
            for (String script : migrator.getCLIScripts()) {
                sb.append("        ").append(script).append("\n");
            }
            log.info( sb.toString() );
        } 
        catch (CliScriptException ex) {
            throw ex;
        }

        try {
            migrator.copyItems();
        }
        catch (CopyException ex) {
            // TODO: Move this procedure into some rollback() method.
            Utils.removeData(ctx.getRollbackData());
            // TODO: Can't just blindly delete, we need to keep info if we really created it!
            // TODO: Create some dedicated module dir manager.
            FileUtils.deleteQuietly( Utils.createPath(conf.getGlobal().getAS7Dir(), "modules", "jdbc"));
            throw new MigrationException(ex);
        }

        try {
            migrator.apply();
            log.info("");
            log.info("Migration was successful.");
        } catch (ApplyMigrationException ex) {
            // TODO: Rollback handling needs to be wrapped behind an abstract API.
            //       Calls such like this have to be encapsulated in some RollbackManager.
            Utils.cleanStandalone(nonAlteredStandalone, conf);
            Utils.removeData(ctx.getRollbackData());
            FileUtils.deleteQuietly( Utils.createPath(conf.getGlobal().getAS7Dir(), "modules", "jdbc"));
            throw ex;
        }
        
    }// migrate()

    
}// class
