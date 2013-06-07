package org.jboss.loom.tools.report;


import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.FileUtils;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 *  Extracts report data from MigrationContext and dumps them to a XML file.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Reporter {
    public static final Logger log = LoggerFactory.getLogger( Reporter.class );
    
    private static final String XSLT_PATH = "/org/jboss/loom/tools/report/xslt/MigrationReportJaxbBean.xsl";

    
    public static void createReport( MigrationContext ctx, File reportDir ) throws MigrationException {
        try {
            // Create the reporting content.
            MigrationReportJaxbBean report = new MigrationReportJaxbBean();
            report.config = ctx.getConf();
            report.comparisonResult = ctx.getSourceServer().getHashesComparisonResult();
            report.configData = ctx.getMigrationData().values();
            report.actions = ctx.getActions();
            report.finalException = ctx.getFinalException();
            report.sourceServer = ctx.getSourceServer();
            
            Marshaller mar = XmlUtils.createMarshaller( MigrationReportJaxbBean.class );
            
            // File name
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());
            File reportFile = new File(reportDir, "MigrationReport-"+timestamp+".xml");
            FileUtils.forceMkdir( reportDir );
            
            // Write to a file.
            //log.debug("Writing the report to " + reportFile.getPath());
            //mar.marshal( report, reportFile );
            
            // Write to a Node.
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            mar.marshal( report, doc );
            
            // Write node to a file.
            log.debug("Storing the XML report to " + reportFile.getPath());
            XmlUtils.saveXmlToFile( doc, reportFile );
            
            // Use XSLT to produce HTML report.
            File htmlFile = new File( reportFile.getPath() + ".html");
            log.debug("Storing the HTML report to " + htmlFile.getPath());
            InputStream xsltIS = Reporter.class.getResourceAsStream(XSLT_PATH);
            XmlUtils.transformDocToFile( doc, htmlFile, xsltIS );
        }
        catch( Exception ex ) {
            log.error("AAAA!", ex);
            throw new MigrationException("Failed creating migration report:\n    " + ex.getMessage(), ex);
        }
    }
    
    
}// class
