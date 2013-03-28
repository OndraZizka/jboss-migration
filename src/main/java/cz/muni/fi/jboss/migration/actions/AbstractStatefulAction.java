package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.MigrationContext;
import cz.muni.fi.jboss.migration.ex.MigrationException;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public abstract class AbstractStatefulAction implements IMigrationAction {

    IMigrationAction.State state = State.INITIAL;
    
    private MigrationContext ctx;

    @Override
    public void setMigrationContext( MigrationContext ctx ) {
        this.ctx = ctx;
    }
    

    @Override
    public IMigrationAction.State getState() { return state; }
    public void setState( IMigrationAction.State state ) { this.state = state; }
    
    public void checkState( IMigrationAction.State state ) throws MigrationException {
        if( this.state != state )
            throw new MigrationException("Action not in expected state '" + state + ": " + this);
    }
    
}
