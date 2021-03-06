package org.jboss.loom.migrators._ext;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.loom.MigrationEngine;
import org.jboss.loom.TestAppConfig;
import org.jboss.loom.TestUtils;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.ConfigurationValidator;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.utils.ClassUtils;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith( Arquillian.class )
public class ExtMigrationTestBase extends ExternalMigratorsTestEnv {
    private static final Logger log = LoggerFactory.getLogger( ExtMigrationTestBase.class );
    
    //@ArquillianResource private ManagementClient mc; // ARQ-1443


    
    /**
     *  Test itself
     */
    protected MigrationEngine doTest( String migName, File dir, TestPreparation prep ) throws IOException, UnknownHostException, MigrationException, Exception {
        System.out.println( "-----------------------------" );
        System.out.println( "---  "   + migName +   "  ---" );
        System.out.println( "-----------------------------" );
        
        // Create temp dir if not given.
        if( dir == null )
            dir = Files.createTempDirectory("ExtMigr-" + migName + "-").toFile();
        
        // Put the .mig.xml in the dir.
        putMigratorFile( migName, dir );
        
        // Callback
        prep.prepareDir( dir );
        
        // Migration Configuration
        Configuration conf = createSingleExtMigTestConf( migName );
        conf.getGlobal().setExternalMigratorsDir( dir.getPath() );
        
        // Callback
        prep.prepareConfig( conf );
        
        TestUtils.announceMigration( conf );
        ConfigurationValidator.validate( conf );
        
        // Migrate.
        try {
            MigrationEngine migEngine = new MigrationEngine(conf);
            migEngine.doMigration();
            return migEngine;
        } catch ( Throwable ex ){
            ex.printStackTrace();
            throw ex;
        }
    }


    private void putMigratorFile( String name, File dir ) throws IOException {
        String file = name + ".mig.xml";
        ClassUtils.copyResourceToDir( this.getClass(), file, dir ); // Subclass.
    }


    private Configuration createSingleExtMigTestConf( String migName ) throws IOException, UnknownHostException, MigrationException {
        Configuration conf = TestAppConfig.createTestConfig_EAP_520("production");
        TestAppConfig.updateAS7ConfAsPerServerMgmtInfo( conf.getGlobal().getAS7Config() );
        
        // Set external migrators dir.
        conf.getGlobal().setExternalMigratorsDir( workDir.getPath() );
        conf.getGlobal().addOnlyMigrator( migName );
        return conf;
    }
    
    
    
    /**
     *  The intent was to share one preparation for multiple tests, but that doesn't happen much.
     */
    public interface TestPreparation {
        void prepareDir( File dir ) throws Exception;
        void prepareConfig( Configuration conf ) throws Exception;

        static TestPreparation NOOP = new TestPreparation() {
            @Override public void prepareDir( File dir ) { }
            @Override public void prepareConfig( Configuration conf ) { }
        };
        
        public static class CopyResourcesPreparation implements TestPreparation {
            private final String[] paths;
            private final Class cls;
            
            public CopyResourcesPreparation( Class cls, String[] paths ){
                this.cls = cls;
                this.paths = paths;
            }

            @Override public void prepareDir( File dir ) throws IOException {
                for( String path : paths ) {
                    ClassUtils.copyResourceToDir( this.cls, path, dir );
                }
            }
            
            @Override public void prepareConfig( Configuration conf ) { }
        }
    }    
    
}// class

