package org.jboss.loom.migrators._ext.process;

/**
 *  Interface for context. Maybe should be named ProcessingContext or so.
 */
public interface ProcessingStackItem {


    //Map<String, Object> getVariables();
    /**
     *  Through this method, contexts may provide variables to XSLT, JAXB and Groovy.
     *  Variables will be visible in $this context and all children contexts.
     */
    Object getVariable( String name );
    
} // class
