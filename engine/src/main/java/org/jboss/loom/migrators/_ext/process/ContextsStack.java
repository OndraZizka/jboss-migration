package org.jboss.loom.migrators._ext.process;

import java.util.Stack;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.utils.el.IExprLangEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ContextsStack  implements IExprLangEvaluator.IVariablesProvider {
    private static final Logger log = LoggerFactory.getLogger( ContextsStack.class );

    private final Stack<ProcessingStackItem> stack = new Stack();
    
    
    /**
     *  Look up the top-most variable on the stack. Return null if not found.
     *  I decided to prefer this method over recursive calls down the stack, using parentContext reference,
     *  because this way I have more control. Could become handy later.
     */
    @Override public Object getVariable( String name ){
        for( ProcessingStackItem context : this.stack ) {
            Object var = context.getVariable( name );
            if( var != null )
                return var;
        }
        return null;
    }
    

    // --- Delegations. ---
    public ProcessingStackItem push( ProcessingStackItem item ) { return stack.push( item ); }
    public synchronized ProcessingStackItem pop() { return stack.pop(); }
    public synchronized ProcessingStackItem peek() { return stack.peek(); }
    public synchronized int size() { return stack.size(); }

    
    /**
     *  Adds a warning to the nearest context able to take it (most likely, an Action).
     * 
     *  @throws IllegalArgumentException  If none of parent items can have warnings.
     */
    private void addWarning( String warnStr ) throws IllegalArgumentException {
        
        for( int i = this.stack.size()-1; i >= 0; i-- ) {  // Could use Guava's Lists.reverse() view.
            ProcessingStackItem item = this.stack.get( i );
            if( ! (item instanceof Has.Warnings ) )
                continue;
            
            ((Has.Warnings)item).addWarning( warnStr );
            return;
        }
        
        throw new IllegalArgumentException("None of parent items can have warnings.\n    Current position: " + this.formatCurrentPosition());
    }

    /**
     *  Adds a warning to the nearest context able to take it (most likely, an Action).
     * 
     *  @throws IllegalArgumentException  If none of parent items can have warnings.
     */
    private void addAction( IMigrationAction action ) throws IllegalArgumentException {
        
        for( int i = this.stack.size()-1; i >= 0; i-- ) {  // Could use Guava's Lists.reverse() view.
            ProcessingStackItem item = this.stack.get( i );
            if( ! (item instanceof Has.Actions ) )
                continue;
            
            ((Has.Actions)item).addAction( action );
            return;
        }
        
        throw new IllegalArgumentException("None of parent items can have actions.\n    Current position: " + this.formatCurrentPosition());
    }


    /**
     *  Formats a string representing the current stack.
     */
    private String formatCurrentPosition() {
        StringBuilder sb = new StringBuilder();
        for( ProcessingStackItem item : stack ) {
            sb.append("    ").append(item);
        }
        return sb.toString();
    }


    /**
     *  Propagates the argument up the stack and offers it to all contexts, until one takes it.
     *  Returns whether the item was accepted by any context.
     */
    boolean propagate( IPropagable whatever ) {
        // Now this is cheating... it should go through some IContext's offer() or such.
        if( whatever instanceof IMigrationAction ){
            this.addAction( (IMigrationAction) whatever );
        }
        else if( whatever instanceof Warning ){
            this.addWarning( ((Warning) whatever).text );
        }
        
        // MIGR-153 - Currently there are only actions and warnings, so we keep it simple,
        //            but what it should really look like is:
        if( 1 < 0 ){
            boolean accepted = false;
            for( int i = this.stack.size()-1; i >= 0; i-- ) {
                ProcessingStackItem item = this.stack.get( i );
                //accepted |= item.offer( item, accepted );
            }
            return accepted;
        }
        
        return true;
    }
    
    
    
    
    /**
     *  Marker interface for things which can be offered to the migration definition contexts on the stack.
     */
    public static interface IPropagable {}
    
    /**
     *  Currently only used for this IPropagable concept fake implementation, so leaving it here.
     *  In the future it should be somewhere in org.jboss.loom.actions or around.
     */
    static class Warning implements IPropagable {
        public String text;

        public Warning( String text ) {
            this.text = text;
        }
    }
    
}// class
