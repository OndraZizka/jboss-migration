package org.jboss.loom;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.jboss.loom.conf.AS7Config;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.utils.AS7CliUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.loom.categories.AS;
import org.jboss.loom.categories.EAP;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith(Arquillian.class)
public class ArqTest {
    
    private static File getDestServerDir(){
        return new File("target/jboss-as-7.1.1.Final");
        // new File("target/jboss-eap-6.2.0.Beta1");
    }
    
    private static Configuration createTestConfig_AS_510_all() throws IOException {
        Configuration conf = new Configuration();
        
        conf.getGlobal().getAS5Config().setDir("testdata/as5configs/01_510all");
        conf.getGlobal().getAS5Config().setProfileName("all");
        
        FileUtils.copyDirectory( getDestServerDir(), new File("target/as7configs/01_510all"));
        conf.getGlobal().getAS7Config().setDir("target/jboss-as-7.1.1.Final");
        conf.getGlobal().getAS7Config().setConfigPath("standalone/configuration/standalone.xml");
                
        return conf;
    }
    
    private static Configuration createTestConfig_EAP_520_production() throws IOException {
        Configuration conf = new Configuration();
        
        conf.getGlobal().getAS5Config().setDir("testdata/as5configs/02_EAP-520-prod");
        conf.getGlobal().getAS5Config().setProfileName("production");
        
        FileUtils.copyDirectory( getDestServerDir(), new File("target/as7configs/02_EAP-520-prod"));
        conf.getGlobal().getAS7Config().setDir("target/as5configs/02_EAP-520-prod");
        conf.getGlobal().getAS7Config().setConfigPath("standalone/configuration/standalone.xml");
                
        return conf;
    }
    

    
    /**
     * Test of doMigration method, of class MigratorEngine.
     */
    @Test @Category( AS.class )
    @RunAsClient
    //@Ignore
    public void test_AS_510_all( /*@ArquillianResource ManagementClient client*/ ) throws Exception {
        System.out.println( "doMigration" );
                
        Configuration conf = createTestConfig_AS_510_all();
        AS7Config as7Config = conf.getGlobal().getAS7Config();
        
        // Set the Mgmt host & port from the client;
        //as7Config.setHost( client.getMgmtAddress() );
        //as7Config.setManagementPort( client.getMgmtPort() );
        /*
           This fails for some reason:
            java.lang.RuntimeException: Provider for type class org.jboss.as.arquillian.container.ManagementClient returned a null value: org.jboss.as.arquillian.container.ManagementClientProvider@4aee4b87
                at org.jboss.arquillian.test.impl.enricher.resource.ArquillianResourceTestEnricher.lookup(ArquillianResourceTestEnricher.java:115)
                at org.jboss.arquillian.test.impl.enricher.resource.ArquillianResourceTestEnricher.resolve(ArquillianResourceTestEnricher.java:91)        
                 
           Let's resort to default values.
        */
        
        //ModelControllerClient as7client = client.getControllerClient();
        ModelControllerClient as7client = ModelControllerClient.Factory.create(as7Config.getHost(), as7Config.getManagementPort());
        
        // Then query for the server path.
        String as7Dir = AS7CliUtils.queryServerHomeDir( as7client );
        if( as7Dir != null )  // AS 7.1.1 doesn't define it.
            conf.getGlobal().getAS7Config().setDir( as7Dir );
        
        announceMigration( conf );
        
        MigratorApp.validateConfiguration( conf );
        
        //MigratorApp.migrate( conf );
        MigratorEngine migrator = new MigratorEngine(conf);
        migrator.doMigration();
    }
    
    
    /**
     *   With EAP 5.2.0. config.
     */
    @Test @Category( EAP.class )
    @RunAsClient
    @Ignore
    public void test_EAP_520_production( ) throws Exception {
        System.out.println( "doMigration" );
                
        Configuration conf = createTestConfig_EAP_520_production();
        AS7Config as7Config = conf.getGlobal().getAS7Config();
        
        ModelControllerClient as7client = ModelControllerClient.Factory.create(as7Config.getHost(), as7Config.getManagementPort());
        
        // Query for the server path.
        String as7Dir = AS7CliUtils.queryServerHomeDir( as7client );
        if( as7Dir != null )  // AS 7.1.1 doesn't define it.
            conf.getGlobal().getAS7Config().setDir( as7Dir );
        
        announceMigration( conf );
        
        MigratorApp.validateConfiguration( conf );
        
        MigratorEngine migrator = new MigratorEngine(conf);
        migrator.doMigration();
    }
    
    
    private static void announceMigration( Configuration conf ){
        String msg = "\n\n"
                + "==========================================================="
                + "  Migrating "
                + "  " + conf.getGlobal().getAS5Config().getDir() + " | " + conf.getGlobal().getAS5Config().getProfileName()
                + "  to "
                + "   " + conf.getGlobal().getAS7Config().getDir() + " | " + conf.getGlobal().getAS7Config().getConfigPath()
                + "===========================================================\n";
        System.out.println( msg );
    }
    
}// class
