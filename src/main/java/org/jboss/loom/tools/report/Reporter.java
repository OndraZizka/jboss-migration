package org.jboss.loom.tools.report;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import org.apache.commons.io.FileUtils;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ex.MigrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Reporter {
    private static final Logger log = LoggerFactory.getLogger( Reporter.class );

    
    public static void createReport( MigrationContext ctx, File reportDir ) throws MigrationException {
        try {
            MigrationReportJaxbBean report = new MigrationReportJaxbBean(
                ctx.getConf(),
                ctx.getSourceServer().getHashesComparisonResult(),
                ctx.getActions()
            );
            
            Marshaller mar = JAXBContext.newInstance(MigrationReportJaxbBean.class).createMarshaller();
            mar.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
            mar.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            //marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.jboss.org/schema/swanloom.xsd swanloom.xsd");
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());
            File reportFile = new File(reportDir, "MigrationReport-"+timestamp+".xml");
            
            log.debug("Writing to " + reportFile.getPath());
            FileUtils.forceMkdir( reportDir );
            mar.marshal( report, reportFile );
        }
        catch( Exception ex ) {
            log.error("AAAA!", ex);
            throw new MigrationException("Failed creating migration report:\n    " + ex.getMessage(), ex);
        }
    }
    

}// class
