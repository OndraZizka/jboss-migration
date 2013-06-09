package org.jboss.loom.tools.report.adapters;


import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.tools.report.beans.ConfigFragmentReportBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class IConfigFragmentAdapter extends XmlAdapter<ConfigFragmentReportBean, IConfigFragment> {
    private static final Logger log = LoggerFactory.getLogger( IConfigFragment.class );



    @Override
    public ConfigFragmentReportBean marshal( IConfigFragment fragment ) throws Exception {
        return ConfigFragmentReportBean.from( fragment );
    }
    
    @Override
    public IConfigFragment unmarshal( ConfigFragmentReportBean v ) throws Exception {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

}// class
