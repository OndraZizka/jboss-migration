package org.jboss.loom.tools.report.adapters;


import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.tools.report.beans.ActionBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ToActionBeanAdapter extends XmlAdapter<ActionBean, IMigrationAction> {
    private static final Logger log = LoggerFactory.getLogger( ToActionBeanAdapter.class );


    @Override
    public ActionBean marshal( IMigrationAction action ) throws Exception {
        ActionBean ret = new ActionBean( action );        
        return ret;
    }

    @Override
    public IMigrationAction unmarshal( ActionBean v ) throws Exception {
        throw new UnsupportedOperationException("Not supported. Converts objects to their hashcode.");
    }

}// class
