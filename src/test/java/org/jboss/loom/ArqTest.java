package org.jboss.loom;

import org.jboss.loom.MigratorApp;
import org.jboss.loom.MigratorEngine;
import org.jboss.loom.conf.AS7Config;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.utils.AS7CliUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.ModelControllerClient;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith(Arquillian.class)
public class ArqTest {
    
    private static Configuration createTestConfig01() {
        Configuration conf = new Configuration();
        
        conf.getGlobal().getAS5Config().setDir("testdata/as5configs/01_510all");
        conf.getGlobal().getAS5Config().setProfileName("all");
        
        conf.getGlobal().getAS7Config().setDir("target/jboss-as-7.1.1.Final");
        conf.getGlobal().getAS7Config().setConfigPath("standalone/configuration/standalone.xml");
                
        return conf;
    }
    

    
    /**
     * Test of doMigration method, of class MigratorEngine.
     */
    @Test
    @RunAsClient
    public void testDoMigration( /*@ArquillianResource ManagementClient client*/ ) throws Exception {
        System.out.println( "doMigration" );
                
        Configuration conf = createTestConfig01();
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
        // AS 7.1.1 doesn't define it.
        if( as7Dir != null )
            conf.getGlobal().getAS7Config().setDir( as7Dir );
        
        MigratorApp.validateConfiguration( conf );
        
        //MigratorApp.migrate( conf );
        MigratorEngine migrator = new MigratorEngine(conf);
        migrator.doMigration();
    }
    
}// class
