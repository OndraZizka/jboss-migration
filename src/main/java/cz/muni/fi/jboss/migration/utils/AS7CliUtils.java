package cz.muni.fi.jboss.migration.utils;

import cz.muni.fi.jboss.migration.CliApiCommandBuilder;
import cz.muni.fi.jboss.migration.ex.CliBatchException;
import cz.muni.fi.jboss.migration.conf.AS7Config;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.migrators.logging.jaxb.ConsoleHandlerBean;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class AS7CliUtils {
    
    private final static String OP_KEY_PREFIX = "Operation step-";
    
    
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
        if( ! ClientConstants.SUCCESS.equals( node.get(ClientConstants.OUTCOME).asString() )) {
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
    }
    
    /**
     *  Parses the index of operation which failed.
     * 
     *  {"JBAS014653: Composite operation failed and was rolled back. Steps that failed:" => {
     *      "Operation step-12" => "JBAS014803: Duplicate resource [
                (\"subsystem\" => \"security\"),
                (\"security-domain\" => \"other\") ]"
        }}
     */
    public static Integer parseFailedOperationIndex(final ModelNode node) throws MigrationException {
        
        if( ClientConstants.SUCCESS.equals( node.get(ClientConstants.OUTCOME).asString() ))
            return 0;
        
        if( ! node.hasDefined(ClientConstants.FAILURE_DESCRIPTION))
            return null;
        
        ModelNode failDesc = node.get(ClientConstants.FAILURE_DESCRIPTION);
        // "Operation step-1" => "JBAS014803: Duplicate resource ...
        String key = failDesc.keys().iterator().next();
        ModelNode compositeFailDesc = failDesc.get(key);
        Set<String> keys = compositeFailDesc.keys();
        // "Operation step-XX"
        String opKey = keys.iterator().next();
        if( ! opKey.startsWith(OP_KEY_PREFIX) )
            return null;
        
        String opIndex = StringUtils.substring( opKey, OP_KEY_PREFIX.length() );
        
        return Integer.parseInt( opIndex );
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

        if( col.size() == 0 )
            return "";
        
        StringBuilder sb = new StringBuilder();
        for( String item : col )
            sb.append(",\"").append(item).append('"');

        String str = sb.toString();
        str = str.replaceFirst(",", "");
        return str;
    }
    
}// class
