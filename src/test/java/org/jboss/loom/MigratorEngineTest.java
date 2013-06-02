package org.jboss.loom;

import org.jboss.loom.MigratorApp;
import org.jboss.loom.MigrationEngine;
import org.jboss.loom.conf.Configuration;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import org.jboss.loom.conf.ConfigurationValidator;

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
    public void setUp() throws IOException {
        FileUtils.copyDirectory(new File("../AS-7.1.3"), new File("target/as7copy"));
    }
    

    @After
    public void tearDown() throws IOException {
        FileUtils.moveDirectory( new File("target/as7copy"), new File("target/as7result-01") );
    }

    
    
    private static Configuration createTestConfig01() {
        Configuration conf = new Configuration();
        
        conf.getGlobal().getAS5Config().setDir("testdata/as5configs/01_510all");
        conf.getGlobal().getAS5Config().setProfileName("all");
        
        conf.getGlobal().getAS7Config().setDir("target/as7copy");
        conf.getGlobal().getAS7Config().setConfigPath("standalone/configuration/standalone.xml");
                
        return conf;
    }
    

    
    /**
     * Test of doMigration method, of class MigratorEngine.
     */
    @Test
    @Ignore
    public void testDoMigration() throws Exception {
        System.out.println( "doMigration" );
        
        Configuration conf = createTestConfig01();
        ConfigurationValidator.validate( conf );
        
        //MigratorApp.migrate( conf );
        MigrationEngine migrator = new MigrationEngine(conf);
        migrator.doMigration();
    }


}