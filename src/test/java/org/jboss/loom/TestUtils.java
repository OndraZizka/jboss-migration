package org.jboss.loom;


import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.loom.conf.AS7Config;
import org.jboss.loom.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class TestUtils {
    private static final Logger log = LoggerFactory.getLogger( TestUtils.class );


    public static void printTestBanner() {
        System.out.println( "==========================================" );
        System.out.println( "===  " + getCallingMethodName( 1 ) + "  ===" );
        System.out.println( "==========================================" );
    }


    /**
     *  Copies mgmt-users.properties with user admin/admin to configuration/ to allow using Web console.
     */
    //@BeforeClass
    public static void copyMgmtUsersFile( AS7Config as7Config ) throws IOException {
        FileUtils.copyInputStreamToFile( ArqTest.class.getResourceAsStream( "/org/jboss/loom/mgmt-users.properties" ), new File( as7Config.getConfigDir() + "/mgmt-users.properties" ) );
    }


    public static String getCallingMethodName( int skipLevels ) {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[2 + skipLevels];
        return StringUtils.substringAfterLast( ste.getClassName(), "." ) + "." + ste.getMethodName();
    }


    public static void announceMigration( Configuration conf ) {
        String testMethod = getCallingMethodName( 1 );
        String msg = "\n" + "\n==== " + testMethod + "() " + StringUtils.repeat( "=", 94 - 8 - testMethod.length() ) + "\n  Migrating " + "\n    " + conf.getGlobal().getAS5Config().getDir() + " | " + conf.getGlobal().getAS5Config().getProfileName() + "\n  to " + "\n    " + conf.getGlobal().getAS7Config().getDir() + " | " + conf.getGlobal().getAS7Config().getConfigPath() + "\n==============================================================================================\n";
        System.out.println( msg );
    }

    

}// class
