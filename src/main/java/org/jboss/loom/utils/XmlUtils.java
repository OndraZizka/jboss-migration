package org.jboss.loom.utils;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.jboss.loom.ex.MigrationException;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class XmlUtils {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger( XmlUtils.class );
    
    public static <T> T unmarshallBean( File docFile, String xpath, Class<T> cls ) throws MigrationException{
        
        DocumentBuilder docBuilder = Utils.createXmlDocumentBuilder();
        try {
            Document doc = docBuilder.parse(docFile);

            XPath xp = XPathFactory.newInstance().newXPath();
            String exp = "/deployment/bean[@name='BootstrapProfileFactory']/property[@name='applicationURIs']//list[@elementClass='java.net.URI']";
            Node node = (Node) xp.evaluate(exp, doc, XPathConstants.NODE);

            Unmarshaller unmarshaller = JAXBContext.newInstance(cls).createUnmarshaller();
            T bean = (T) unmarshaller.unmarshal(node);
            return bean;
        }
        catch( SAXException | IOException | XPathExpressionException | JAXBException ex ) {
            throw new MigrationException("Failed parsing bean from a XML file " + docFile.getPath() + ": " + ex.getMessage(), ex);
        }
    }
    
}// class
