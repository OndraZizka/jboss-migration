package org.jboss.loom.migrators._ext;

import java.io.File;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.migrators.IMigratorFilter;
import org.jboss.loom.utils.ClassUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ExternalMigratorsLoaderTest {
    
    public ExternalMigratorsLoaderTest() {
    }

    @Test
    public void testLoadTestMigrator() throws Throwable {
        try {
            File workDir = new File("target/extMigrators/");
            FileUtils.forceMkdir( workDir );
            ClassUtils.copyResourceToDir( ExternalMigratorsLoaderTest.class, "res/TestMigrator.mig.xml", workDir );
            ClassUtils.copyResourceToDir( ExternalMigratorsLoaderTest.class, "res/TestJaxbBean.groovy",  workDir );
            
            Map<Class<? extends DefinitionBasedMigrator>, DefinitionBasedMigrator> migs
                = new ExternalMigratorsLoader().loadMigrators( workDir, new IMigratorFilter.All(), new GlobalConfiguration() );
            
            assertEquals("1 migrator loaded", 1, migs.size());
            DefinitionBasedMigrator mig = migs.values().iterator().next();
            assertEquals("1 migrator loaded", "MailExtMigrator", mig.getClass().getName());
        }
        catch( Throwable ex ){
            ex.printStackTrace();
            throw ex;
        }
    }
    
}