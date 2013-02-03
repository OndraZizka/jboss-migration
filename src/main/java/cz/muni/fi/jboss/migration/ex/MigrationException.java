package cz.muni.fi.jboss.migration.ex;

/**
 * Exception representing error in migration.
 *
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:38 AM
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
