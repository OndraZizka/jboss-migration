package cz.muni.fi.jboss.migration.ex;

/**
 * @author Roman Jakubco
 *         Date: 2/2/13
 *         Time: 11:52 AM
 */
public class NodeGenerationException extends MigrationException {
    public NodeGenerationException(String message) {
        super(message);
    }

    public NodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeGenerationException(Throwable cause) {
        super(cause);
    }
}
