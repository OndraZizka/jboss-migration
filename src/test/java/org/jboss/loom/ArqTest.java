package org.jboss.loom;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.loom.categories.AS;
import org.jboss.loom.categories.EAP;
import org.jboss.loom.conf.AS7Config;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.ConfigurationValidator;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.utils.as7.AS7CliUtils;
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
    @Ignore
    public void test_AS_510_all( /*@ArquillianResource ManagementClient client*/ ) throws Exception {
                
        Configuration conf = TestAppConfig.createTestConfig_AS_510_all();
        AS7Config as7Config = conf.getGlobal().getAS7Config();
        
        // Set the Mgmt host & port from the client;
        //as7Config.setHost( client.getMgmtAddress() );
        //as7Config.setManagementPort( client.getMgmtPort() );
        //ModelControllerClient as7client = client.getControllerClient();
        /*
           This fails for some reason:
            java.lang.RuntimeException: Provider for type class org.jboss.as.arquillian.container.ManagementClient returned a null value: org.jboss.as.arquillian.container.ManagementClientProvider@4aee4b87
                at org.jboss.arquillian.test.impl.enricher.resource.ArquillianResourceTestEnricher.lookup(ArquillianResourceTestEnricher.java:115)
                at org.jboss.arquillian.test.impl.enricher.resource.ArquillianResourceTestEnricher.resolve(ArquillianResourceTestEnricher.java:91)        
                 
           Let's resort to default values.
        */
        
        
        TestAppConfig.updateAS7ConfAsPerServerMgmtInfo( as7Config );
        
        TestUtils.announceMigration( conf );
        ConfigurationValidator.validate( conf );
        MigrationEngine migrator = new MigrationEngine(conf);
        migrator.doMigration();
    }
    
    
    private void runEap520( String profile ) throws Exception {
        Configuration conf = TestAppConfig.createTestConfig_EAP_520(profile);
        TestAppConfig.updateAS7ConfAsPerServerMgmtInfo( conf.getGlobal().getAS7Config() );
        TestUtils.copyMgmtUsersFile( conf.getGlobal().getAS7Config() );
        
        TestUtils.announceMigration( conf );
        ConfigurationValidator.validate( conf );
        MigrationEngine migrator = new MigrationEngine(conf);
        migrator.doMigration();
    }
    
    /**
     *   EAP 5.2.0 production.
     */
    @Test @Category( EAP.class )
    @RunAsClient
    public void test_EAP_520_production( ) throws Exception {
        runEap520("production");
    }
    
    /**
     *   EAP 5.2.0 all.
     */
    @Test @Category( EAP.class )
    @RunAsClient
    public void test_EAP_520_all( ) throws Exception {
        runEap520("all");
    }

    /**
     *   EAP 5.2.0 default.
     */
    @Test @Category( EAP.class )
    @RunAsClient
    public void test_EAP_520_default( ) throws Exception {
        runEap520("default");
    }

    /**
     *   EAP 5.2.0 standard.
     */
    @Test @Category( EAP.class )
    @RunAsClient
    public void test_EAP_520_standard( ) throws Exception {
        runEap520("standard");
    }

    /**
     *   EAP 5.2.0 web.
     */
    @Test @Category( EAP.class )
    @RunAsClient
    public void test_EAP_520_web( ) throws Exception {
        runEap520("web");
    }

    
    
    /**
     *   EAP 5.2.0, dry run.
     */
    @Test @Category( EAP.class )
    @RunAsClient
    public void testDryRun_EAP_520_production( ) throws Exception {

        Configuration conf = TestAppConfig.createTestConfig_EAP_520("production");
        conf.getGlobal().setDryRun( true );
        TestAppConfig.updateAS7ConfAsPerServerMgmtInfo( conf.getGlobal().getAS7Config() );
        
        TestUtils.announceMigration( conf );
        ConfigurationValidator.validate( conf );
        MigrationEngine migrator = new MigrationEngine(conf);
        migrator.doMigration();
    }
    
    /**
     *   EAP 5.2.0, with JBOSS_HOME (which should be ignored) set.
     */
    @Test @Category( EAP.class )
    @RunAsClient
    @Ignore("JBOSS_HOME is an env var, not sys prop.")
    public void testJBOSS_HOME() throws Exception {

        Configuration conf = TestAppConfig.createTestConfig_EAP_520("production");
        System.setProperty("JBOSS_HOME", "/foo/bar");
        TestAppConfig.updateAS7ConfAsPerServerMgmtInfo( conf.getGlobal().getAS7Config() );
        
        TestUtils.announceMigration( conf );
        ConfigurationValidator.validate( conf );
        MigrationEngine migrator = new MigrationEngine(conf);
        migrator.doMigration();
    }
        
}// class
