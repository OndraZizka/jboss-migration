package org.jboss.loom.migrators._ext;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.loom.MigrationEngine;
import org.jboss.loom.TestAppConfig;
import org.jboss.loom.TestUtils;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.ConfigurationValidator;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.utils.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith( Arquillian.class )
public class ExternalMigratorsLoaderTest {
    
    private static File workDir;
    
    @BeforeClass
    public static void copyTestExtMigratorFiles() throws IOException {
        workDir = new File("target/extMigrators/");
        FileUtils.forceMkdir( workDir );
        Utils.copyResourceToDir( ExternalMigratorsLoader.class, "TestMigrator.mig.xml", workDir );
        Utils.copyResourceToDir( ExternalMigratorsLoader.class, "TestJaxbBean.groovy",  workDir );
    }
    
    @AfterClass
    public static void deleteTestExtMigratorFiles() throws IOException {
        FileUtils.forceDelete( workDir );
    }

    @Ignore
    @Test @RunAsClient
    public void testLoadMigrators() throws Exception {
        TestUtils.printTestBanner();
        
        Map<Class<? extends DefinitionBasedMigrator>, DefinitionBasedMigrator> migs
                = new ExternalMigratorsLoader().loadMigrators( workDir, new GlobalConfiguration() );
        
        for( Map.Entry<Class<? extends DefinitionBasedMigrator>, DefinitionBasedMigrator> entry : migs.entrySet() ) {
            Class<? extends DefinitionBasedMigrator> cls = entry.getKey();
            DefinitionBasedMigrator mig = entry.getValue();
            System.out.println( String.format("  Loaded migrator %s: %s", cls.getName(), mig.toString() ) );
        }
    }
    
    @Test @RunAsClient
    public void testExternalMigrator() throws Exception {
        TestUtils.printTestBanner();

        Configuration conf = TestAppConfig.createTestConfig_EAP_520("production");
        
        // Set external migrators dir.
        conf.getGlobal().setExternalMigratorsDir( workDir.getPath() );
        
        TestAppConfig.announceMigration( conf );
        ConfigurationValidator.validate( conf );
        MigrationEngine migrator = new MigrationEngine(conf);
        migrator.doMigration();
    }
    

}// class