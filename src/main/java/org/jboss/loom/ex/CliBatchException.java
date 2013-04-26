package org.jboss.loom.ex;

import org.jboss.dmr.ModelNode;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class CliBatchException extends Exception {

    private ModelNode responseNode;

    public CliBatchException( String msg, ModelNode responseNode ) {
        super( msg );
        this.responseNode = responseNode;
    }


    public ModelNode getResponseNode() {
        return responseNode;
    }
    
}// class
