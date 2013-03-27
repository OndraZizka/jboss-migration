package cz.muni.fi.jboss.migration.ex;

/**
 * Exception representing error in apply method in IMigrator
 *
 * @author Roman Jakubco
 */
public class ApplyMigrationException extends MigrationException {

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
