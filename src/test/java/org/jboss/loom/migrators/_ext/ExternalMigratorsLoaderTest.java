package org.jboss.loom.migrators._ext;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.utils.Utils;
import org.junit.Test;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ExternalMigratorsLoaderTest {
    
    public ExternalMigratorsLoaderTest() {
    }


    @Test
    public void testLoadMigrators() throws Exception {
        System.out.println( "loadMigrators" );
        
        File workDir = new File("target/extMigrators/");
        FileUtils.forceMkdir( workDir );
        Utils.copyResourceToDir( ExternalMigratorsLoader.class, "TestMigrator.mig.xml", workDir );
        Utils.copyResourceToDir( ExternalMigratorsLoader.class, "TestJaxbBean.groovy",  workDir );
        
        new ExternalMigratorsLoader().loadMigrators( workDir, new GlobalConfiguration() );
    }


}// class