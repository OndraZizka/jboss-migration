package org.jboss.loom;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import org.apache.commons.io.FileUtils;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.loom.conf.AS7Config;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.utils.as7.AS7CliUtils;

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

    

    // -- Util methods --
    public static void updateAS7ConfAsPerServerMgmtInfo( AS7Config conf ) throws UnknownHostException, MigrationException {
        ModelControllerClient as7client = ModelControllerClient.Factory.create( conf.getHost(), conf.getManagementPort() );
        updateAS7ConfAsPerServerMgmtInfo( conf, as7client );
    }

    public static void updateAS7ConfAsPerServerMgmtInfo( AS7Config conf, ModelControllerClient as7client ) throws UnknownHostException, MigrationException {
        String as7Dir = AS7CliUtils.queryServerHomeDir( as7client );
        if( as7Dir != null ) {
            conf.setDir( as7Dir );
        }
    }
    
}// class
