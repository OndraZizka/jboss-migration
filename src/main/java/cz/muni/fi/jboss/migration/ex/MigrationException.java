package cz.muni.fi.jboss.migration.ex;

/**
 * Exception representing error in migration.
 *
 * @author Roman Jakubco
 */
public class MigrationException extends Exception {

    public MigrationException(String message) {
        super(message);
    }

    public MigrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MigrationException(Throwable cause) {
        super(cause);
    }
}
