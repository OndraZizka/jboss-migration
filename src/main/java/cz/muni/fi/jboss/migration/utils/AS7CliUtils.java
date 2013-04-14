package cz.muni.fi.jboss.migration.utils;

import cz.muni.fi.jboss.migration.ex.MigrationException;
import java.io.Closeable;
import java.io.IOException;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class AS7CliUtils {
    
    /**
     * Temp method for testing
     */
    public static void executeRequest(ModelNode request) throws IOException, MigrationException {
        ModelControllerClient client = null;
        try {
            client = ModelControllerClient.Factory.create("localhost", 9999);
            final ModelNode response = client.execute(new OperationBuilder(request).build());
            reportFailure(response);
        } catch (IOException e) {
            //throw new MigrationException("Execution of the batch failed: " + e.getMessage(), e);
            
            // Specific problem on Roman's PC. Need to connect two times.
            final ModelNode response = client.execute(new OperationBuilder(request).build());
            reportFailure( response );
        } finally {
            safeClose(client);
        }
    }
    
    /**
     * Temp method for testing
     */
    private static void safeClose(final Closeable closeable) throws MigrationException {
        if (closeable != null) try {
            closeable.close();
        } catch (IOException e) {
            throw new MigrationException("Closing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Temp method for testing
     */
    private static void reportFailure(final ModelNode node) throws MigrationException {
        if (!node.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {
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
            throw new MigrationException(msg);
        }
    }
    
    
}// class
