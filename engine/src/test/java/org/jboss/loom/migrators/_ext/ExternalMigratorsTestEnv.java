package org.jboss.loom.migrators._ext;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.jboss.loom.utils.ClassUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Base class to prepare external migrators test environment (copies files etc.).
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ExternalMigratorsTestEnv {
    private static final Logger log = LoggerFactory.getLogger( ExternalMigratorsTestEnv.class );

    protected static File workDir;
    
    @BeforeClass
    public static void copyTestExtMigratorFiles() throws IOException {
        workDir = new File("target/extMigrators/");
        FileUtils.forceMkdir( workDir );
        ClassUtils.copyResourceToDir( ExternalMigratorsTestEnv.class, "res/TestMigrator.mig.xml", workDir );
        ClassUtils.copyResourceToDir( ExternalMigratorsTestEnv.class, "res/TestJaxbBean.groovy",  workDir );
    }
    
    @AfterClass
    public static void deleteTestExtMigratorFiles() throws IOException {
        FileUtils.forceDelete( workDir );
    }

}// class
