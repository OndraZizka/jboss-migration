package org.jboss.loom;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
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
    
    public static Configuration createTestConfig_EAP_520_production() throws IOException {
        return createTestConfig("02_EAP-520-prod", "production", "standalone/configuration/standalone.xml");
    }

    
    public static void announceMigration( Configuration conf ){
        String msg = "\n\n"
                + "==========================================================="
                + "  Migrating "
                + "   " + conf.getGlobal().getAS5Config().getDir() + " | " + conf.getGlobal().getAS5Config().getProfileName()
                + "  to "
                + "   " + conf.getGlobal().getAS7Config().getDir() + " | " + conf.getGlobal().getAS7Config().getConfigPath()
                + "===========================================================\n";
        System.out.println( msg );
    }
    
}// class
