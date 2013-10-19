package org.jboss.loom.utils;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.loom.utils.as7.AS7CliUtils;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.loom.TestAppConfig;
import org.jboss.loom.conf.AS7Config;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith( Arquillian.class )
public class AS7CliUtilsTest {

    @RunAsClient
    @Test public void testFormatCommand(){
        ModelNode cmd = new ModelNode();
        ModelNode addr = cmd.get(ClientConstants.OP_ADDR);
        addr.add("foo", "a");
        addr.add("bar", "b");
        cmd.get(ClientConstants.OP).set("do-something");
        cmd.get("param1").set("val1");
        cmd.get("param2").set("val2");
        
        String str = AS7CliUtils.formatCommand( cmd );
        
        Assert.assertEquals( "/foo=a/bar=b:do-something(param1=\"val1\",param2=\"val2\")", str);
    }
    
    /**
     *  parse() -> format() gives the same as original CLI command.
     */
    @RunAsClient
    @Test public void testParseAndFormatCommand(){
        final String command = "/foo=a/bar=b:do-something(param1=\"val1\",param2=\"val2\")";
        final ModelNode modalNode = AS7CliUtils.parseCommand( command );
        final String command2 = AS7CliUtils.formatCommand( modalNode );
        
        Assert.assertEquals( command, command2 );
    }
    
    /**
     *  Check that "com.h2database.h2" -> "h2".
     */
    @Test public void testFindJdbcDriverUsingModule() throws Exception {
        System.out.println( "findJdbcDriverUsingModule" );
        
        AS7Config conf = TestAppConfig.createTestConfig_EAP_520("production").getGlobal().getAS7Config();
        ModelControllerClient as7Client = ModelControllerClient.Factory.create(conf.getHost(), conf.getManagementPort());
        
        String expResult = "h2";
        String result = AS7CliUtils.findJdbcDriverUsingModule( "com.h2database.h2", as7Client );
        
        assertEquals( expResult, result );
    }
    
    
}// class
