package org.jboss.loom.migrators._ext.process;

import java.util.Stack;
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

    
    /**
     *  Adds a warning to the nearest context able to take it (most likely, an Action).
     * 
     *  @throws IllegalArgumentException  If none of parent items can have warnings.
     */
    void addWarning( String warnStr ) throws IllegalArgumentException {
        
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
     *  Formats a string representing the current stack.
     */
    private String formatCurrentPosition() {
        StringBuilder sb = new StringBuilder();
        for( ProcessingStackItem item : stack ) {
            sb.append("    ").append(item);
        }
        return sb.toString();
    }
    
}// class
