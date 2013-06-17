/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.actions;

import org.jboss.loom.conf.Configuration;
import static org.jboss.loom.conf.Configuration.IfExists.ASK;
import static org.jboss.loom.conf.Configuration.IfExists.FAIL;
import static org.jboss.loom.conf.Configuration.IfExists.MERGE;
import static org.jboss.loom.conf.Configuration.IfExists.OVERWRITE;
import static org.jboss.loom.conf.Configuration.IfExists.SKIP;
import static org.jboss.loom.conf.Configuration.IfExists.WARN;
import org.jboss.loom.ex.ActionException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.utils.as7.AS7CliUtils;
import org.jboss.loom.utils.as7.BatchedCommandWithAction;
import org.jboss.as.cli.batch.BatchedCommand;
import org.jboss.dmr.ModelNode;
import org.jboss.loom.spi.ann.ActionDescriptor;
import org.jboss.loom.spi.ann.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@ActionDescriptor(
    header = "Perform CLI command:",
    props = {
        @Property(name="cliCommand", expr = "command.command", style = "code") // TODO
    }
)
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
        return "Perform CLI command: " + this.command.getCommand() + " ; ifExists=" + ifExists + ", todo=" + todo;
    }
    


    @Override
    public void preValidate() throws MigrationException {
        if ((this.command.getCommand() == null) || (this.command.getCommand().isEmpty()))
            throw new ActionException(this, "No CLI script set for CliCommandAction.");
        if (this.command.getRequest() == null) {
            throw new ActionException(this, "ModelNode for CliCommandAction cannot be null.");
        }
        
        // If already exists, 
        boolean exists;
        try {
            exists = AS7CliUtils.exists( this.command.getRequest(), getMigrationContext().getAS7Client() );
            //log.debug( "Exists? " + exists + "  :  " + this.command );
        } catch( Exception ex ) {
            throw new ActionException( this, "Failed querying AS 7 for existence of " + this.command.getRequest() + ": " + ex, ex );
        }
        if( ! exists ) return;
        
        // ... act as per configuration.
        switch( this.ifExists ){
            case OVERWRITE: this.todo = OVERWRITE; break;                    
            case FAIL:  throw new ActionException(this, "ModelNode already exists in AS 7 config: " + this.command.getCommand() );
            case MERGE: throw new UnsupportedOperationException("ModelNode merging not supported yet. MIGR-61");
            case WARN:  this.todo = SKIP; log.warn("ModelNode already exists in AS 7 config: " + this.command.getCommand() ); return;
            case SKIP:  this.todo = SKIP; return;
            case ASK:   throw new UnsupportedOperationException("Interactive duplicity handling not supported yet. MIGR-62");
        }
    }// preValidate()


    @Override
    public void perform() throws MigrationException {
        if( this.todo == SKIP )  return;
        if( this.todo == OVERWRITE ){
            // Remove the pre-existing node.
            ModelNode remCmd = AS7CliUtils.createRemoveCommandForResource( this.command.getRequest() );
            //log.debug("\n    Adding REMOVE operation: " + remCmd);
            String desc = AS7CliUtils.formatCommand( remCmd ); //remCmd.asString();
            getMigrationContext().getBatch().add( new BatchedCommandWithAction( this, desc, remCmd) );
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
    

    /*
     *  Workaround until MIGR-128.
     *  TODO: Rename getCommand() to getBatchCommand() and this to getCommand().
     */
    @Property(name = "cliCommand", style = "code")
    public String getCommandCommand(){
        return this.command.getCommand();
    }
    
    @Override
    public String toString() {
        return "CliCommandAction{" + command.getCommand() + "; ifExists=" + ifExists + ", todo=" + todo + '}';
    }

}// class
