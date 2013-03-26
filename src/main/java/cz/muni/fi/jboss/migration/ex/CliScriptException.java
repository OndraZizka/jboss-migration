package cz.muni.fi.jboss.migration.ex;

/**
 * Exception representing error in methods for generating CLI scripts.
 *
 * @author Roman Jakubco
 */

public class CliScriptException extends MigrationException {

    public CliScriptException(String message) {
        super(message);
    }

    public CliScriptException(String message, Throwable cause) {
        super(message, cause);
    }

    public CliScriptException(Throwable cause) {
        super(cause);
    }
}
