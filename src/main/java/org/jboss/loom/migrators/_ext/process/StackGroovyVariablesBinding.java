package org.jboss.loom.migrators._ext.process;

import groovy.lang.Binding;
import java.util.Map;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Variables stack based binding for Groovy API.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class StackGroovyVariablesBinding extends Binding {
    private static final Logger log = LoggerFactory.getLogger( StackGroovyVariablesBinding.class );
    
    
    private final Stack<ProcessingStackItem> stack;


    StackGroovyVariablesBinding( Stack<ProcessingStackItem> stack ) {
        this.stack = stack;
    }


    @Override
    public Object getVariable( String name ) {
        // TODO
        return null;
    }


    @Override
    public void setVariable( String name, Object value ) {
        throw new UnsupportedOperationException("This Bindings is read only.");
    }


    @Override public boolean hasVariable( String name ) {
        return getVariable( name ) != null;
    }


    @Override public Map getVariables() {
        throw new UnsupportedOperationException("Variables listing is not supported.");
    }

}// class
