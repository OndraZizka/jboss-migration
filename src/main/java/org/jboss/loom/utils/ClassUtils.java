package org.jboss.loom.utils;

import groovy.lang.GroovyClassLoader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.loom.migrators._ext.ExternalMigratorsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Works with classpaths, classes, various searching etc.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ClassUtils {
    private static final Logger log = LoggerFactory.getLogger( ClassUtils.class );

    
    public static File findClassOriginFile( Class cls ){
        // Try to find the class file.
        try {
            final URL url = cls.getClassLoader().getResource( cls.getName().replace('.', '/') + ".class");
            final File file = new File( url.getFile() ); // toString()
            if( file.exists() )
                return file;
        }
        catch( Exception ex ) { }
        
        // Method 2
        try {
            URL url = cls.getProtectionDomain().getCodeSource().getLocation();
            final File file = new File( url.getFile() ); // toString()
            if( file.exists() )
                return file;
        }
        catch( Exception ex ) { }
        
        return null;
    }
    
    public static String findClassPathRootFor( Class<ExternalMigratorsLoader> cls ) {
        File clsFile = findClassOriginFile( cls );
        log.debug("Class' file: " + clsFile);
        if( clsFile == null )  return null;
        //clsFile.getName().endsWith(".class") )
        
        String clsSubPath = cls.getName().replace('.','/') + ".class";
        return StringUtils.removeEnd( clsFile.getPath(), clsSubPath );
    }


    // ======= Class utils ====== //
    public static void copyResourceToDir( Class cls, String name, File dir ) throws IOException {
        String packageDir = cls.getPackage().getName().replace( '.', '/' );
        String path = "/" + packageDir + "/" + name;
        InputStream is = GroovyClassLoader.class.getResourceAsStream( path );
        if( is == null ) {
            throw new IllegalArgumentException( "Resource not found: " + packageDir );
        }
        FileUtils.copyInputStreamToFile( is, new File( dir, name ) );
    }


    static boolean containsClass( JarFile jarFile, String classFilePath ) {
        final Enumeration<JarEntry> entries = jarFile.entries();
        while( entries.hasMoreElements() ) {
            final JarEntry entry = entries.nextElement();
            if( (!entry.isDirectory()) && entry.getName().contains( classFilePath ) ) {
                return true;
            }
        }
        return false;
    }


    /**
     *  TODO: Return a list of files.
     */
    public static File lookForJarWithClass( String className, File... dirs ) throws IOException {
        for( File dir : dirs ) {
            log.debug( "    Looking in " + dir.getPath() + " for a .jar with: " + className );
            if( !dir.isDirectory() ) {
                log.trace( "    Not a directory: " + dir.getPath() );
                continue;
            }
            Collection<File> jarFiles = FileUtils.listFiles( dir, new String[]{ "jar" }, true );
            log.trace( "    Found .jar files: " + jarFiles.size() );
            String classFilePath = className.replace( ".", "/" );
            for( File file : jarFiles ) {
                try (final JarFile jarFile = new JarFile( file )) {
                    if( containsClass( jarFile, classFilePath ) ) {
                        return file;
                    }
                }
            }
        }
        return null;
    }
    

}// class
