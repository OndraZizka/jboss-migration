package org.jboss.loom.utils;

import org.jboss.loom.ex.CliScriptException;
import org.jboss.loom.ex.CopyException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Global utils class.
 *
 * @author Roman Jakubco
 */
public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);


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
     * Utils class for finding name of jar file containing class from logging configuration.
     *
     * @param className  name of the class which must be found
     * @param dirAS5     AS5 home dir
     * @param profileAS5 name of AS5 profile
     * @return name of jar file which contains given class
     * @throws FileNotFoundException if the jar file is not found
     *                               <p/>
     *                               TODO: This would cause false positives - e.g. class = org.Foo triggered by org/Foo/Blah.class .
     */
    public static File findJarFileWithClass(String className, String dirAS5, String profileAS5) throws FileNotFoundException, IOException {

        String classFilePath = className.replace(".", "/");

        // First look for jar file in lib directory in given AS5 profile
        File dir = Utils.createPath(dirAS5, "server", profileAS5, "lib");
        File jar = lookForJarWithAClass(dir, classFilePath);
        if (jar != null)
            //return jar.getName();
            return jar;

        // If not found in profile's lib directory then try common/lib folder (common jars for all profiles)
        dir = Utils.createPath(dirAS5, "common", "lib");
        jar = lookForJarWithAClass(dir, classFilePath);
        if (jar != null)
            //return jar.getName();
            return jar;

        throw new FileNotFoundException("Cannot find jar file which contains class: " + className);
    }

    private static File lookForJarWithAClass(File dir, String classFilePath) throws IOException {
        log.debug("    Looking in " +  dir.getPath() + " for a .jar with: " + classFilePath);
        if( ! dir.isDirectory() ){
            log.trace("    Not a directory: " +  dir.getPath());
            return null;
        }

        //SuffixFileFilter sf = new SuffixFileFilter(".jar");
        //List<File> list = (List<File>) FileUtils.listFiles(dir, sf, FileFilterUtils.makeCVSAware(null));
        Collection<File> jarFiles = FileUtils.listFiles(dir, new String[]{"jar"}, true);
        log.trace("    Found .jar files: " + jarFiles.size());

        for (File file : jarFiles) {
            // Search the contained files for those containing $classFilePath.
            try (JarFile jarFile = new JarFile(file)) {
                final Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry entry = entries.nextElement();
                    if ((!entry.isDirectory()) && entry.getName().contains(classFilePath)) {

                        // Assuming that jar file contains some package with class (common Java practice)
                        //return  StringUtils.substringAfterLast(file.getPath(), "/");
                        return file;
                    }
                }
            }
        }
        return null;
    }

    
    /**
     *  Searches a file of given name under given directory tree.
     *  @throws  CopyException if nothing found.
     */
    public static Collection<File> searchForFile(String fileName, File dir) throws CopyException {

        IOFileFilter nff = new NameFileFilter(fileName);
        Collection<File> list = FileUtils.listFiles(dir, nff, FileFilterUtils.trueFileFilter());
        if( list.isEmpty() ) {
            throw new CopyException("File '" + fileName + "' was not found in " + dir.getAbsolutePath());
        }
        return list;
    }

    /**
     * Builds up a File object with path consisting of given components.
     */
    public static File createPath(String parent, String child, String... more) {
        File file = new File(parent, child);
        for (String component : more) {
            file = new File(file, component);
        }
        return file;
    }


    /**
     * Creates a new default document builder.
     *
     *
     */
    public static DocumentBuilder createXmlDocumentBuilder() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        try {
            return dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex); // Tunnel
        }
    }

    /**
     * @deprecated TODO: useless?
     */
    public static Document parseFileToXmlDoc(File file) throws SAXException, IOException {
        DocumentBuilder db = Utils.createXmlDocumentBuilder();
        Document doc = db.parse(file);
        return doc;
    }

    /**
     * Creates clean Document used in other classes for working with XML
     *
     * @return clean Document
     * @throws ParserConfigurationException if creation of document fails
     */
    public static Document createDoc() throws ParserConfigurationException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setIgnoringComments(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();

        Document doc = builder.getDOMImplementation().createDocument(null, null, null);
        return doc;
    }

    /**
     * Transforms given Document into given File
     *
     * @param doc  xml document to transform
     * @param file targeted file
     * @return file containing XML document
     * @throws TransformerException if transformer fails
     */
    public static File transformDocToFile(Document doc, File file) throws TransformerException {
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

        transformer.transform(new DOMSource(doc), new StreamResult(file));

        return file;
    }


}// class
