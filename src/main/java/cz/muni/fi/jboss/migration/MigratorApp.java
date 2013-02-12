package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.Configuration.ModuleSpecificProperty;
import cz.muni.fi.jboss.migration.ex.*;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if( null == configuration )  System.exit(1);
        
        // Migrate.
        migrate( configuration );

        
    }// main()


    // TODO: Some files are declared for example in standard profile in AS5 but files which they reference are not? security web-console*

    
    
    /**
     *  Parses app's arguments.
     *  @returns  Configuration initialized according to args.
     */
    private static Configuration parseArguments(String[] args) {
    
        // Global config
        GlobalConfiguration global = new GlobalConfiguration();
        
        // Module-specific options.
        List<ModuleSpecificProperty> moduleOptions = new LinkedList<>();
        
        
        // For each argument...
        for (String arg : args) {
            if(arg.startsWith("--help")){
                Utils.writeHelp();
                return null;
            }
            if (arg.startsWith("--as5.dir=")) {
                global.setDirAS5(StringUtils.substringAfter(arg, "=") + File.separator + "server" + File.separator);
                continue;
            }

            if (arg.startsWith("--as7.dir=")) {
                global.setDirAS7(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if (arg.startsWith("--as5.profile=")) {
                global.setProfileAS5(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if (arg.startsWith("--as7.confPath=")) {
                global.setConfPathAS7(StringUtils.substringAfter(arg, "="));
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
                
                moduleOptions.add( new ModuleSpecificProperty(module, propName, value));
            }

            System.err.println("Warning: Unknown argument: " + arg + " !");
            Utils.writeHelp();
            continue;
        }
        global.setStandalonePath();

        Configuration configuration = new Configuration();
        configuration.setModuleOtions(moduleOptions);
        configuration.setOptions(global);
        
        return configuration;
        
    }// parseArguments()

    
    
    
    /**
     *  Performs the migration.
     *  TODO: Should probably be in Migrator{}.
     */
    private static void migrate( Configuration conf ) {
        
        Migrator migrator;
        MigrationContext ctx;
        Document nonAlteredStandalone;

        System.out.println("Migration:");
        try {
            ctx = new MigrationContext();
            
            ctx.createBuilder();
            File standalone = new File(conf.getGlobal().getStandaloneFilePath());

            Document doc = ctx.getDocBuilder().parse(standalone);
            nonAlteredStandalone = ctx.getDocBuilder().parse(standalone);
            ctx.setStandaloneDoc(doc);

            // Create Migrator
            migrator = new Migrator(conf, ctx);

            migrator.loadAS5Data();
        } catch (ParserConfigurationException | LoadMigrationException | SAXException | IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            migrator.getDOMElements();
        } catch (MigrationException e) {
            e.printStackTrace();
            return;
        }

        try {
            System.out.println("Generated Cli scripts:");
            for (String script : migrator.getCLIScripts()) {
                System.out.println(script);
            }
        } catch (CliScriptException e) {
            e.printStackTrace();
            return;
        }

        try {
            migrator.copyItems();
        } catch (CopyException e) {
            e.printStackTrace();
            Utils.removeData(ctx.getRollbackDatas());
            FileUtils.deleteQuietly(new File(conf.getGlobal().getDirAS7() + File.separator + "modules" + File.separator + "jdbc"));
            return;
        }

        try {
            migrator.apply();
            System.out.println();
            System.out.println("Migration was successful");
        } catch (ApplyMigrationException e) {
            e.printStackTrace();
            Utils.cleanStandalone(nonAlteredStandalone, conf);
            Utils.removeData(ctx.getRollbackDatas());
            FileUtils.deleteQuietly(new File(conf.getGlobal().getDirAS7() + File.separator + "modules" + File.separator + "jdbc"));
        }
        
    }// migrate()


}// class
