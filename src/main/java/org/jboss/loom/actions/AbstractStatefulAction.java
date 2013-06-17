/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.actions;

import java.util.HashSet;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.spi.IMigrator;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

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
        this.originStacktrace = Thread.currentThread().getStackTrace()[3];
        // 0 - Thread.getStackTrace().
        // 1 - This constructor.
        // 2 - *Action constructor.
        // 3 - Whatever called new CliCommandAction.
        // Could be better, e.g. first non-constructor after 2.
    }

    public AbstractStatefulAction( Class<? extends IMigrator> fromMigrator ) {
        this();
        this.fromMigrator = fromMigrator;
    }

    public AbstractStatefulAction addWarning(String text) {
        warnings.add(text);
        return this;
    }

    
    public void checkState(IMigrationAction.State... states) {
        for( State state : states ) {
            if (this.state == state)
                return;
        }
        throw new RuntimeException("Action not in expected states " + StringUtils.join( states, " " ) 
                + ", but in " +this.getState()+ ":\n    " + this.toDescription());
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


    /* ----- Dependency stuff ----- */
    @Override
    public List<IMigrationAction> getDependencies() {
        return this.deps;
    }

    @Override
    public IMigrationAction addDependency( IMigrationAction dep ) {
        this.deps.add( dep );
        return this;
    }
    
    /**
     *  {@inheritDoc}
     */
    @Override
    public int dependsOn( IMigrationAction other ) throws CircularDependencyException {
        
        Set<IMigrationAction> visited = new HashSet();
        visited.add( this );
        visited.add( other );
        
        return dependsOn( other, visited );
    }
        
    /**
     *  {@inheritDoc}
     */
    private int dependsOn( IMigrationAction other, Set<IMigrationAction> visited ) throws CircularDependencyException {
        
        if( this.getDependencies().isEmpty() )
            return -1;
        if( this.equals( other ) )
            return 0;
        if( this.getDependencies().contains( other ) )
            return 1;
        
        int minDist = Integer.MAX_VALUE;
        for( IMigrationAction dep : this.getDependencies() ){
            if( visited.contains( dep ) )
                throw new CircularDependencyException(this, dep);
            int dist = dep.dependsOn( other );
            if( dist > 0 )
                minDist = Math.min( dist, minDist );
        }
        if( minDist == Integer.MAX_VALUE )  return -1;
        else return minDist + 1;
    }
    
    public static class CircularDependencyException extends MigrationException {
        public CircularDependencyException( IMigrationAction a, IMigrationAction b ) {
            super("Circular dependency of actions - somewhere between these:\n\n" 
                    + a.toDescription() + "\n\n" + b.toDescription());
        }
    }
    
    
    /**
     *  Sorting nodes of one action's dependency tree.
     */
    public static List<IMigrationAction> sortNodes_LeavesFirst( IMigrationAction action ){
        List<IMigrationAction> retNodes = new LinkedList();
        sortNodes_LeavesFirst( action, retNodes );
        return retNodes;
    }
    
    private static void sortNodes_LeavesFirst( IMigrationAction action, List<IMigrationAction> retNodes ) {
        for( IMigrationAction dep : action.getDependencies() ){
            sortNodes_LeavesFirst( dep, retNodes );
        }
        retNodes.add( action );
    }
    
}// class
