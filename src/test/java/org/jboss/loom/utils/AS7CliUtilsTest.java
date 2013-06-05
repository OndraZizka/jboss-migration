package org.jboss.loom.utils;

import org.jboss.loom.utils.as7.AS7CliUtils;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class AS7CliUtilsTest {

    @Test
    public void testFormatCommand(){
        
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
    
}// class
