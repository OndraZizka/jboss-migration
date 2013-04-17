package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.ex.ActionException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import org.jboss.as.cli.batch.BatchedCommand;
import org.jboss.as.cli.batch.impl.DefaultBatchedCommand;
import org.jboss.dmr.ModelNode;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class CliCommandAction extends AbstractStatefulAction {

    //private ModelNode cliCommand;
    //private String script;

    // Better approach
    private BatchedCommand command;

    
    // script parameter is created text script and cliCommand is script representation in CLI API
    public CliCommandAction( Class<? extends IMigrator> fromMigrator, String script, ModelNode cliCommand) {
        super(fromMigrator);
        //this.cliCommand = scriptAPI;
        //this.script = script;
        this.command = new DefaultBatchedCommand(script, cliCommand);
    }


    @Override
    public String toDescription() {
        return "Perform CLI command: " + this.command.getCommand();
    }
    


    @Override
    public void preValidate() throws MigrationException {
        if ((this.command.getCommand() == null) || (this.command.getCommand().isEmpty()))
            throw new ActionException(this, "CLI script for CliCommandAction doesn't exist");
        if (this.command.getRequest() == null) {
            throw new ActionException(this, "ModelNode for CliCommandAction cannot be null");
        }
    }


    @Override
    public void perform() throws MigrationException {
        getMigrationContext().getBatch().add(this.command);
        setState(State.DONE);
    }


    @Override
    public void rollback() throws MigrationException {
        // Batch provides rollback. Probably empty method.
        setState(State.ROLLED_BACK);

    }


    @Override
    public void postValidate() throws MigrationException {
        // Empty?
    }


    @Override
    public void backup() throws MigrationException {
        // Will be empty. Batch do everything
        setState(State.BACKED_UP);
    }


    @Override
    public void cleanBackup() {
        // Empty?
        setState(State.FINISHED);
    }

}// class
