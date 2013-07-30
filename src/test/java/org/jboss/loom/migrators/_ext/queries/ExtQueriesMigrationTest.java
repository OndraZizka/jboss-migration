package org.jboss.loom.migrators._ext.queries;

import java.io.File;
import java.util.List;
import org.jboss.loom.migrators._ext.*;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.loom.MigrationEngine;
import org.jboss.loom.TestUtils;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.utils.ClassUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Test the queries defined in external files.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith( Arquillian.class )
public class ExtQueriesMigrationTest extends ExtMigrationTestBase {
    private static final Logger log = LoggerFactory.getLogger( ExtQueriesMigrationTest.class );
    
    //@ArquillianResource private ManagementClient mc; // ARQ-1443


    /**
        <a>
            <foo name="foo1" value="fooValue"/>
            <bar name="bar1">
                <value>barValue</value>
            </bar> 
        </a>
     */
    @Test @RunAsClient
    public void testXmlQuery() throws Exception {
        TestUtils.printTestBanner();
        
        MigrationEngine migEngine = 
        doTest( "XmlQueryTest", null, new DirPreparation() {
            @Override public void prepareDir( File dir ) throws Exception {
                ClassUtils.copyResourceToDir( this.getClass(), "XmlQueryTest.xml", dir );
            }
        } );
        
        
        final List<IMigrationAction> actions = migEngine.getContext().getActions();
        
        Assert.assertEquals("1 action created", 1, actions.size());
        final List<String> warnings = migEngine.getContext().getActions().get(0).getWarnings();
        Assert.assertEquals("1 warning in action", 1, warnings.size());
        Assert.assertTrue("Warning contains fooValue", warnings.get(0).contains("fooValue"));
        Assert.assertTrue("Warning contains barValue", warnings.get(0).contains("barValue"));
    }
    
    /**
        foo=bar
        bar=baz
        baz=212
     */
    @Test @RunAsClient
    public void testPropsQuery() throws Exception {
        TestUtils.printTestBanner();
        
        MigrationEngine migEngine = 
        doTest( "PropertiesQueryTest", null, new DirPreparation() {
            @Override public void prepareDir( File dir ) throws Exception {
                ClassUtils.copyResourceToDir( this.getClass(), "PropertiesQueryTest.properties", dir );
            }
        } );
        
        final List<IMigrationAction> actions = migEngine.getContext().getActions();
        
        Assert.assertEquals("1 action created", 1, actions.size());
        final List<String> warnings = migEngine.getContext().getActions().get(0).getWarnings();
        Assert.assertEquals("1 warning in action", 1, warnings.size());
        Assert.assertTrue("Warning contains foo=bar", warnings.get(0).contains("foo=bar"));
        Assert.assertTrue("Warning contains bar=baz", warnings.get(0).contains("bar=baz"));
        Assert.assertTrue("Warning contains baz=212", warnings.get(0).contains("baz=212"));
    }

}// class
