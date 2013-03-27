package cz.muni.fi.jboss.migration.ex;

/**
 * @author Roman Jakubco
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
