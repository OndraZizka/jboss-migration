package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.conf.Configuration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class MigratorEngineTest {
    
    public MigratorEngineTest() {
    }
    

    @BeforeClass
    public static void setUpClass() {
    }
    

    @AfterClass
    public static void tearDownClass() {
    }
    

    @Before
    public void setUp() {
    }
    

    @After
    public void tearDown() {
    }


    /**
     * Test of doMigration method, of class MigratorEngine.
     */
    @Test
    public void testDoMigration() throws Exception {
        System.out.println( "doMigration" );
        
        Configuration conf = createTestConfig01();
        MigratorApp.validateConfiguration( conf );
        
        MigratorApp.migrate( conf );
    }


    private Configuration createTestConfig01() {
        Configuration conf = new Configuration();
        
        conf.getGlobal().getAS5Config().setDir("testdata/as5configs/01");
        conf.getGlobal().getAS5Config().setProfileName("default");
        conf.getGlobal().getAS7Config().setDir("target/as7config-result");
        conf.getGlobal().getAS7Config().setConfigPath("standalone");
                
        return conf;
    }
   
}