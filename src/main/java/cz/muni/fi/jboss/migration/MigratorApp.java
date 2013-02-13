package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.ex.*;
import cz.muni.fi.jboss.migration.migrators.connectionFactories.ResAdapterMigrator;
import cz.muni.fi.jboss.migration.migrators.dataSources.DatasourceMigrator;
import cz.muni.fi.jboss.migration.migrators.logging.LoggingMigrator;
import cz.muni.fi.jboss.migration.migrators.security.SecurityMigrator;
import cz.muni.fi.jboss.migration.migrators.server.ServerMigrator;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
        
        

        // Find IMigrator implementations.
        List<Class<? extends IMigrator>> migratorClasses = findMigratorClasses();
        
        // TODO: Initialize migrator instances and pass that; 
        //       and let parseArguments() let migrators process module-specific args.
        //List<? extends IMigrator> migrators = createMigrators( migratorClasses );
        
                
        // Parse arguments.
        Configuration configuration = parseArguments( args, migratorClasses );
        if( null == configuration )  System.exit(1);
        
        // Migrate.
        migrate( configuration );

        
    }// main()


    // TODO: Some files are declared for example in standard profile in AS5 but files which they reference are not? security web-console*

    
    /**
     *  Find implementation of IMigrator.
     *  TODO: Implement scanning for classes.
     */
    private static List<Class<? extends IMigrator>> findMigratorClasses() {
        
        LinkedList<Class<? extends IMigrator>> migrators = new LinkedList();
        migrators.add( SecurityMigrator.class );
        migrators.add( ServerMigrator.class );
        migrators.add( DatasourceMigrator.class );
        migrators.add( ResAdapterMigrator.class );
        migrators.add( LoggingMigrator.class );
        
        return migrators;
    }

    
    /**
     *  Parses app's arguments.
     *  @returns  Configuration initialized according to args.
     */
    private static Configuration parseArguments(String[] args, List<Class<? extends IMigrator>> migratorClasses) {
    
        // Global config
        GlobalConfiguration global = new GlobalConfiguration();
        
        // Module-specific options map. TODO: Could be wrapped into class of it's own.
        Map<Class<? extends IMigrator>, MultiValueMap> moduleOptions = new HashMap();
        for (Class<? extends IMigrator> cls : migratorClasses) {
            moduleOptions.put(cls, new MultiValueMap());
        }
        
        
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
                String conf = StringUtils.substringAfter(arg, ".");
                String module = StringUtils.substringBefore(conf, ".");
                
                if (conf.startsWith("datasource.")) {
                    String property = StringUtils.substringBetween(conf, ".", "=");
                    String value = StringUtils.substringAfter(conf, "=");
                    moduleOptions.get(DatasourceMigrator.class).put(property, value);
                }

                else if (conf.startsWith("logging.")) {
                    String property = StringUtils.substringBetween(conf, ".", "=");
                    String value = StringUtils.substringAfter(conf, "=");
                    moduleOptions.get(LoggingMigrator.class).put(property, value);
                }

                else if (conf.startsWith("security.")) {
                    String property = StringUtils.substringBetween(conf, ".", "=");
                    String value = StringUtils.substringAfter(conf, "=");
                    moduleOptions.get(SecurityMigrator.class).put(property, value);
                }

                else if (conf.startsWith("resource.")) {
                    String property = StringUtils.substringBetween(conf, ".", "=");
                    String value = StringUtils.substringAfter(conf, "=");
                    moduleOptions.get(ResAdapterMigrator.class).put(property, value);
                }

                else if (conf.startsWith("server.")) {
                    String property = StringUtils.substringBetween(conf, ".", "=");
                    String value = StringUtils.substringAfter(conf, "=");
                    moduleOptions.get(ServerMigrator.class).put(property, value);
                }

                else
                    System.err.println("Error: No module knows the argument: " + arg + " !");
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
