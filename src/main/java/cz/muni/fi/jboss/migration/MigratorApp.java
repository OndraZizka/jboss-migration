package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.ex.*;
import cz.muni.fi.jboss.migration.migrators.connectionFactories.ResAdapterMigrator;
import cz.muni.fi.jboss.migration.migrators.dataSources.DatasourceMigrator;
import cz.muni.fi.jboss.migration.migrators.logging.LoggingMigrator;
import cz.muni.fi.jboss.migration.migrators.security.SecurityMigrator;
import cz.muni.fi.jboss.migration.migrators.server.ServerMigrator;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Main class of the application
 *
 * @author Roman Jakubco
 */

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
            e.printStackTrace();
            return;
        }

        try {
            migrator.getDOMElements();
        } catch (MigrationException e) {
            System.err.println(e.toString());
            e.printStackTrace();
            return;
        }

        // TODO: Where write scripts?
        try {
           for(String script : migrator.getCLIScripts()){
               System.out.println(script);
           }
        } catch (CliScriptException e) {
            System.err.println(e.toString());
            e.printStackTrace();
            return;
        }

        try {
            migrator.copyItems();
        } catch (CopyException e) {
            System.err.println(e.toString());
            e.printStackTrace();
            removeData(ctx.getCopyMemories());
            FileUtils.deleteQuietly(new File(global.getDirAS7() + File.separator + "modules" + File.separator + "jdbc"));
            return;
        }

        try {
            migrator.apply();
        } catch (ApplyMigrationException e) {
            System.err.println(e.toString());
            cleanStandalone(nonAlteredStandalone, configuration);
            e.printStackTrace(); removeData(ctx.getCopyMemories());
            FileUtils.deleteQuietly(new File(global.getDirAS7() + File.separator + "modules" + File.separator + "jdbc"));
            return;
        }
    }

    /**
     * Helping method for writing help.
     */
    private static void writeHelp(){

    }

    /**
     * Method for removing copied required data for migration from AS5 server if the app fails.
     *
     * @param copyMemories files, which where copied to AS7 folder
     */
    private static void removeData(Collection<CopyMemory> copyMemories){
        for(CopyMemory cp : copyMemories){
            if(!(cp.getType().equals("driver"))){
                FileUtils.deleteQuietly(new File(cp.getTargetPath() + File.separator + cp.getName()));
            }
        }
    }

    /**
     * Method for returning standalone file to its original state before migration if the app fails.
     *
     * @param doc object of Document representing original standalone file
     * @param config configuration of app
     */
    private static void cleanStandalone(Document doc, Configuration config){
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult result = new StreamResult(new File(config.getGlobal().getStandaloneFilePath()));

            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    // TODO: Name of the oracle driver is ojdbc.jar. What is problem. Check all others drivers.
    // TODO: Some files are declared for example in standard profile in AS5 but files which they reference are not? security web-console*


}

