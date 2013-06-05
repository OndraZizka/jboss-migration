package org.jboss.loom;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.loom.conf.Configuration;

/**
 *  Base class for tests - provides util methods.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class TestAppConfig {
    
    private static File getDestServerDistDir(){
        return new File("target/as-dist");
    }
    
    public static Configuration createTestConfig( String as5config, String as5profile, String as7confPath) throws IOException {
        Configuration conf = new Configuration();
        conf.getGlobal().setTestRun( true );
        conf.getGlobal().setReportDir("target/MigrationReport");
        
        conf.getGlobal().getAS5Config().setDir("testdata/as5configs/" + as5config);
        conf.getGlobal().getAS5Config().setProfileName( as5profile );
        
        File destServerDir = new File("target/as7configs/" + as5config);
        FileUtils.copyDirectory( getDestServerDistDir(), destServerDir );
        conf.getGlobal().getAS7Config().setDir( destServerDir.getPath() );
        conf.getGlobal().getAS7Config().setConfigPath( as7confPath );
                
        return conf;
    }
    
    public static Configuration createTestConfig_AS_510_all() throws IOException {
        return createTestConfig("01_510all", "all", "standalone/configuration/standalone.xml");
    }
    
    public static Configuration createTestConfig_EAP_520( String profile ) throws IOException {
        return createTestConfig("02_EAP-520", profile, "standalone/configuration/standalone.xml");
    }

    
    public static void announceMigration( Configuration conf ){
        String testMethod = getCallingMethodName(1);
        String msg = "\n"
                + "\n==== " + testMethod + "() " + StringUtils.repeat("=", 94 - 8 - testMethod.length())
                + "\n  Migrating "
                + "\n    " + conf.getGlobal().getAS5Config().getDir() + " | " + conf.getGlobal().getAS5Config().getProfileName()
                + "\n  to "
                + "\n    " + conf.getGlobal().getAS7Config().getDir() + " | " + conf.getGlobal().getAS7Config().getConfigPath()
                + "\n==============================================================================================\n";
        System.out.println( msg );
    }
    
    public static String getCallingMethodName( int skipLevels ){
        StackTraceElement ste = Thread.currentThread().getStackTrace()[2+skipLevels];
        return StringUtils.substringAfterLast(ste.getClassName(),".") + "." + ste.getMethodName();
    }
    
}// class
