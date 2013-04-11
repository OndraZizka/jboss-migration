package cz.muni.fi.jboss.migration.ex;

/**
 * @author Roman Jakubco
 */
public class ActionException extends MigrationException {
    public ActionException(String message) {
        super(message);
    }

    public ActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActionException(Throwable cause) {
        super(cause);
    }
}
