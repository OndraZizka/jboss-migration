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
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.migrators.mail.MailServiceBean;
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
    
    /**
     *  Read XML from the File, look for nodes by XPath, and unmarshall them into given Class.
     *  If Class is Origin.Wise, the origin is stored.
     */
    public static <T> List<T> unmarshallBeans( File docFile, String xpath, Class<T> cls ) throws MigrationException{
        
        List<T> beans = new LinkedList();
        DocumentBuilder docBuilder = Utils.createXmlDocumentBuilder();
        try {
            // Parse
            Document doc = docBuilder.parse(docFile);

            // XPath
            XPath xp = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xp.evaluate(xpath, doc, XPathConstants.NODESET);
            
            final Origin orig = new Origin( docFile, xpath );
            
            // Unmarshall
            Unmarshaller unmarshaller = JAXBContext.newInstance(cls).createUnmarshaller();
            for( int i = 0; i < nodes.getLength(); i++ ) {
                Node node = nodes.item( i );
                T bean = (T) unmarshaller.unmarshal(node);
                beans.add( bean );
                
                // Origin - set File and XPath.
                if( bean instanceof Origin.Wise ){
                    ((Origin.Wise) bean).setOrigin( orig ); //getOrigin().setFile( docFile ).setPart( xpath );
                }
            }
        }
        catch( SAXException | IOException | XPathExpressionException | JAXBException ex ) {
            throw new MigrationException("Failed parsing bean from a XML file " + docFile.getPath() + ": " + ex.getMessage(), ex);
        }
        return beans;
    }
    
    
    public static <T> T readXmlConfigFile( File file, String xpath, Class<T> cls, String confAreaDesc) throws MigrationException{
        T bean;
        try {
            bean = XmlUtils.unmarshallBean( file, xpath, cls);
        } catch( Exception ex ) {
            throw new MigrationException("Failed loading "+confAreaDesc+" config from "+file.getPath()+": " + ex.getMessage(), ex);
        }
        return bean;
    }

    
    
    public static void main( String[] args ) throws MigrationException {
        List<MailServiceBean> beans = unmarshallBeans( 
            new File("/home/ondra/work/AS/Migration/git-repo/testdata/as5configs/01_510all/server/all/deploy/mail-service.xml"), 
            "/server/mbean[@code='org.jboss.mail.MailService']", MailServiceBean.class);
        for( MailServiceBean ms : beans) {
            System.out.println( ms.getJndiName() );
        }
    }
    
}// class
