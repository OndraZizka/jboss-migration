package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.conf.Configuration;
import static cz.muni.fi.jboss.migration.conf.Configuration.IfExists.ASK;
import static cz.muni.fi.jboss.migration.conf.Configuration.IfExists.FAIL;
import static cz.muni.fi.jboss.migration.conf.Configuration.IfExists.MERGE;
import static cz.muni.fi.jboss.migration.conf.Configuration.IfExists.OVERWRITE;
import static cz.muni.fi.jboss.migration.conf.Configuration.IfExists.SKIP;
import static cz.muni.fi.jboss.migration.conf.Configuration.IfExists.WARN;
import cz.muni.fi.jboss.migration.ex.ActionException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import cz.muni.fi.jboss.migration.utils.AS7CliUtils;
import cz.muni.fi.jboss.migration.utils.as7.BatchedCommandWithAction;
import org.jboss.as.cli.batch.BatchedCommand;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class CliCommandAction extends AbstractStatefulAction {
    private static final Logger log = LoggerFactory.getLogger(CliCommandAction.class);
    
    /** Management API command to perform. */
    private BatchedCommandWithAction command;
    
    /** What to do if the resource (node) already exists (for ADD operations). */
    private Configuration.IfExists ifExists = Configuration.IfExists.WARN;
    
    // Workflow control - what to do in the perform step.
    private Configuration.IfExists todo = null;
    
    
    
    // script parameter is created text script and cliCommand is script representation in CLI API
    public CliCommandAction( Class<? extends IMigrator> fromMigrator, String script, ModelNode cliCommand) {
        super(fromMigrator);
        //this.cliCommand = scriptAPI;
        //this.script = script;
        this.command = new BatchedCommandWithAction(this, script, cliCommand);
    }


    @Override
    public String toDescription() {
        return "Perform CLI command: " + this.command.getCommand();
    }
    


    @Override
    public void preValidate() throws MigrationException {
        if ((this.command.getCommand() == null) || (this.command.getCommand().isEmpty()))
            throw new ActionException(this, "No CLI script set for CliCommandAction");
        if (this.command.getRequest() == null) {
            throw new ActionException(this, "ModelNode for CliCommandAction cannot be null");
        }
        
        // If already exists, 
        boolean exists;
        try {
            exists = AS7CliUtils.exists( this.command.getRequest(), getMigrationContext().getAS7Client() );
        } catch( Exception ex ) {
            throw new ActionException( this, "Failed querying AS 7 for existence of " + this.command.getRequest() + ": " + ex, ex );
        }
        if( ! exists ) return;
        
        // ... act as per configuration.
        switch( this.ifExists ){
            case OVERWRITE: todo = OVERWRITE; break;                    
            case FAIL:  throw new ActionException(this, "ModelNode already exists in AS 7 config: " + this.command.getCommand() );
            case MERGE: throw new UnsupportedOperationException("ModelNode merging not supported yet. MIGR-61");
            case WARN:  todo = SKIP; log.warn("ModelNode already exists in AS 7 config: " + this.command.getCommand() ); return;
            case SKIP:  todo = SKIP; return;
            case ASK:   throw new UnsupportedOperationException("Interactive duplicity handling not supported yet. MIGR-62");
        }
    }// preValidate()


    @Override
    public void perform() throws MigrationException {
        if( todo == SKIP )  return;
        if( todo == OVERWRITE ){
            // Remove the pre-existing node.
            ModelNode remCmd = AS7CliUtils.createRemoveCommandForResource( this.command.getRequest() );
            getMigrationContext().getBatch().add( new BatchedCommandWithAction( this, remCmd.asString(), remCmd) );
        }
        
        // Perform.
        getMigrationContext().getBatch().add(this.command);
        setState(State.DONE);
    }


    @Override
    public void rollback() throws MigrationException {
        // Batch provides rollback.
        setState(State.ROLLED_BACK);
    }


    @Override
    public void postValidate() throws MigrationException {
        // Empty
    }


    @Override
    public void backup() throws MigrationException {
        // Batch handles backup.
        setState(State.BACKED_UP);
    }


    @Override
    public void cleanBackup() {
        // Empty
        setState(State.FINISHED);
    }


    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public BatchedCommand getCommand() { return command; }
    public void setCommand( BatchedCommand command ) { this.command = new BatchedCommandWithAction( this, command ); }
    public Configuration.IfExists getIfExists() { return ifExists; }
    public CliCommandAction setIfExists( Configuration.IfExists ifExists ) { this.ifExists = ifExists; return this; }


    //</editor-fold>
    
    
    
    @Override
    public String toString() {
        return "CliCommandAction{" + command + "; ifExists=" + ifExists + ", todo=" + todo + '}';
    }
    
}// class
