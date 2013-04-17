package cz.muni.fi.jboss.migration.ex;

import cz.muni.fi.jboss.migration.actions.IMigrationAction;

/**
 * @author Roman Jakubco
 */
public class ActionException extends MigrationException {
    
    
    private final IMigrationAction action;
    
    
    public ActionException(IMigrationAction action, String message) {
        super(message);
        this.action = action;
    }

    public ActionException(IMigrationAction action, String message, Throwable cause) {
        super(message, cause);
        this.action = action;
    }

    public ActionException(IMigrationAction action, Throwable cause) {
        super(cause);
        this.action = action;
    }


    public IMigrationAction getAction() {
        return action;
    }
    
}// class
