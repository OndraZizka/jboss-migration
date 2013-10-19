package org.jboss.loom.migrators._ext.process;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class GroovyScriptUtils {
    private static final Logger log = LoggerFactory.getLogger( GroovyScriptUtils.class );


    /**
     *  Evaluates the given expression with given variables to a boolean.
     *  Proprietary Groovy way. Needs a passive variables map.
     */
    static boolean evaluateGroovyExpressionToBool( String script, ContextsStack stack ) {
        final GroovyShell gsh = new GroovyShell( new Binding(){
            
        });
        
        Object res;
        try {
            res = gsh.evaluate( script );
        } catch( CompilationFailedException ex ) {
            throw new IllegalArgumentException( "Filter Groovy code failed to compile: " + ex.getMessage(), ex );
        }
        if( !(res instanceof Boolean) ) {
            throw new IllegalArgumentException( "Filter Groovy doesn't evaluate to boolean." );
        }
        return (Boolean) res;
    }


    // JSR-223 way
    static boolean evaluateGroovyExpressionToBool2( String script, ContextsStack stack ) {
        ScriptEngineManager scMgr = new ScriptEngineManager();
        ScriptEngine engine = scMgr.getEngineByName( "groovy" );
        Object res;
        try {
            res = engine.eval( script, new StackScriptVariablesBinding( stack ) );
        } catch( ScriptException ex ) {
            throw new IllegalArgumentException( "Filter Groovy code failed to compile: " + ex.getMessage(), ex );
        }
        if( !(res instanceof Boolean) ) {
            throw new IllegalArgumentException( "Filter Groovy doesn't evaluate to boolean." );
        }
        return (Boolean) res;
    }

    

}// class
