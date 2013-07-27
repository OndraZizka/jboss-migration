package org.jboss.loom.migrators._ext.process;


import java.util.LinkedList;
import java.util.List;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.actions.ManualAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Root context to collect the actions to.
 */
class RootContext extends Variables implements ProcessingStackItem, Has.Actions, Has.Warnings {
    List<IMigrationAction> actions = new LinkedList();
    List<String> warnings = new LinkedList();


    @Override public void addAction( IMigrationAction action ) { this.actions.add( action ); }

    @Override public List<IMigrationAction> getActions() { return actions; }

    @Override public void addWarning( String warn ) { this.warnings.add( warn ); }

    @Override public List<String> getWarnings() { return warnings; }

    @Override public Object getVariable( String name ) { return this.getVariable( name ); }


    /**  Returns a ManualAction with warnings of the root context, or null if there were no warnings. */
    ManualAction convertWarningsToManualAction() {
        if( this.warnings.isEmpty() ) {
            return null;
        }
        ManualAction action = new ManualAction();
        for( String warn : this.warnings ) {
            action.addWarning( warn );
        }
        return action;
    }


}// class
