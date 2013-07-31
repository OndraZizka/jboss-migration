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
    protected MigrationEngine doTest( String migName, File dir, DirPreparation prep ) throws IOException, UnknownHostException, MigrationException, Exception {
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
    public interface DirPreparation {
        void prepareDir( File dir ) throws Exception;

        static DirPreparation NOOP = new DirPreparation() {
            @Override public void prepareDir( File dir ) { }
        };
    }    
    
}// class

