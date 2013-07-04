package org.jboss.loom.migrators._ext;


import java.util.Map;
import org.jboss.loom.TestUtils;
import org.jboss.loom.conf.GlobalConfiguration;
import static org.jboss.loom.migrators._ext.ExternalMigratorsTestEnv.workDir;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ExternalMigratorsDefReadTest extends ExternalMigratorsTestEnv {
    private static final Logger log = LoggerFactory.getLogger( ExternalMigratorsDefReadTest.class );

    @Test
    public void testLoadMigrators() throws Exception {
        TestUtils.printTestBanner();
        
        Map<Class<? extends DefinitionBasedMigrator>, DefinitionBasedMigrator> migs
                = new ExternalMigratorsLoader().loadMigrators( workDir, new GlobalConfiguration() );
        
        Assert.assertEquals("1 migrator was loaded", 1, migs.size() );
        
        for( Map.Entry<Class<? extends DefinitionBasedMigrator>, DefinitionBasedMigrator> entry : migs.entrySet() ) {
            Class<? extends DefinitionBasedMigrator> cls = entry.getKey();
            DefinitionBasedMigrator mig = entry.getValue();
            System.out.println( String.format("  Loaded migrator %s: %s", cls.getName(), mig.toString() ) );
            for( MigratorDefinition.ActionDef actDef : mig.getDescriptor().actionDefs ) {
                System.out.println( String.format("    ActionDef: " + actDef.toString()) );
            }
        }
    }
    

}// class
