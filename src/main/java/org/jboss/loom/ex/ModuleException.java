package org.jboss.loom.ex;

/**
 * @author Roman Jakubco
 */
public class ModuleException extends MigrationException {
    public ModuleException(String message) {
        super(message);
    }

    public ModuleException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModuleException(Throwable cause) {
        super(cause);
    }
}
