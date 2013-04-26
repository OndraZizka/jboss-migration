package org.jboss.loom.utils;

import org.jboss.loom.CliApiCommandBuilder;
import org.jboss.loom.ex.CliBatchException;
import org.jboss.loom.conf.AS7Config;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.utils.as7.BatchFailure;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.jboss.dmr.ModelType;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class AS7CliUtils {
    
    private final static String OP_KEY_PREFIX = "Operation step-";
    
    
    
    public static void removeResourceIfExists( ModelNode loggerCmd, ModelControllerClient aS7Client ) throws IOException, CliBatchException {
        
        // Check if exists.
        if( ! exists( loggerCmd, aS7Client ))  return;
        
        // Remove.
        ModelNode res = aS7Client.execute( createRemoveCommandForResource( loggerCmd ) );
        throwIfFailure( res );
    }
    
    /**
     *  Queries the AS 7 if given resource exists.
     */
    public static boolean exists( final ModelNode resource, ModelControllerClient client ) throws IOException {
        ModelNode query = new ModelNode();
        // Read operation.
        query.get(ClientConstants.OP).set(ClientConstants.READ_RESOURCE_OPERATION);
        // Copy the address.
        query.get(ClientConstants.OP_ADDR).set( resource.get(ClientConstants.OP_ADDR) );
        ModelNode res = client.execute( query );
        return wasSuccess( res );
    }


    
    public static ModelNode createRemoveCommandForResource( ModelNode resource ) {
        // Copy the address.
        ModelNode query = new ModelNode();
        query.get(ClientConstants.OP_ADDR).set( resource.get(ClientConstants.OP_ADDR) );
        // Remove operation.
        query.get(ClientConstants.OP).set(ClientConstants.REMOVE_OPERATION);
        
        return query;
    }
    
    
    /**
     *  Executes CLI request.
     */
    public static void executeRequest(ModelNode request, AS7Config as7config) throws IOException, CliBatchException {
        ModelControllerClient client = null;
        try {
            client = ModelControllerClient.Factory.create(as7config.getHost(), as7config.getManagementPort());
            final ModelNode response = client.execute(new OperationBuilder(request).build());
            throwIfFailure(response);
        }
        catch (IOException ex) {
            // Specific problem on Roman's PC. Need to connect two times.
            final ModelNode response = client.execute(new OperationBuilder(request).build());
            throwIfFailure( response );
        }
        finally {
            safeClose(client);
        }
    }
    
    /**
     *  Safely closes closeable resource (a CLI connection in our case).
     */
    public static void safeClose(final Closeable closeable) {
        if (closeable != null) try {
            closeable.close();
        } catch (IOException e) {
            //throw new MigrationException("Closing failed: " + e.getMessage(), e);
        }
    }

    /**
     *  If the result is an error, throw an exception.
     */
    private static void throwIfFailure(final ModelNode node) throws CliBatchException {
        if( wasSuccess( node ) )
            return;
        
        final String msg;
        if (node.hasDefined(ClientConstants.FAILURE_DESCRIPTION)) {
            if (node.hasDefined(ClientConstants.OP)) {
                msg = String.format("Operation '%s' at address '%s' failed: %s", node.get(ClientConstants.OP), node.get(ClientConstants.OP_ADDR), node.get(ClientConstants.FAILURE_DESCRIPTION));
            } else {
                msg = String.format("Operation failed: %s", node.get(ClientConstants.FAILURE_DESCRIPTION));
            }
        } else {
            msg = String.format("Operation failed: %s", node);
        }
        throw new CliBatchException(msg, node);
    }
    
    private static boolean wasSuccess( ModelNode node ) {
        return ClientConstants.SUCCESS.equals( node.get(ClientConstants.OUTCOME).asString() );
    }
    
    
    
    /**
     *  Parses the index of operation which failed.
     * 
     *  "failure-description" => 
     *  {"JBAS014653: Composite operation failed and was rolled back. Steps that failed:" => {
     *      "Operation step-12" => "JBAS014803: Duplicate resource [
                (\"subsystem\" => \"security\"),
                (\"security-domain\" => \"other\") ]"
        }}
        * 
        * @deprecated  Use extractFailedOperationNode().
     */
    public static Integer parseFailedOperationIndex(final ModelNode node) throws MigrationException {
        
        if( ClientConstants.SUCCESS.equals( node.get(ClientConstants.OUTCOME).asString() ))
            return 0;
        
        if( ! node.hasDefined(ClientConstants.FAILURE_DESCRIPTION))
            return null;
        
        ModelNode failDesc = node.get(ClientConstants.FAILURE_DESCRIPTION);
        String key = failDesc.keys().iterator().next();
        // "JBAS014653: Composite operation failed and was rolled back. Steps that failed:" => ...
        
        ModelNode compositeFailDesc = failDesc.get(key);
        // { "Operation step-1" => "JBAS014803: Duplicate resource ...
        
        Set<String> keys = compositeFailDesc.keys();
        String opKey = keys.iterator().next();
        // "Operation step-XX"
        
        if( ! opKey.startsWith(OP_KEY_PREFIX) )
            return null;
        
        String opIndex = StringUtils.substring( opKey, OP_KEY_PREFIX.length() );
        
        return Integer.parseInt( opIndex );
    }

    /**
     * @returns A ModelNode with two properties: "failedOpIndex" and "failureDesc".
     */
    public static BatchFailure extractFailedOperationNode(final ModelNode node) throws MigrationException {
        
        if( ClientConstants.SUCCESS.equals( node.get(ClientConstants.OUTCOME).asString() ))
            return null;
        
        if( ! node.hasDefined(ClientConstants.FAILURE_DESCRIPTION))
            return null;
        
        ModelNode failDesc = node.get(ClientConstants.FAILURE_DESCRIPTION);
        String key = failDesc.keys().iterator().next();
        // "JBAS014653: Composite operation failed and was rolled back. Steps that failed:" => ...
        
        ModelNode compositeFailDesc = failDesc.get(key);
        // { "Operation step-1" => "JBAS014803: Duplicate resource ...
        
        Set<String> keys = compositeFailDesc.keys();
        String opKey = keys.iterator().next();
        // "Operation step-XX"

        if( ! opKey.startsWith(OP_KEY_PREFIX) )
            return null;
        
        String opIndex = StringUtils.substring( opKey, OP_KEY_PREFIX.length() );
        
        return new BatchFailure( Integer.parseInt( opIndex ), compositeFailDesc.get(opKey).toString());
    }


    
    /**
     *  Copies properties using reflection.
     * @param handler  From this object.
     * @param builder  Append to this CLI builder.
     * @param A list of properties, as a space separated string.
     */
    public static void copyProperties( Object source, CliApiCommandBuilder builder, String props ) {
        String[] parts = StringUtils.split( props );
        for( String prop : parts ) {
            try {
                Method method = source.getClass().getMethod( convertPropToMethodName(prop) );
                if( String.class != method.getReturnType() )
                    continue;
                String val = (String) method.invoke(source);
                builder.addProperty( prop, val );
            }
            catch ( NoSuchMethodException ex ){
                throw new RuntimeException( ex );
            }
            catch ( InvocationTargetException ex ){
                throw new RuntimeException( ex );
            }
            catch ( IllegalAccessException ex ){
                throw new RuntimeException( ex );
            }
            catch ( IllegalArgumentException ex ){
                throw new RuntimeException( ex );
            }
        }
    }
    
    private static String convertPropToMethodName( String propName ){
        StringBuilder sb  = new StringBuilder("get");
        String[] parts = StringUtils.split( propName, "-");
        for( String part : parts) {
            sb.append( StringUtils.capitalize( part ) );
        }
        return sb.toString();
    }
    
    
    /**
     *  Joins the given list into a string of quoted values joined with ", ".
     * @param col
     * @return 
     */
    public static String joinQuoted( Collection<String> col ){

        if( col.isEmpty() )
            return "";
        
        StringBuilder sb = new StringBuilder();
        for( String item : col )
            sb.append(",\"").append(item).append('"');

        String str = sb.toString();
        str = str.replaceFirst(",", "");
        return str;
    }


    /**
     *   Formats Model node to the form of CLI script command - /foo=a/bar=b/:operation(param=value,...) .
     */
    public static String formatCommand( ModelNode command ) {
        
        if( ! command.has(ClientConstants.OP) )
            throw new IllegalArgumentException("'"+ClientConstants.OP+"' not defined.");
        if( command.get(ClientConstants.OP).getType() != ModelType.STRING )
            throw new IllegalArgumentException("'"+ClientConstants.OP+"' must be a string.");
        if( ! command.has(ClientConstants.OP_ADDR) )
            throw new IllegalArgumentException("'"+ClientConstants.OP_ADDR+"' not defined.");
        if( command.get(ClientConstants.OP_ADDR).getType() != ModelType.LIST )
            throw new IllegalArgumentException("'"+ClientConstants.OP_ADDR+"' must be a list.");
        
        // Operation.
        String op = command.get(ClientConstants.OP).asString();
        
        // Address
        ModelNode addr = command.get(ClientConstants.OP_ADDR);
        StringBuilder sb = new StringBuilder("/");
        for( int i = 0; ; i++ ) {
            if( ! addr.has(i) )  break;
            ModelNode segment = addr.get( i );
            String key = segment.keys().iterator().next();
            sb.append(key).append('=').append(segment.get(key).asString()).append('/');
        }
        sb.append(':').append(op);
        
        // Params.
        boolean hasParams = false;
        Set<String> keys = command.keys();
        for( String key : keys ) {
            switch( key ){
                case ClientConstants.OP:
                case ClientConstants.OP_ADDR: continue;
            }
            sb.append( hasParams ? ',' : '(');
            hasParams = true;
            sb.append(key).append('=').append(command.get(key));
        }
        if( hasParams )  sb.append(')');
        return sb.toString();
    }
    
    /**
        /path=jboss.server.base.dir/:read-attribute(name=path,include-defaults=true)
        {
            "outcome" => "success",
            "result" => "/home/ondra/work/AS/Migration/AS-7.1.3/standalone"
        }
     */
    public static String queryServerPath( String path, ModelControllerClient client ) throws MigrationException{

        ModelNode query = new ModelNode();
        query.get(ClientConstants.OP).set(ClientConstants.READ_ATTRIBUTE_OPERATION);
        query.get(ClientConstants.OP_ADDR).add("path", path);
        query.get("name").set("path");
        ModelNode response;
        try {
            response = client.execute( query );
            throwIfFailure( response );
        } catch( IOException | CliBatchException ex ) {
            throw new MigrationException("Failed querying for AS 7 directory.", ex);
        }
        ModelNode result = response.get(ClientConstants.RESULT);
        if( result.getType() == ModelType.UNDEFINED )
            return null;
        return result.asString();
    }
    
    /**
     *  Actually it returns the base dir, i.e. the dir containing bin/, standalone/ etc.
     */
    public static String queryServerHomeDir( ModelControllerClient client ) throws MigrationException{
        return queryServerPath( "jboss.home.dir", client);
    }
    
    /**
     *  E.g. $AS/standalone (full path).
     */
    public static String queryServerBaseDir( ModelControllerClient client ) throws MigrationException{
        return queryServerPath( "jboss.server.base.dir", client);
    }
    
    public static String queryServerConfigDir( ModelControllerClient client ) throws MigrationException{
        return queryServerPath( "jboss.server.config.dir", client);
    }
    
    
    /**
     *  Escape CLI address element - the parts between / and = in /foo=bar/baz=moo .
     */
    public static String escapeAddressElement(String element) {
        element = element.replace(":", "\\:");
        element = element.replace("/", "\\/");
        element = element.replace("=", "\\=");
        element = element.replace(" ", "\\ ");
        return element;
    }  
    
}// class
