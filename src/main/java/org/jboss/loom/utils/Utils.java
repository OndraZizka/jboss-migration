/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.utils;

import groovy.lang.GroovyClassLoader;
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
import java.io.InputStream;
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
     *  Prints app help.
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
     *  TODO: Return a list of files.
     */
    public static File lookForJarWithClass( String className, File... dirs ) throws IOException {
        for( File dir : dirs ) {
            log.debug("    Looking in " +  dir.getPath() + " for a .jar with: " + className);

            if( ! dir.isDirectory() ){
                log.trace("    Not a directory: " +  dir.getPath());
                continue;
            }

            Collection<File> jarFiles = FileUtils.listFiles(dir, new String[]{"jar"}, true);
            log.trace("    Found .jar files: " + jarFiles.size());

            String classFilePath = className.replace(".", "/");

            for( File file : jarFiles ) {
                // Search the contained files for those containing $classFilePath.
                try( JarFile jarFile = new JarFile(file) ) {
                    if( containsClass( jarFile, classFilePath ) )
                        return file;
                }
            }
        }
        return null;
    }// lookForJarWithClass()


    
    private static boolean containsClass( JarFile jarFile, String classFilePath ) {
        final Enumeration<JarEntry> entries = jarFile.entries();
        while( entries.hasMoreElements() ) {
            final JarEntry entry = entries.nextElement();
            if( ( ! entry.isDirectory() ) && entry.getName().contains( classFilePath ))
                return true;
        }
        return false;
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
    public static List<File> searchForFileOrDir( final String name, final File dir ) throws IOException {

        List<File> found = new DirectoryWalker(){
            @Override protected boolean handleDirectory( File directory, int depth, Collection results ) throws IOException {
                if( directory.getName().equals( name ))
                    results.add( directory );
                return true;
            }
            @Override protected void handleFile( File file, int depth, Collection results ) throws IOException {
                if( file.getName().equals( name ))
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
     *  Missing from Commons IO's FileUtils...
     */
    public static void copyFileOrDirectory( File src, File dest  ) throws IOException {
        if( src.isFile() )
            FileUtils.copyFile( src, dest );
        else if( src.isDirectory() )
            FileUtils.copyDirectory( src, dest );
        else
            throw new UnsupportedOperationException("Can only copy file or directory. Not this: " + src.getPath());
    }


    
    // ======= Lang utils ====== //
    
    
    
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
     *  @see also @Property.Utils.convert*()
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
     *  Throws a formatted message (name + errMsg) if string is null or empty.
     */
    public static void throwIfBlank(String string, String errMsg, String name) throws CliScriptException {
        if ((string == null) || (string.isEmpty())) {
            throw new CliScriptException(name + errMsg);
        }
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

    
    
    // ======= Class utils ====== //
    
    public static void copyResourceToDir( Class cls, String name, File dir ) throws IOException {
        String packageDir =  cls.getPackage().getName().replace('.', '/');
        String path =  "/" + packageDir + "/" + name;
        InputStream is = GroovyClassLoader.class.getResourceAsStream( path );
        if( is == null )
            throw new IllegalArgumentException("Resource not found: " + packageDir);
        FileUtils.copyInputStreamToFile( is, new File(dir, name) );
    }

}// class
