/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.utils;

import org.jboss.loom.ex.CliScriptException;
import org.jboss.loom.ex.CopyException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.lang.StringUtils;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.tools.report.Reporter;

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
        System.out.println(" JBoss configuration migration tool for AS 5 / EAP 5 -> AS 7 / EAP 6 / WildFly 8");
        System.out.println();
        System.out.println(" Usage:");
        System.out.println();
        System.out.println("    java -jar AsMigrator.jar [<option>, ...] [as5.dir=]<as5.dir> [as7.dir=]<as7.dir>");
        System.out.println();
        System.out.println("       <as5.dir>   is expected to contain path to AS 5 or EAP 5 home directory, i.e. the one with server/ subdirectory.");
        System.out.println();
        System.out.println("       <as7.dir>   is expected to contain path to AS 7 or EAP 6 home directory, i.e. the one with jboss-modules.jar.");
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
        log.debug("    Looking in " +  dir.getPath() + " for a .jar with: " + classFilePath.replace('/', '.'));
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
        Collection<File> found = FileUtils.listFiles(dir, nff, FileFilterUtils.trueFileFilter());
        if( found.isEmpty() ) {
            throw new CopyException("File '" + fileName + "' was not found in " + dir.getAbsolutePath());
        }
        return found;
    }

    /**
     *  Searches a file of given name under given directory tree.
     *  @throws  CopyException if nothing found.
     */
    public static List<File> searchForFileOrDir(final String name, final File dir) throws IOException {

        List<File> found = new DirectoryWalker(){
            @Override protected boolean handleDirectory( File directory, int depth, Collection results ) throws IOException {
                if( directory.getName().equals( name ))
                    results.add( directory );
                return true;
            }
            @Override protected void handleFile( File file, int depth, Collection results ) throws IOException {
                results.add( file );
            }
            public List<File> search() throws IOException {
                List<File> found = new LinkedList();
                try { 
                    this.walk( dir, found );
                } catch( IOException ex ) {
                    throw new IOException("Failed traversing directory '" + dir.getAbsolutePath() + "' when looking for '" + name + "'");
                }
                return found;
            }
        }.search();
        
        if( found.isEmpty() ) {
            throw new FileNotFoundException("File '" + name + "' was not found in " + dir.getAbsolutePath());
        }
        return found;
    }

    /**
     * Builds up a File object with path consisting of given components.
     */
    public static File createPath(String parent, String child, String... more) {
        return createPath( new File(parent), child, more);
    }
    public static File createPath(File parent, String child, String... more) {
        File file = new File(parent, child);
        for (String component : more) {
            file = new File(file, component);
        }
        return file;
    }

    
    /**
     *  Finds a subclass of given class in current stacktrace.
     *  Returns null if not found.
     */
    public static <T> Class<? extends T> findSubclassInStackTrace(Class<T> parentClass) {
        // 0 - Thread.getStackTrace().
        // 1 - This method.
        // 2 - Whatever called this method.
        return findSubclassInStackTrace( parentClass, Thread.currentThread().getStackTrace(), 2 );
    }
    /**
     *  Finds a subclass of given $parentClass in given $stackTrace, skipping $skip levels.
     *  Returns null if not found.
     */
    public static <T> Class<? extends T> findSubclassInStackTrace(Class<T> parentClass, StackTraceElement[] stackTrace, int skip) {
        //for( StackTraceElement call : stackTrace) {
        for( int i = skip; i < stackTrace.length; i++ ) {
            StackTraceElement call = stackTrace[i];
            try {
                Class<?> callClass = Class.forName( call.getClassName() );
                if( parentClass.isAssignableFrom( callClass ) )
                    return (Class<? extends T>)  callClass;
            } catch( ClassNotFoundException ex ) {
                Reporter.log.error("Can't load class " + call.getClassName() + ":\n    " + ex.getMessage());
            }
        }
        return null;
    }
    
    
    
    /**
     *  Extracts all String getters properties to a map.
     */
    public static Map<String, String> describeBean(IConfigFragment bean){
        
        Map<String, String> ret = new LinkedHashMap();
                
        Method[] methods = bean.getClass().getMethods();
        for( Method method : methods ) {
            boolean get = false;
            String name = method.getName();
            
            // Only use getters which return String.
            if( method.getParameterTypes().length != 0 )  continue;
            if( ! method.getReturnType().equals( String.class ) )  continue;
            if( name.startsWith("get") )  get = true;
            if( ! (get || name.startsWith("is")) )  continue;
            
            // Remove "get" or "is".
            name =  name.substring( get ? 3 : 2 );
            // Uncapitalize, unless it's getDLQJNDIName.
            if( name.length() > 1 && ! Character.isUpperCase( name.charAt(2) ) )
                name =  StringUtils.uncapitalize( name );
            
            try {
                ret.put( name, (String) method.invoke(bean));
            } catch(     IllegalAccessException | IllegalArgumentException | InvocationTargetException ex ) {
                log.warn("Failed extracting property from " + bean.getClass().getSimpleName() + ":\n    " + ex.getMessage(), ex );
            }
        }
        return ret;
    }
    
    
    /**
     *  Returns null for empty strings.
     */
    public static String nullIfEmpty(String str){
        return str == null ? null : (str.isEmpty() ? null : str);
    }
    
    
    
    public static Properties mapToProperties( Map<String, String> map ) {
        Properties props = new Properties();
        Set<Map.Entry<String, String>> entries = map.entrySet();
        for( Map.Entry<String, String> entry : entries ) {
            props.put( entry.getKey(), entry.getValue() );
        }
        return props;
    }
    
}// class
