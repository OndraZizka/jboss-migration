package org.jboss.loom.migrators._ext.nested;

import org.jboss.loom.migrators._ext.actions.*;
import java.util.List;
import org.jboss.loom.migrators._ext.*;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.loom.MigrationEngine;
import org.jboss.loom.TestUtils;
import org.jboss.loom.actions.IMigrationAction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Test the actions defined in external files.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith( Arquillian.class )
public class ExtForEachMigrationTest extends ExtMigrationTestBase {
    private static final Logger log = LoggerFactory.getLogger( ExtActionsMigrationTest.class );
    
    //@ArquillianResource private ManagementClient mc; // ARQ-1443


    /**
            <forEach query="foo" var="configFragment">
                <action type="manual">
                    <warning>
                        configFragment=${configFragment.value}
                        workdir=${workdir} 
                        srcServer.dir=${srcServer.dir}
                        destServer.dir=${destServer.dir}
                        action.class.name=${action.class.name}
                        userVarTest=${userVarTest}
                    </warning>
                </action>
            </forEach>
     */
    @Test @RunAsClient
    public void testForEachWithAction() throws Exception {
        TestUtils.printTestBanner();
        
        MigrationEngine migEngine = 
        doTest( "ForEachWithActionTest", null, new TestPreparation.CopyResourcesPreparation( this.getClass(), new String[]{"foo.xml"}) );
        
        final List<IMigrationAction> actions = migEngine.getContext().getActions();
        
        Assert.assertEquals("2 actions created", 2, actions.size());
        final List<String> warnings = migEngine.getContext().getActions().get(0).getWarnings();
        Assert.assertEquals("1 warning in action 1", 1, warnings.size());
        
        final String warn = warnings.get(0);
        System.out.println("The warning in action 1:\n    " + warn );
        
        for( String str : new String[]{
            "configFragment=" + "fooValue",
            "srcServer.dir=" + migEngine.getConfig().getGlobal().getSourceServerDir(),
            "destServer.dir=" + migEngine.getConfig().getGlobal().getTargetServerDir(),
            "action.class.simpleName=" + "ManualAction",
            "userVarTest=userVarTest"
        }){
            Assert.assertTrue("Warning contains " + str, warn.contains(str));
        }
    }
    
    
}// class
