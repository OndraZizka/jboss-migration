package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.MigrationContext;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.spi.IMigrator;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 *         <p/>
 *         TODO: Introduce do***(), eg. doBackup(), to manage the states here, not in the impl.
 */
public abstract class AbstractStatefulAction implements IMigrationAction {

    IMigrationAction.State state = State.INITIAL;

    private MigrationContext ctx;
    private String originMessage;
    private Class<? extends IMigrator> fromMigrator;
    private List<String> warnings = new LinkedList();


    public AbstractStatefulAction( Class<? extends IMigrator> fromMigrator ) {
        this.fromMigrator = fromMigrator;
    }

    public void addWarning(String text) {
        warnings.add(text);
    }


    @Override
    public void setMigrationContext(MigrationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public MigrationContext getMigrationContext() {
        return this.ctx;
    }

    @Override
    public IMigrationAction.State getState() {
        return state;
    }

    public void setState(IMigrationAction.State state) {
        this.state = state;
    }

    public void checkState(IMigrationAction.State state) throws MigrationException {
        if (this.state != state)
            throw new MigrationException("Action not in expected state '" + state + ": " + this);
    }


    @Override
    public String getOriginMessage() {
        return originMessage;
    }
    
    public Class<? extends IMigrator> getFromMigrator(){
        return fromMigrator;
    }

    @Override
    public List<String> getWarnings() {
        return warnings;
    }


    protected boolean isAfterBackup() {
        return this.state.ordinal() >= State.BACKED_UP.ordinal();
    }

    protected boolean isAfterPerform() {
        return this.state.ordinal() >= State.DONE.ordinal();
    }

}
