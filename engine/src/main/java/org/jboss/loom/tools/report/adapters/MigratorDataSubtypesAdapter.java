package org.jboss.loom.tools.report.adapters;


import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.jboss.loom.ctx.MigratorData;
import org.jboss.loom.tools.report.beans.MigratorDataReportBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class MigratorDataSubtypesAdapter extends XmlAdapter<MigratorDataReportBean, MigratorData> {
    private static final Logger log = LoggerFactory.getLogger( MigratorDataSubtypesAdapter.class );

    @Override
    public MigratorDataReportBean marshal( MigratorData migData ) throws Exception {
        return MigratorDataReportBean.from( migData );
    }

    @Override
    public MigratorData unmarshal( MigratorDataReportBean v ) throws Exception {
        throw new UnsupportedOperationException( "Not supported." );
    }

}// class
