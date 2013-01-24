package cz.muni.fi.jboss.migration.ex;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:38 AM
 */
public class ApplyMigrationException extends Exception{

    public ApplyMigrationException(String message) {
        super(message);
    }

    public ApplyMigrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplyMigrationException(Throwable cause) {
        super(cause);
    }
}
