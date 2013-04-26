package org.jboss.loom.ex;

/**
 * Exception representing error in copying required files from AS5 to AS7
 *
 * @author Roman Jakubco
 */
public class CopyException extends MigrationException {
    public CopyException(String message) {
        super(message);
    }

    public CopyException(String message, Throwable cause) {
        super(message, cause);
    }

    public CopyException(Throwable cause) {
        super(cause);
    }
}
