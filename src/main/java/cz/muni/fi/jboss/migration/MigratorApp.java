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
import java.util.Map;

/**
 * Main class of the application
 *
 * @author Roman Jakubco
 */

public class MigratorApp {
    public static void main(String[] args) {
        if (args.length == 0) {
            Utils.writeHelp();
            return;
        }
        GlobalConfiguration global = new GlobalConfiguration();
        Configuration configuration = new Configuration();

        Map<Class<? extends IMigrator>, MultiValueMap> moduleOptions = new HashMap();
        moduleOptions.put(SecurityMigrator.class, new MultiValueMap());
        moduleOptions.put(ServerMigrator.class, new MultiValueMap());
        moduleOptions.put(DatasourceMigrator.class, new MultiValueMap());
        moduleOptions.put(ResAdapterMigrator.class, new MultiValueMap());
        moduleOptions.put(LoggingMigrator.class, new MultiValueMap());

        for (String arg : args) {
            if (arg.contains("as5.dir")) {
                global.setDirAS5(StringUtils.substringAfter(arg, "=") + File.separator + "server" + File.separator);
                continue;
            }

            if (arg.contains("as7.dir")) {
                global.setDirAS7(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if (arg.contains("as5.profile")) {
                global.setProfileAS5(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if (arg.contains("as7.confPath")) {
                global.setConfPathAS7(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if (arg.contains("conf")) {
                String conf = StringUtils.substringAfter(arg, ".");
                if (conf.contains("datasource")) {
                    String property = StringUtils.substringBetween(conf, ".", "=");
                    String value = StringUtils.substringAfter(conf, "=");
                    moduleOptions.get(DatasourceMigrator.class).put(property, value);
                }

                if (conf.contains("logging")) {
                    String property = StringUtils.substringBetween(conf, ".", "=");
                    String value = StringUtils.substringAfter(conf, "=");
                    moduleOptions.get(LoggingMigrator.class).put(property, value);
                }

                if (conf.contains("security")) {
                    String property = StringUtils.substringBetween(conf, ".", "=");
                    String value = StringUtils.substringAfter(conf, "=");
                    moduleOptions.get(SecurityMigrator.class).put(property, value);
                }

                if (conf.contains("resource")) {
                    String property = StringUtils.substringBetween(conf, ".", "=");
                    String value = StringUtils.substringAfter(conf, "=");
                    moduleOptions.get(ResAdapterMigrator.class).put(property, value);
                }

                if (conf.contains("server")) {
                    String property = StringUtils.substringBetween(conf, ".", "=");
                    String value = StringUtils.substringAfter(conf, "=");
                    moduleOptions.get(ServerMigrator.class).put(property, value);
                }

                System.err.println("Error: Wrong command : " + arg + " !");
                Utils.writeHelp();

                return;
            }

            System.err.println("Error: Wrong command : " + arg + " !");
            Utils.writeHelp();
            return;
        }
        global.setStandalonePath();

        configuration.setModuleOtions(moduleOptions);
        configuration.setOptions(global);

        Migrator migrator;
        MigrationContext ctx;
        Document nonAlteredStandalone;


        try {
            ctx = new MigrationContext();
            ctx.createBuilder();
            File standalone = new File(configuration.getGlobal().getStandaloneFilePath());

            Document doc = ctx.getDocBuilder().parse(standalone);
            nonAlteredStandalone = ctx.getDocBuilder().parse(standalone);
            ctx.setStandaloneDoc(doc);

            migrator = new Migrator(configuration, ctx);

            migrator.loadAS5Data();
        } catch (ParserConfigurationException | LoadMigrationException | SAXException | IOException e) {
            System.err.println(e.toString());
            return;
        }

        try {
            migrator.getDOMElements();
        } catch (MigrationException e) {
            System.err.println(e.toString());
            return;
        }

        // TODO: Where write scripts?
        try {
            for (String script : migrator.getCLIScripts()) {
                System.out.println(script);
            }
        } catch (CliScriptException e) {
            System.err.println(e.toString());
            return;
        }

        try {
            migrator.copyItems();
        } catch (CopyException e) {
            System.err.println(e.toString());
            Utils.removeData(ctx.getRollbackDatas());
            FileUtils.deleteQuietly(new File(global.getDirAS7() + File.separator + "modules" + File.separator + "jdbc"));
            return;
        }

        try {
            migrator.apply();
        } catch (ApplyMigrationException e) {
            System.err.println(e.toString());
            Utils.cleanStandalone(nonAlteredStandalone, configuration);
            Utils.removeData(ctx.getRollbackDatas());
            FileUtils.deleteQuietly(new File(global.getDirAS7() + File.separator + "modules" + File.separator + "jdbc"));
        }
    }


    // TODO: Some files are declared for example in standard profile in AS5 but files which they reference are not? security web-console*


}

