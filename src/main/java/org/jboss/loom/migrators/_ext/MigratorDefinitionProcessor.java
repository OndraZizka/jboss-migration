package org.jboss.loom.migrators._ext;


import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.jboss.dmr.ModelNode;
import org.jboss.loom.actions.CliCommandAction;
import org.jboss.loom.actions.CopyFileAction;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.actions.ManualAction;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.utils.XmlUtils;
import org.jboss.loom.utils.el.IExprLangEvaluator;
import org.jboss.loom.utils.el.IExprLangEvaluator.JuelCustomResolverEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Processor for migrator definitions from .mig.xml files.
 *  These define:
 * 
 *    * JAXB classes, coded as Groovy files.
 *    * Queries which result in a list of IConfigFragment (typically, backed by those JAXB classes).
 *    * ForEach iterators which iterate over results of queries.
 *      * ForEach's define a variable which is available in EL and sub-contexts.
 * 
 *    * Nested elements which result in actions.
 *      * These actions can depend on each other.
 *      * Actions may contain warnings.
 *      * Some strings may contain EL expressions - e.g. ${varName.bean.path}
 * 
 *  TODO:
 * 
 *  This is the first attempt on implementation. It's a bit too procedural I guess and could be rewritten to something smarter,
 *  perhaps Ant tasks engine could be used as a basis.
 * 
 *  Other thing to improve could be to process the instructions in the order of appearance in the XML file.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class MigratorDefinitionProcessor implements IExprLangEvaluator.IVariablesProvider {
    private static final Logger log = LoggerFactory.getLogger( MigratorDefinitionProcessor.class );
    
    private Stack<ProcessingStackItem> stack = new Stack();
    
    private final DefinitionBasedMigrator dbm;
    
    private JuelCustomResolverEvaluator eval = new JuelCustomResolverEvaluator( this );


    MigratorDefinitionProcessor( DefinitionBasedMigrator dbm ) {
        this.stack.push( (ProcessingStackItem) new RootContext().setVariable("mig", dbm).setVariable("conf", dbm.getConfig()));
        this.dbm = dbm;
    }
    
    
    /**
     *  Look up the top-most variable on the stack. Return null if not found.
     *  I decided to prefer this method over recursive calls down the stack, using parentContext reference,
     *  because this way I have more control. Could become handy later.
     */
    public Object getVariable( String name ){
        for( ProcessingStackItem context : stack ) {
            Object var = context.getVariable( name );
            if( var != null )
                return var;
        }
        return null;
    }


    /**
     *  Recursively processes the .mig.xml descriptor into Actions.
     *  TODO: Could be better to put the stack manipulation to the beginning and end of the method.
     */
    List<IMigrationAction> processChildren( ContainerOfStackableDefs cont ) throws MigrationException {
        
        List<IMigrationAction> actions = new LinkedList();
        
        // ForEach defs.
        if( cont.hasForEachDefs() )
        for( MigratorDefinition.ForEachDef forEachDef : cont.forEachDefs ) {
            
            DefinitionBasedMigrator.ConfigLoadResult queryResult = this.dbm.getQueryResultByName( forEachDef.queryName ); 
            if( null == queryResult )
                throw new MigrationException("Query '"+forEachDef.queryName+"' not found. Needed at " + XmlUtils.formatLocation(forEachDef.location));
            
            ForEachContext forEachContext = new ForEachContext(forEachDef);
            this.stack.push( forEachContext );
            
            // For each item in query result...
            //for( IConfigFragment configFragment : queryResult.configFragments ) {
            for( IConfigFragment configFragment : forEachContext ) {
                // A variable is set automatically in next().
                
                // Recurse.
                this.processChildren( forEachDef );
            }
            this.stack.pop();
        }
        
        // Action definitions.
        if( cont.hasActionDefs() )
        for( MigratorDefinition.ActionDef actionDef : cont.actionDefs ) {
            IMigrationAction action;
            switch( actionDef.type ){
                case "manual":
                    action = new ManualAction();
                    // warning
                    // forEach
                    break;
                case "copy": 
                    String src = actionDef.attribs.get("src");
                    String dest = actionDef.attribs.get("dest");
                    CopyFileAction.IfExists ifExists = CopyFileAction.IfExists.valueOf("ifExists");
                    action = new CopyFileAction( DefinitionBasedMigrator.class, new File(src), new File(dest), ifExists ); 
                    break;
                case "cli": 
                    String cliScript = actionDef.attribs.get("cliScript");
                    ModelNode modelNode = ModelNode.fromString( cliScript );
                    action = new CliCommandAction( DefinitionBasedMigrator.class, cliScript, modelNode ); 
                    break;
                default: 
                    throw new MigrationException("Unsupported action type '" + actionDef.type + "' in " + cont.location.getSystemId());
            }
            
            // Recurse
            this.stack.push( new ActionContext( action ) );
            this.processChildren( actionDef );
            this.stack.pop();
            
            actions.add( action );
        }
        return actions;
    }// process();
    
    

    /**
     *  Interface for context. Maybe should be named ProcessingContext or so.
     */
    public static interface ProcessingStackItem {
        //Map<String, Object> getVariables();
        
        /**
         *  Through this method, contexts may provide variables to XSLT, JAXB and Groovy.
         *  Variables will be visible in $this context and all children contexts.
         */
        Object getVariable( String name );
    }
    
    public static interface HasWarnings {
        void addWarning( String warn );
        List<String> getWarnings();
    }
    
    public static interface HasActions {
        void addAction( IMigrationAction action );
        List<IMigrationAction> getActions();
    }
    
    
    
    /**
     *  Root context to collect the actions to.
     */
    public static class RootContext extends Variables implements ProcessingStackItem, HasActions, HasWarnings {
        
        private List<IMigrationAction> actions = new LinkedList();
        private List<String> warnings = new LinkedList();
        @Override public void addAction( IMigrationAction action ) { this.actions.add( action ); }
        @Override public List<IMigrationAction> getActions() { return actions; }
        @Override public void addWarning( String warn ) { this.warnings.add( warn ); }
        @Override public List<String> getWarnings() { return warnings; }
        
        /**  Returns a ManualAction with warnings of the root context, or null if there were no warnings. */
        ManualAction convertWarningsToManualAction(){
            if( this.warnings.isEmpty() )
                return null;
            ManualAction action = new ManualAction();
            for( String warn : this.warnings ) {
                action.addWarning( warn );
            }
            return action;
        }
        
        @Override public Object getVariable( String name ){ return this.getVariable( name ); }
    }

    
    /**
     *  Action context - delegates actions and warnings to the referenced action.
     */
    public class ActionContext implements ProcessingStackItem, HasActions, HasWarnings {
        
        private IMigrationAction action;
        public ActionContext( IMigrationAction action ) {
            this.action = action;
        }
        
        @Override public void addAction( IMigrationAction action ) {
            action.addDependency( action );
        }
        
        @Override public List<IMigrationAction> getActions() { return action.getDependencies(); }
        
        @Override public void addWarning( String warn ) {
            action.getWarnings().add( warn );
        }

        @Override public List<String> getWarnings() { return action.getWarnings(); }

        //@Override public Map<String, Object> getVariables() { return null; }
        @Override public Object getVariable( String name ){ return null; }
    }
    
    
    /**
     *  ForEachContext passes most additions etc to the parent element.
     */
    class ForEachContext implements ProcessingStackItem, HasActions, HasWarnings, Iterable<IConfigFragment> {
        
        private final MigratorDefinition.ForEachDef def;
        private final Iterator<IConfigFragment> it;
        private IConfigFragment current = null;
        
        ForEachContext( MigratorDefinition.ForEachDef forEachDef ) {
            this.def = forEachDef;

            // Initialize the iterator.
            DefinitionBasedMigrator.ConfigLoadResult queryResult = MigratorDefinitionProcessor.this.dbm.getQueryResultByName( this.def.queryName );
            this.it = queryResult.configFragments.iterator();
        }

        // Iterator delegation.
        @Override
        public Iterator<IConfigFragment> iterator() {
            return new Iterator<IConfigFragment>() {
                @Override public boolean hasNext() { return it.hasNext(); }
                @Override public IConfigFragment next() { ForEachContext.this.current = it.next(); return ForEachContext.this.current; }
                @Override public void remove() { throw new UnsupportedOperationException("Remove not supported."); }
            };
        }
        
        // getVariable()
        @Override public Object getVariable( String name ) {
            if( ! def.variableName.equals( name ) )  return null;
            return this.current;
        }

        
        
        @Override
        public void addAction( IMigrationAction action ) {
            ProcessingStackItem top = MigratorDefinitionProcessor.this.stack.peek();
            if( ! (top instanceof HasActions))
                throw new IllegalArgumentException("It's not possible to add actions to " + top);
            ((HasActions)top).addAction( action );
        }


        @Override public List<IMigrationAction> getActions() {
            ProcessingStackItem top = MigratorDefinitionProcessor.this.stack.peek();
            if( ! (top instanceof HasActions))
                throw new IllegalArgumentException("Doesn't have actions: " + top);
            return ((HasActions)top).getActions();
        }


        @Override
        public void addWarning( String warn ) {
            ProcessingStackItem top = MigratorDefinitionProcessor.this.stack.peek();
            if( ! (top instanceof HasWarnings))
                throw new IllegalArgumentException("It's not possible to add warnings to " + top);
            ((HasWarnings)top).addWarning( warn );
        }


        @Override
        public List<String> getWarnings() {
            ProcessingStackItem top = MigratorDefinitionProcessor.this.stack.peek();
            if( ! (top instanceof HasWarnings))
                throw new IllegalArgumentException("Doesn't have warnings: " + top);
            return ((HasWarnings)top).getWarnings();
        }
    }
    
    
    /**
     *  Base class for stackable contexts which have variables map. Currently only RootContext.
     */
    private static class Variables {
        private Map<String, Object> variables;
        public Map<String, Object> getVariables() {
            return this.variables;
        }
        public Variables setVariable( String name, Object value ){
            if( this.variables == null ) this.variables = new HashMap();
            this.variables.put( name, value );
            return this;
        }
    }

}// class
