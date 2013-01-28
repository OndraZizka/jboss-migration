package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.ex.ApplyMigrationException;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.CopyException;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.migrators.connectionFactories.ResAdapterMigrator;
import cz.muni.fi.jboss.migration.migrators.dataSources.DatasourceMigrator;
import cz.muni.fi.jboss.migration.migrators.logging.LoggingMigrator;
import cz.muni.fi.jboss.migration.migrators.security.SecurityMigrator;
import cz.muni.fi.jboss.migration.migrators.server.ServerMigrator;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import javafx.util.Pair;
import org.apache.commons.lang.StringUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:25 AM
 */
//
//Concole UI
//        ==========
//
//        Usage:
//
//        java -jar AsMigrator.jar [<option>, ...] [as5.dir=]<as5.dir> [as7.dir=]<as7.dir>
//
//        Options:
//
//        as5.profile=<name>
//Path to AS 5 profile.
//        Default: "default"
//
//        as7.confPath=<path>
//Path to AS 7 config file.
//        Default: "standalone/conf/standalone.xml"
//
//        conf.<module>.<property>=<value> := Module-specific options.
//
//<module> := Name of one of modules. E.g. datasource, jaas, security, ...
//<property> := Name of the property to set. Specific per module. May occur multiple times.

public class MigratorApp {
    public static void main(String[] args) {
        if(args.length == 0){
            System.out.println("help");
        }
        GlobalConfiguration global = new GlobalConfiguration();
        Configuration configuration = new Configuration();

        Map< Class<? extends IMigrator>,List<Pair<String, String>>> moduleOptions = new HashMap();
        moduleOptions.put(SecurityMigrator.class, new ArrayList());
        moduleOptions.put(ServerMigrator.class, new ArrayList());
        moduleOptions.put(DatasourceMigrator.class, new ArrayList());
        moduleOptions.put(ResAdapterMigrator.class, new ArrayList());
        moduleOptions.put(LoggingMigrator.class, new ArrayList());

        for( int i = 0; i < args.length; i++ ) {
            if( args[i].contains("as5.dir") ) {
                global.setDirAS5(StringUtils.substringAfter(args[i], "=") + File.separator + "server" + File.separator);
                continue;
            }

            if( args[i].contains("as7.dir") ) {
                global.setDirAS7(StringUtils.substringAfter(args[i], "="));
                continue;
            }

            if( args[i].contains("as5.profile") ) {
                global.setProfileAS5(StringUtils.substringAfter(args[i], "="));
                continue;
            }

            if( args[i].contains("as7.confPath") ) {
                global.setProfileAS7(StringUtils.substringAfter(args[i], "="));
                continue;
            }

            if( args[i].contains("conf") ) {
                switch (StringUtils.substringAfter(args[i], ".")){
                     case "datasource":{
                         String property = StringUtils.substringAfterLast(args[i], ".");
                         String value =    StringUtils.substringAfter(args[i], "=");
                         moduleOptions.get(DatasourceMigrator.class).add(new Pair(property, value));
                     }
                     break;
                     case "logging":{
                         String property = StringUtils.substringAfterLast(args[i], ".");
                         String value =    StringUtils.substringAfter(args[i], "=");
                         moduleOptions.get(LoggingMigrator.class).add(new Pair(property, value));
                     }
                     break;
                     case "security":{
                         String property = StringUtils.substringAfterLast(args[i], ".");
                         String value =    StringUtils.substringAfter(args[i], "=");
                         moduleOptions.get(SecurityMigrator.class).add(new Pair(property, value));
                     }
                     break;
                     case "resource-adapter":{
                         String property = StringUtils.substringAfterLast(args[i], ".");
                         String value =    StringUtils.substringAfter(args[i], "=");
                         moduleOptions.get(ResAdapterMigrator.class).add(new Pair(property, value));
                     }
                     break;
                     case "server":{
                         String property = StringUtils.substringAfterLast(args[i], ".");
                         String value =    StringUtils.substringAfter(args[i], "=");
                         moduleOptions.get(ServerMigrator.class).add(new Pair(property, value));
                     }
                     default:{
                         System.err.println("Error: Wrong command : " + args[i] + " !");
                         writeHelp();
                         return;
                     }
                }
                continue;
            }

            System.err.println("Error: Wrong command : " + args[i] + " !");
            writeHelp();
            return;
        }
        global.setStandalonePath();
        configuration.setModuleOtions(moduleOptions);
        configuration.setOptions(global);

        try {
            MigrationContext ctx = new MigrationContext();
            ctx.createBuilder();
            Migrator migrator = new Migrator(configuration, ctx);

            migrator.loadAS5Data();
            migrator.apply();
            migrator.copyItems();
            for(String s : migrator.getCLIScripts()){
                System.out.println(s);
            }


        } catch (LoadMigrationException | CliScriptException | ApplyMigrationException | CopyException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } finally {
            // Reverse copying
        }
    }

    public static void writeHelp(){

    }

    // TODO: Name of the oracle driver is ojdbc.jar. What is problem. Check all others drivers.
    // TODO: Some files are declared for example in standard profile in AS5 but files which they reference are not? security web-console*


}

