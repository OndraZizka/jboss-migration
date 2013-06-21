package org.jboss.loom.utils;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class UtilsAS5 {
    private static final Logger log = LoggerFactory.getLogger( UtilsAS5.class );


    // ======= File utils ====== //
    /**
     * Utils class for finding name of jar file containing class from logging configuration.
     *
     * @param className  name of the class which must be found
     * @param dirAS5     AS5 home dir
     * @param profileAS5 name of AS5 profile
     * @return name of jar file which contains given class
     * @throws FileNotFoundException if the jar file is not found
     *     <p/>
     *     TODO: This would cause false positives - e.g. class = org.Foo triggered by org/Foo/Blah.class .
     */
    public static File findJarFileWithClass( String className, String dirAS5, String profileAS5 ) throws FileNotFoundException, IOException {
        
        File jar = Utils.lookForJarWithClass( className, 
                Utils.createPath( dirAS5, "server", profileAS5, "lib" ),
                Utils.createPath( dirAS5, "common/lib" ));
        if( jar != null )
            return jar;
        throw new FileNotFoundException( "Cannot find jar file which contains class: " + className );
    }
    

}// class
