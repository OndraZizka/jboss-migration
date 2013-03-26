package cz.muni.fi.jboss.migration.utils;

import cz.muni.fi.jboss.migration.Configuration;
import cz.muni.fi.jboss.migration.RollbackData;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.CopyException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
     * 
     * TODO: This needs to be moved to some RollbackManager.
     */
    public static void setRollbackData(RollbackData rollData, List<File> list, String targetPath)
            throws CopyException {
        // TODO: What is list good for? It's not really used.
        
        // Huh? TODO: This should be wereever list is created.
        if( list.isEmpty() ) {
            throw new CopyException("Cannot locate file: " + rollData.getName());
        } 
        
        rollData.setHomePath(list.get(0).getAbsolutePath());

        switch( rollData.getType() ){
            case LOG: rollData.setTargetPath( Utils.createPath(targetPath, "standalone", "log").getPath() );
                break;
            case RESOURCE: rollData.setTargetPath(Utils.createPath(targetPath, "standalone", "deployments").getPath());
                break;
            case SECURITY: rollData.setTargetPath(Utils.createPath(targetPath, "standalone", "configuration").getPath());
                break;
            case DRIVER: case LOGMODULE:{
                rollData.setName(list.get(0).getName());
                if( rollData.getModule() == null)
                    throw new CopyException("Module in a rollback record is null!");

                /*String[] parts = rollData.getModule().split("\\."); // Split by dots.
                String module = "";
                for (String s : parts) {
                    module = module + s + File.separator;
                }*/
                String moduleSubPath = rollData.getModule().replace('.', '/');
                
                // TODO: Configurable modules dir. E.g. EAP 6.1 has modules in /system/base/modules.
                rollData.setTargetPath( Utils.createPath(targetPath, "modules", moduleSubPath, "main").getPath() );
            } break;

        }// switch( rollData.type )

    }// setRollbackData()

    
    /**
     * Helping method for writing help.
     */
    public static void writeHelp() {
        System.out.println();
        System.out.println(" Usage:");
        System.out.println();
        System.out.println("    java -jar AsMigrator.jar [<option>, ...] [as5.dir=]<as5.dir> [as7.dir=]<as7.dir>");
        System.out.println();
        System.out.println(" Options:");
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
            if (!(rolldata.getType().equals(RollbackData.Type.DRIVER))) {
                FileUtils.deleteQuietly(new File(rolldata.getTargetPath(), rolldata.getName()));
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

            StreamResult result = new StreamResult(new File(config.getGlobal().getAs7ConfigFilePath()));

            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Utils class for finding name of jar file containing class from logging configuration.
     *
     * @param className  name of the class which must be found
     * @param dirAS5   AS5 home dir
     * @param profileAS5  name of AS5 profile
     * @return  name of jar file which contains given class
     * @throws FileNotFoundException if the jar file is not found
     * 
     * TODO: This would cause false positives - e.g. class = org.Foo triggered by org/Foo/Blah.class .
     */
    public static String findJarFileWithClass(String className, String dirAS5, String profileAS5) throws FileNotFoundException, IOException {
        
        String classFilePath = className.replace(".", "/");
        
        // First look for jar file in lib directory in given AS5 profile
        File dir = Utils.createPath(dirAS5, "server", profileAS5, "lib");
        File jar = lookForJarWithAClass( dir, classFilePath );
        if( jar != null )
            return jar.getName();
        
        // If not found in profile's lib directory then try common/lib folder (common jars for all profiles)
        dir = Utils.createPath(dirAS5, "common", profileAS5, "lib");
        jar = lookForJarWithAClass( dir, classFilePath );
        if( jar != null )
            return jar.getName();
                            
        throw new FileNotFoundException("Cannot find jar file which contains class: " + className);
    }
    
    private static File lookForJarWithAClass( File dir, String classFilePath ) throws IOException {
        //SuffixFileFilter sf = new SuffixFileFilter(".jar");
        //List<File> list = (List<File>) FileUtils.listFiles(dir, sf, FileFilterUtils.makeCVSAware(null));
        Collection<File> list = FileUtils.listFiles(dir, new String[]{".jar"}, true);

        for( File file : list ) {
            JarFile jarFile = new JarFile(file);
            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if( ( ! entry.isDirectory() ) && entry.getName().contains(classFilePath)) {

                    // Assuming that jar file contains some package with class (common Java practice)
                    //return  StringUtils.substringAfterLast(file.getPath(), "/");
                    return file;
                }
            }
        }
        return null;
    }

    
    /**
     * Searching for file, which is represented as RollbackData in the application, in given directory
     *
     * @param rollData object representing file for search
     * @param dir directory for searching
     * @return list of found files
     */
    public static List<File> searchForFile(RollbackData rollData, File dir) {
        NameFileFilter nff;

        if (rollData.getType().equals(RollbackData.Type.DRIVER)) {
            final String name = rollData.getName();

            nff = new NameFileFilter(name) {
                @Override
                public boolean accept(File file) {
                    return file.getName().contains(name) && file.getName().contains("jar");
                }
            };
        } else {
            nff = new NameFileFilter(rollData.getName());
        }

        List<File> list = (List<File>) FileUtils.listFiles(dir, nff, FileFilterUtils.makeCVSAware(null));

        // One more search for driver jar. Other types of rollbackData just return list.
        if(rollData.getType().equals(RollbackData.Type.DRIVER)) {

            // For now only expecting one jar for driver. Pick the first one.
            if (list.isEmpty()) {

                // Special case for freeware jdbc driver jdts.jar
                if (rollData.getAltName() != null) {
                    final String altName = rollData.getAltName();

                    nff = new NameFileFilter(altName) {
                        @Override
                        public boolean accept(File file) {
                            return file.getName().contains(altName) && file.getName().contains("jar");
                        }
                    };
                    List<File> altList = (List<File>) FileUtils.listFiles(dir, nff,
                            FileFilterUtils.makeCVSAware(null));

                    return altList;
                }
            }
        }

        return list;
    }

    
    /**
     *  Builds up a File object with path consisting of given components.
     */
    public static File createPath( String parent, String child, String ... more) {
        File file = new File(parent, child);
        for( String component : more ) {
            file = new File(file, component);
        }
        return file;
    }
    
}// class
