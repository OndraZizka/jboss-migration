package org.jboss.loom.actions;

import org.jboss.loom.MigrationContext;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.spi.IMigrator;

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
    private StackTraceElement originStacktrace;
    private Class<? extends IMigrator> fromMigrator;
    private List<String> warnings = new LinkedList();

    public AbstractStatefulAction(){
    }

    public AbstractStatefulAction( Class<? extends IMigrator> fromMigrator ) {
        this.fromMigrator = fromMigrator;
        this.originStacktrace = Thread.currentThread().getStackTrace()[3];
        // 0 - Thread.getStackTrace().
        // 1 - This constructor.
        // 2 - *Action constructor.
        // 3 - Whatever called new CliCommandAction.
        // Could be better, e.g. first non-constructor after 2.
    }

    public void addWarning(String text) {
        warnings.add(text);
    }

    
    public void checkState(IMigrationAction.State state) throws MigrationException {
        if (this.state != state)
            throw new MigrationException("Action not in expected state '" + state + ": " + this);
    }


    //<editor-fold defaultstate="collapsed" desc="get/set">
    @Override public void setMigrationContext(MigrationContext ctx) { this.ctx = ctx; }
    @Override public MigrationContext getMigrationContext() { return this.ctx; }
    
    @Override public IMigrationAction.State getState() { return state; }
    public void setState(IMigrationAction.State state) { this.state = state; }
    
    @Override public StackTraceElement getOriginStackTrace(){ return originStacktrace; }
    @Override public String getOriginMessage() { return originMessage; }
    public AbstractStatefulAction setOriginMessage(String msg) { this.originMessage = msg; return this; }
    @Override public Class<? extends IMigrator> getFromMigrator(){ return fromMigrator; }
    @Override public List<String> getWarnings() { return warnings; }
    //</editor-fold>

    protected boolean isAfterBackup() {
        return this.state.ordinal() >= State.BACKED_UP.ordinal();
    }

    protected boolean isAfterPerform() {
        return this.state.ordinal() >= State.DONE.ordinal();
    }

}// class
