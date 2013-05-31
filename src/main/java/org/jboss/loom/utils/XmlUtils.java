package org.jboss.loom.utils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class XmlUtils {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger( XmlUtils.class );
    
    public static <T> T unmarshallBean( File docFile, String xpath, Class<T> cls ) throws MigrationException{
        return unmarshallBeans( docFile, xpath, cls ).get(0);
    }
        
    public static <T> List<T> unmarshallBeans( File docFile, String xpath, Class<T> cls ) throws MigrationException{
        
        List<T> beans = new LinkedList();
        DocumentBuilder docBuilder = Utils.createXmlDocumentBuilder();
        try {
            Document doc = docBuilder.parse(docFile);

            XPath xp = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xp.evaluate(xpath, doc, XPathConstants.NODESET);
            
            Unmarshaller unmarshaller = JAXBContext.newInstance(cls).createUnmarshaller();
            for( int i = 0; i < nodes.getLength(); i++ ) {
                Node node = nodes.item( i );
                T bean = (T) unmarshaller.unmarshal(node);
                beans.add( bean );
            }
        }
        catch( SAXException | IOException | XPathExpressionException | JAXBException ex ) {
            throw new MigrationException("Failed parsing bean from a XML file " + docFile.getPath() + ": " + ex.getMessage(), ex);
        }
        return beans;
    }
    
}// class
