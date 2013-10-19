package org.jboss.loom.migrators._ext.process;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.script.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Variables stack based binding for JSR-223 Scripting API.
 *  That binding extends Map<String, Object>.
 *  This one is read only, so it throws for all methods except get-related.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class StackScriptVariablesBinding implements Bindings {
    private static final Logger log = LoggerFactory.getLogger( StackScriptVariablesBinding.class );
    
    
    private final ContextsStack stack;


    StackScriptVariablesBinding( ContextsStack stack ) {
        this.stack = stack;
    }


    @Override public Object put( String name, Object value ) {
        throw new UnsupportedOperationException( "This Bindings is read only." );
    }

    @Override public void putAll( Map<? extends String, ? extends Object> toMerge ) {
        throw new UnsupportedOperationException( "This Bindings is read only." );
    }


    @Override public boolean containsKey( Object key ) {
        return get( key ) != null;
    }


    @Override public Object get( Object key ) {
        // TODO
        return null;
    }


    @Override public Object remove( Object key ) {
        throw new UnsupportedOperationException( "This Bindings is read only." );
    }


    @Override public int size() {
        throw new UnsupportedOperationException( "Size is unknown." );
    }


    @Override public boolean isEmpty() {
        throw new UnsupportedOperationException( "Size is unknown." );
    }


    @Override public boolean containsValue( Object value ) {
        throw new UnsupportedOperationException( "Value search not supported." );
    }

    @Override public void clear() {
        throw new UnsupportedOperationException( "This Bindings is read only." );
    }


    @Override public Set<String> keySet() {
        throw new UnsupportedOperationException( "Variables listing not supported." );
    }


    @Override public Collection<Object> values() {
        throw new UnsupportedOperationException( "Value listing not supported." );
    }


    @Override public Set<Map.Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException( "Variables listing not supported." );
    }    

}// class
