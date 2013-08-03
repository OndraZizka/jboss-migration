package org.jboss.loom.migrators._ext.el;

import java.io.File;
import java.util.List;
import org.jboss.loom.migrators._ext.*;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.loom.MigrationEngine;
import org.jboss.loom.TestUtils;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.utils.ClassUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Test the EL in various places.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith( Arquillian.class )
public class ExtElMigrationTest extends ExtMigrationTestBase {
    private static final Logger log = LoggerFactory.getLogger( ExtElMigrationTest.class );
    
    //@ArquillianResource private ManagementClient mc; // ARQ-1443


    /**
            <warning>
                workdir=${workdir} 
                srcServer.dir=${srcServer.dir}
                destServer.dir=${destServer.dir}
                action.class.name=${action.class.name}
                userVarTest=${userVarTest}
            </warning>
     */
    @Ignore
    @Test @RunAsClient
    public void testElInWarning() throws Exception {
        TestUtils.printTestBanner();
        
        MigrationEngine migEngine = 
        doTest( "ElInWarningTest", null, new TestPreparation() {
            @Override public void prepareDir( File dir ) throws Exception {
                ClassUtils.copyResourceToDir( this.getClass(), "userVarTest.xml", dir );
            }
            @Override public void prepareConfig( Configuration conf ) throws Exception {
                conf.getGlobal().setUserVar("userVarTest", "userVarTest");
            }
        } );
        
        final List<IMigrationAction> actions = migEngine.getContext().getActions();
        
        Assert.assertEquals("1 action created", 1, actions.size());
        final List<String> warnings = migEngine.getContext().getActions().get(0).getWarnings();
        Assert.assertEquals("1 warning in action", 1, warnings.size());
        
        final String warn = warnings.get(0);
        System.out.println("The warning produced:\n    " + warn );
        
        for( String str : new String[]{
            "workdir=" + new File(".").getAbsolutePath(),
            "srcServer.dir=" + migEngine.getConfig().getGlobal().getSourceServerDir(),
            "destServer.dir=" + migEngine.getConfig().getGlobal().getTargetServerDir(),
            "action.class.simpleName=" + "ManualAction",
            "userVarTest=userVarTest"
        }){
            Assert.assertTrue("Warning contains " + str, warn.contains(str));
        }
    }
    
    
    /**
     */
    @Test @RunAsClient
    public void testElInAction() throws Exception {
        TestUtils.printTestBanner();
        
        MigrationEngine migEngine = 
        doTest( "ElInActionTest", null, new TestPreparation() {
            @Override public void prepareDir( File dir ) throws Exception {
                ClassUtils.copyResourceToDir( this.getClass(), "userVarTest.xml", dir );
            }
            @Override public void prepareConfig( Configuration conf ) throws Exception {
                conf.getGlobal().setUserVar("userVarTest", "userVarTest");
            }
        } );
        
        final List<IMigrationAction> actions = migEngine.getContext().getActions();
        
        Assert.assertEquals("1 action created", 1, actions.size());
        final List<String> warnings = migEngine.getContext().getActions().get(0).getWarnings();
        Assert.assertEquals("1 warning in action", 1, warnings.size());
        
        final String warn = warnings.get(0);
        System.out.println("The warning produced:\n    " + warn );
        
        //command=BatchedCommandWithAction{/system-property=foo:add(goo=${goo[0]}, action=${action.class.simpleName}, userVarTest=${userVarTest}); action: CliCommandAction{/system-property=foo:add(goo=${goo[0]}, action=${action.class.simpleName}, userVarTest=${userVarTest}); ifExists=WARN, todo=null}}
        for( String str : new String[]{
            "command=" + "/system-property=foo:add(goo=fooValue, userVarTest=userVarTest)"
        }){
            //Assert.assertTrue("Warning contains " + str, warn.contains(str));
            Assert.assertEquals("Warning equals ", str, warn);
        }
    }

}// class
