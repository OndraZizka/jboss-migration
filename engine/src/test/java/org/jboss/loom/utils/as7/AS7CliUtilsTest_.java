/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.loom.utils.as7;

import java.io.Closeable;
import java.util.Collection;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.loom.conf.AS7Config;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class AS7CliUtilsTest_ {
    
    public AS7CliUtilsTest_() {
    }


    @Test
    public void testRemoveResourceIfExists() throws Exception {
        System.out.println( "removeResourceIfExists" );
        ModelNode loggerCmd = null;
        ModelControllerClient aS7Client = null;
        AS7CliUtils.removeResourceIfExists( loggerCmd, aS7Client );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testExists() throws Exception {
        System.out.println( "exists" );
        ModelNode resource = null;
        ModelControllerClient client = null;
        boolean expResult = false;
        boolean result = AS7CliUtils.exists( resource, client );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testCreateRemoveCommandForResource() {
        System.out.println( "createRemoveCommandForResource" );
        ModelNode resource = null;
        ModelNode expResult = null;
        ModelNode result = AS7CliUtils.createRemoveCommandForResource( resource );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testExecuteRequest() throws Exception {
        System.out.println( "executeRequest" );
        ModelNode request = null;
        AS7Config as7config = null;
        AS7CliUtils.executeRequest( request, as7config );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testSafeClose() {
        System.out.println( "safeClose" );
        Closeable closeable = null;
        AS7CliUtils.safeClose( closeable );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testParseFailedOperationIndex() throws Exception {
        System.out.println( "parseFailedOperationIndex" );
        ModelNode node = null;
        Integer expResult = null;
        Integer result = AS7CliUtils.parseFailedOperationIndex( node );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testExtractFailedOperationNode() throws Exception {
        System.out.println( "extractFailedOperationNode" );
        ModelNode node = null;
        BatchFailure expResult = null;
        BatchFailure result = AS7CliUtils.extractFailedOperationNode( node );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testCopyProperties() {
        System.out.println( "copyProperties" );
        Object source = null;
        CliApiCommandBuilder builder = null;
        String props = "";
        AS7CliUtils.copyProperties( source, builder, props );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testJoinQuoted() {
        System.out.println( "joinQuoted" );
        Collection<String> col = null;
        String expResult = "";
        String result = AS7CliUtils.joinQuoted( col );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testParseCommand() {
        System.out.println( "parseCommand" );
        String command = "";
        ModelNode expResult = null;
        ModelNode result = AS7CliUtils.parseCommand( command );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testUnquote() {
        System.out.println( "unquote" );
        String string = "";
        String expResult = "";
        String result = AS7CliUtils.unquote( string );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testFormatCommand() {
        System.out.println( "formatCommand" );
        ModelNode command = null;
        String expResult = "";
        String result = AS7CliUtils.formatCommand( command );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testQueryServerPath() throws Exception {
        System.out.println( "queryServerPath" );
        String path = "";
        ModelControllerClient client = null;
        String expResult = "";
        String result = AS7CliUtils.queryServerPath( path, client );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testQueryServerHomeDir() throws Exception {
        System.out.println( "queryServerHomeDir" );
        ModelControllerClient client = null;
        String expResult = "";
        String result = AS7CliUtils.queryServerHomeDir( client );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testQueryServerBaseDir() throws Exception {
        System.out.println( "queryServerBaseDir" );
        ModelControllerClient client = null;
        String expResult = "";
        String result = AS7CliUtils.queryServerBaseDir( client );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testQueryServerConfigDir() throws Exception {
        System.out.println( "queryServerConfigDir" );
        ModelControllerClient client = null;
        String expResult = "";
        String result = AS7CliUtils.queryServerConfigDir( client );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testEscapeAddressElement() {
        System.out.println( "escapeAddressElement" );
        String element = "";
        String expResult = "";
        String result = AS7CliUtils.escapeAddressElement( element );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testFormatGetterName() {
        System.out.println( "formatGetterName" );
        String prop = "";
        String expResult = "";
        String result = AS7CliUtils.formatGetterName( prop );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }
    
}