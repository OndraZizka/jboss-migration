package org.jboss.loom.tools.report;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
    private static final Logger log = LoggerFactory.getLogger( Reporter.class );

    
    public static void createReport( MigrationContext ctx, File reportDir ) throws MigrationException {
        try {
            // Create the reporting content.
            MigrationReportJaxbBean report = new MigrationReportJaxbBean(
                ctx.getConf(),
                ctx.getSourceServer().getHashesComparisonResult(),
                ctx.getMigrationData().values(),
                ctx.getActions()
            );
            
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
            log.debug("Storing the report to " + reportFile.getPath());
            saveXmlToFile( doc, reportFile );
        }
        catch( Exception ex ) {
            log.error("AAAA!", ex);
            throw new MigrationException("Failed creating migration report:\n    " + ex.getMessage(), ex);
        }
    }
    
    
    public static void saveXmlToFile( Document doc, File file ) throws MigrationException {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult( file );
            Source input = new DOMSource(doc);
            transformer.transform(input, output);
        } catch( TransformerException ex ) {
            throw new MigrationException("Failed saving XML document to " + file.getPath()+":\n    " + ex.getMessage(), ex);
        }
    }

}// class
