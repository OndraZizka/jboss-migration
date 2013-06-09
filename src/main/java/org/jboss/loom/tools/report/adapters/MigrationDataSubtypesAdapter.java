package org.jboss.loom.tools.report.adapters;


import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.jboss.loom.ctx.MigrationData;
import org.jboss.loom.tools.report.beans.MigrationDataReportBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class MigrationDataSubtypesAdapter extends XmlAdapter<MigrationDataReportBean, MigrationData> {
    private static final Logger log = LoggerFactory.getLogger( MigrationDataSubtypesAdapter.class );

    @Override
    public MigrationDataReportBean marshal( MigrationData migData ) throws Exception {
        return MigrationDataReportBean.from( migData );
    }

    @Override
    public MigrationData unmarshal( MigrationDataReportBean v ) throws Exception {
        throw new UnsupportedOperationException( "Not supported." );
    }

}// class
