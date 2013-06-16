package org.jboss.loom.migrators._groovy;


import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.jboss.dmr.ModelNode;
import org.jboss.loom.actions.CliCommandAction;
import org.jboss.loom.actions.CopyFileAction;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.actions.ManualAction;
import org.jboss.loom.ex.MigrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class MigratorDescriptorProcessor {
    private static final Logger log = LoggerFactory.getLogger( MigratorDescriptorProcessor.class );
    
    private Stack<ProcessingStackItem> stack = new Stack();
    
    private final DefinitionBasedMigrator dbm;


    MigratorDescriptorProcessor( DefinitionBasedMigrator dbm ) {
        this.stack.push( new RootContext() );
        this.dbm = dbm;
    }


    /**
     *  Recursively processes the .mig.xml descriptor into Actions.
     *  TODO: Could be better to put the stack manipulation to the beginning and end of the method.
     */
    List<IMigrationAction> process( ContainerOfStackableDefs cont ) throws MigrationException {
        
        List<IMigrationAction> actions = new LinkedList();
        
        // ForEach defs
        if( cont.hasForEachDefs() )
        for( MigratorDefinition.ForEachDef forEachDef : cont.forEachDefs ) {
            
            // Recurse
            this.stack.push( new ForEachContext(forEachDef) );
            this.process( forEachDef );
            this.stack.pop();
        }
        
        // Action definitions
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
            this.process( actionDef );
            this.stack.pop();
            
            actions.add( action );
        }
        return actions;
    }// process();
    
    

    public static interface ProcessingStackItem {}
    
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
    public static class RootContext implements ProcessingStackItem, HasActions, HasWarnings {
        
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
    }
    
    
    /**
     *  ForEachContext passes most additions etc to the parent element.
     */
    public class ForEachContext implements ProcessingStackItem, HasActions, HasWarnings {
        
        private final MigratorDefinition.ForEachDef def;

        private ForEachContext( MigratorDefinition.ForEachDef forEachDef ) {
            this.def = forEachDef;
        }

        
        @Override
        public void addAction( IMigrationAction action ) {
            ProcessingStackItem top = MigratorDescriptorProcessor.this.stack.peek();
            if( ! (top instanceof HasActions))
                throw new IllegalArgumentException("It's not possible to add actions to " + top);
            ((HasActions)top).addAction( action );
        }


        @Override public List<IMigrationAction> getActions() {
            ProcessingStackItem top = MigratorDescriptorProcessor.this.stack.peek();
            if( ! (top instanceof HasActions))
                throw new IllegalArgumentException("Doesn't have actions: " + top);
            return ((HasActions)top).getActions();
        }


        @Override
        public void addWarning( String warn ) {
            ProcessingStackItem top = MigratorDescriptorProcessor.this.stack.peek();
            if( ! (top instanceof HasWarnings))
                throw new IllegalArgumentException("It's not possible to add warnings to " + top);
            ((HasWarnings)top).addWarning( warn );
        }


        @Override
        public List<String> getWarnings() {
            ProcessingStackItem top = MigratorDescriptorProcessor.this.stack.peek();
            if( ! (top instanceof HasWarnings))
                throw new IllegalArgumentException("Doesn't have warnings: " + top);
            return ((HasWarnings)top).getWarnings();
        }
    }

}// class
