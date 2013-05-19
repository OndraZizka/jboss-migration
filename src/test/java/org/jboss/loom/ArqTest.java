package org.jboss.loom;

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
    
    /**
     * Test of doMigration method, of class MigratorEngine.
     */
    @Test @Category( AS.class )
    @RunAsClient
    //@Ignore
    public void test_AS_510_all( /*@ArquillianResource ManagementClient client*/ ) throws Exception {
                
        Configuration conf = TestAppConfig.createTestConfig_AS_510_all();
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
        
        TestAppConfig.announceMigration( conf );
        
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

        Configuration conf = TestAppConfig.createTestConfig_EAP_520_production();
        AS7Config as7Config = conf.getGlobal().getAS7Config();
        
        ModelControllerClient as7client = ModelControllerClient.Factory.create(as7Config.getHost(), as7Config.getManagementPort());
        
        // Query for the server path.
        String as7Dir = AS7CliUtils.queryServerHomeDir( as7client );
        if( as7Dir != null )  // AS 7.1.1 doesn't define it.
            conf.getGlobal().getAS7Config().setDir( as7Dir );
        
        TestAppConfig.announceMigration( conf );
        
        MigratorApp.validateConfiguration( conf );
        
        MigratorEngine migrator = new MigratorEngine(conf);
        migrator.doMigration();
    }
    
        
}// class
