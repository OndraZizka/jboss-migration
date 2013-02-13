package cz.muni.fi.jboss.migration.utils;

import cz.muni.fi.jboss.migration.Configuration;
import cz.muni.fi.jboss.migration.RollbackData;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.CopyException;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Utils class containing helping classes
 *
 * @author Roman Jakubco
 *         Date: 2/2/13
 *         Time: 1:05 PM
 */
public class Utils {
    /**
     * Method for testing if given string is null or empty and if it is then CliScriptException is thrown with given message
     *
     * @param string string for testing
     * @param errMsg message for exception
     * @param name   name of property of tested value
     * @throws CliScriptException if tested string is empty or null
     */
    public static void throwIfBlank(String string, String errMsg, String name) throws CliScriptException {
        if ((string == null) || (string.isEmpty())) {
            throw new CliScriptException(name + errMsg);
        }
    }

    /**
     * Helping method for copying files from AS5 to AS7. It checks if list is empty and if not then set HomePath and
     * targetPath of object of RollbackData. Plus for driver it creates special path from module of the driver.
     *
     * @param rollData   object representing files which should be copied
     * @param list       List of files found for this object of RollbackData
     * @param targetPath path to AS7 home dir
     * @throws cz.muni.fi.jboss.migration.ex.CopyException
     *          if no file was found and rolldata is not representing driver and if it is then if module of
     *          driver is null
     */
    public static void setRollbackData(RollbackData rollData, List<File> list, String targetPath)
            throws CopyException {
        if ((list.isEmpty()) && !(rollData.getType().equals("driver"))) {
            throw new CopyException("Cannot locate log file: " + rollData.getName() + "!");
        } else {
            rollData.setHomePath(list.get(0).getAbsolutePath());

            if (rollData.getType().equals("driver")) {
                rollData.setName(list.get(0).getName());
                String module;

                if (rollData.getModule() != null) {
                    String[] parts = rollData.getModule().split("\\.");
                    module = "";
                    for (String s : parts) {
                        module = module + s + File.separator;
                    }
                    rollData.setTargetPath(targetPath + File.separator + "modules" + File.separator +
                            module + "main");
                } else {
                    throw new CopyException("Error: Module for driver is null!");
                }
            } else {
                switch (rollData.getType()){
                    case "log": rollData.setTargetPath(targetPath + File.separator + "standalone" +
                            File.separator + "log");
                        break;
                    case "resource": rollData.setTargetPath(targetPath + File.separator + "standalone" +
                            File.separator + "deployments");
                        break;
                    case "security": rollData.setTargetPath(targetPath + File.separator + "standalone" +
                            File.separator + "configuration");
                }


            }
        }
    }

    /**
     * Helping method for writing help.
     */
    public static void writeHelp() {
        System.out.println();
        System.out.println("Usage:");
        System.out.println();
        System.out.println("    java -jar AsMigrator.jar [<option>, ...] [as5.dir=]<as5.dir> [as7.dir=]<as7.dir>");
        System.out.println();
        System.out.println("Options:");
        System.out.println();
        System.out.println("    as5.profile=<name>");
        System.out.println("        Path to AS 5 profile.");
        System.out.println("        Default: \"default\"");
        System.out.println();
        System.out.println("    as7.confPath=<path> ");
        System.out.println("        Path to AS 7 config file.");
        System.out.println("        Default: \"standalone/configuration/standalone.xml\"");
        System.out.println();
        System.out.println("    conf.<module>.<property>=<value> := Module-specific options.");
        System.out.println("        <module> := Name of one of modules. E.g. datasource, jaas, security, ...");
        System.out.println("        <property> := Name of the property to set. Specific per module. " +
                "May occur multiple times.");
        System.out.println();
    }

    /**
     * Method for removing copied data for migration from AS5 if the app fails. All rollbackData have set path to copied
     * files in AS7. So tthis method iterate over collection of these objects and try to delete them.
     *
     * @param rollbackDatas files, which where copied to AS7 folder
     */
    public static void removeData(Collection<RollbackData> rollbackDatas) {
        for (RollbackData rolldata : rollbackDatas) {
            if (!(rolldata.getType().equals("driver"))) {
                FileUtils.deleteQuietly(new File(rolldata.getTargetPath() + File.separator + rolldata.getName()));
            }
        }
    }

    /**
     * Method for returning standalone file to its original state before migration if the app fails.
     *
     * @param doc    object of Document representing original standalone file. This file is saved in Main before migration.
     * @param config configuration of app
     */
    public static void cleanStandalone(Document doc, Configuration config) {
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
}
