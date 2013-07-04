package org.jboss.loom.migrators._ext;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.loom.MigrationEngine;
import org.jboss.loom.TestAppConfig;
import org.jboss.loom.TestUtils;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.ConfigurationValidator;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *  Tests the external migrator loader.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith( Arquillian.class )
public class ExternalMigratorsLoaderTest extends ExternalMigratorsTestEnv {

    @Ignore
    @Test @RunAsClient
    public void testExternalMigrator() throws Exception {
        TestUtils.printTestBanner();

        Configuration conf = TestAppConfig.createTestConfig_EAP_520("production");
        TestAppConfig.updateAS7ConfAsPerServerMgmtInfo( conf.getGlobal().getAS7Config() );        
        
        // Set external migrators dir.
        conf.getGlobal().setExternalMigratorsDir( workDir.getPath() );
        
        TestUtils.announceMigration( conf );
        ConfigurationValidator.validate( conf );
        MigrationEngine migrator = new MigrationEngine(conf);
        migrator.doMigration();
    }
    

}// class