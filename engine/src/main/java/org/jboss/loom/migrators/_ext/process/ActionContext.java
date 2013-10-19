package org.jboss.loom.migrators._ext.process;

import java.util.List;
import org.jboss.loom.actions.IMigrationAction;

/**
 *  Action context - delegates actions and warnings to the referenced action.
 */
class ActionContext implements ProcessingStackItem, Has.Actions, Has.Warnings {
    
    IMigrationAction action;
    
    private String varName;


    public ActionContext( IMigrationAction action, String varName ) {
        this.action = action;
        this.varName = varName == null ? "action" : varName.trim();
    }


    @Override public void addAction( IMigrationAction action ) {
        action.addDependency( action );
    }
        
    @Override public List<IMigrationAction> getActions() { return this.action.getDependencies(); }

    
    @Override public void addWarning( String warn ) {
        this.action.getWarnings().add( warn );
    }

    @Override public List<String> getWarnings() { return this.action.getWarnings(); }

    
    //@Override public Map<String, Object> getVariables() { return null; }
    @Override
    public Object getVariable( String name ) {
        return this.varName.equals( name ) ? this.action : null;
    }

}// class
