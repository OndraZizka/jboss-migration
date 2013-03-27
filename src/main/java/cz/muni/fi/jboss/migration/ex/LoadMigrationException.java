package cz.muni.fi.jboss.migration.ex;

/**
 * Exception representing error in load method in IMigrator
 *
 * @author Roman Jakubco
 */
public class LoadMigrationException extends MigrationException {

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
