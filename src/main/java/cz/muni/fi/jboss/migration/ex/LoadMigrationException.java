package cz.muni.fi.jboss.migration.ex;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:38 AM
 */
public class LoadMigrationException extends Exception{

    public LoadMigrationException(String message) {
        super(message);
    }

    public LoadMigrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadMigrationException(Throwable cause) {
        super(cause);
    }
}
