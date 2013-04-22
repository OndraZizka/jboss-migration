package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.conf.Configuration;
import cz.muni.fi.jboss.migration.utils.AS7CliUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
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
    public void testDoMigration( @ArquillianResource ManagementClient client ) throws Exception {
        System.out.println( "doMigration" );
                
        Configuration conf = createTestConfig01();
        
        // Set the Mgmt host & port from the client;
        conf.getGlobal().getAS7Config().setHost( client.getMgmtAddress() );
        conf.getGlobal().getAS7Config().setManagementPort( client.getMgmtPort() );
        
        // Then query for the server path.
        String as7Dir = AS7CliUtils.queryServerHomeDir(client.getControllerClient() );
        conf.getGlobal().getAS7Config().setDir( as7Dir );
        
        MigratorApp.validateConfiguration( conf );
        
        //MigratorApp.migrate( conf );
        MigratorEngine migrator = new MigratorEngine(conf);
        migrator.doMigration();
    }
    
}// class
