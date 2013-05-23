/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.actions;

import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.spi.IMigrator;

import java.util.LinkedList;
import java.util.List;

/**
 * Implements lifecycle methods which manage the state,
 * and some properties (context, origin message, origin stacktrace, origin migrator, warnings, dependencies).
 * 
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
    private List<IMigrationAction> deps = new LinkedList();
    

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


    @Override
    public List<IMigrationAction> getDependencies() {
        return this.deps;
    }


    @Override
    public IMigrationAction addDependency( IMigrationAction dep ) {
        this.deps.add( dep );
        return this;
    }

}// class
