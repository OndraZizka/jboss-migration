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
    public ProcessingStackItem push( ProcessingStackItem item ) {
        return stack.push( item );
    }

    public synchronized ProcessingStackItem pop() {
        return stack.pop();
    }

    public synchronized ProcessingStackItem peek() {
        return stack.peek();
    }
    
}// class
