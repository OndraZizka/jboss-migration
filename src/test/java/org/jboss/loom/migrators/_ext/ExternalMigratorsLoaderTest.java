package org.jboss.loom.migrators._ext;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.utils.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ExternalMigratorsLoaderTest {
    
    private File workDir;
    
    @BeforeClass
    private void copyTestExtMigratorFiles() throws IOException {
        this.workDir = new File("target/extMigrators/");
        FileUtils.forceMkdir( workDir );
        Utils.copyResourceToDir( ExternalMigratorsLoader.class, "TestMigrator.mig.xml", workDir );
        Utils.copyResourceToDir( ExternalMigratorsLoader.class, "TestJaxbBean.groovy",  workDir );
    }
    
    @AfterClass
    private void deleteTestExtMigratorFiles() throws IOException {
        FileUtils.forceDelete( workDir );
    }

    @Test
    public void testLoadMigrators() throws Exception {
        System.out.println( "loadMigrators" );
        
        Map<Class<? extends DefinitionBasedMigrator>, DefinitionBasedMigrator> migs
                = new ExternalMigratorsLoader().loadMigrators( this.workDir, new GlobalConfiguration() );
        
        for( Map.Entry<Class<? extends DefinitionBasedMigrator>, DefinitionBasedMigrator> entry : migs.entrySet() ) {
            Class<? extends DefinitionBasedMigrator> cls = entry.getKey();
            DefinitionBasedMigrator mig = entry.getValue();
            System.out.println( String.format("  Loaded migrator %s: %s", cls.getName(), mig.toString() ) );
        }
    }

}// class