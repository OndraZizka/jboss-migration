package org.jboss.loom.migrators._ext.process;

import java.util.HashMap;
import java.util.Map;

/**
 *  Base class for stackable contexts which have variables map. Currently only RootContext.
 */
class Variables {
    
    private Map<String, Object> variables = new HashMap();


    public Map<String, Object> getVariables() { return this.variables; }

    public Variables setVariable( String name, Object value ) {
        this.variables.put( name, value );
        return this;
    }

}// class
