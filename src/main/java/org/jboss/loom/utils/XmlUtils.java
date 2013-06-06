package org.jboss.loom.utils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
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
    
    /**
     *  Creates this app's standard marshaller.
     *  TODO: Use it in methods below.
     */
    public static Marshaller createMarshaller( Class cls ) throws JAXBException {
        Map<String, Object> props = new HashMap();
        props.put( Marshaller.JAXB_FORMATTED_OUTPUT, true );
        props.put( Marshaller.JAXB_ENCODING, "UTF-8");
        //marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.jboss.org/schema/swanloom.xsd swanloom.xsd");
        JAXBContext jc = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[]{cls}, props);
        
        // JDK way: Marshaller mar = JAXBContext.newInstance(MigrationReportJaxbBean.class).createMarshaller();
        
        return jc.createMarshaller();
    }
    
    /**
     *  Convenience - calls the override with must = true.
     */
    public static <T> T unmarshallBean( File docFile, String xpath, Class<T> cls ) throws MigrationException{
        return unmarshallBean( true, docFile, xpath, cls );
    }
    
    /**
     *  Read XML from the File, look for nodes by XPath, and unmarshall them into given Class.
     *  If Class is Origin.Wise, the origin is stored.
     */
    public static <T> T unmarshallBean( boolean must, File docFile, String xpath, Class<T> cls ) throws MigrationException{
        if( ! docFile.exists() ){
            if( must )  throw new MigrationException("File to unmarshall not found: " + docFile);
            else        return null;
        }
            
        List<T> beans = unmarshallBeans( docFile, xpath, cls );
        if( beans.isEmpty() )
            throw new MigrationException("XPath "+xpath+" returned no nodes from " + docFile);
        return beans.get(0);
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
            throw new MigrationException("Failed parsing bean from a XML file " + docFile.getPath() + ":\n    " + ex.getMessage(), ex);
        }
        return beans;
    }
    
    
    /**
     *  Convenience - calls the override with must = true.
     */
    public static <T> T readXmlConfigFile( File file, String xpath, Class<T> cls, String confAreaDesc ) throws MigrationException{
        return readXmlConfigFile( true, file, xpath, cls, confAreaDesc );
    }
    
    /**
     *  Reads given XML file, finds the first node matching given XPath, and reads it using given JAXB class.
     *  @param confAreaDesc  Used for exception message.
     *  @throws MigrationException wrapping any Exception.
     */
    public static <T> T readXmlConfigFile( boolean must, File file, String xpath, Class<T> cls, String confAreaDesc ) throws MigrationException{
        if( ! file.exists() ){
            if( must )  throw new MigrationException("File to unmarshall not found: " + file);
            else        return null;
        }
            
        try {
            return XmlUtils.unmarshallBean( must, file, xpath, cls );
        } catch( Exception ex ) {
            throw new MigrationException("Failed loading "+confAreaDesc+" config from "+file.getPath()+":\n    " + ex.getMessage(), ex);
        }
    }

    public static <T> List<T> readXmlConfigFileMulti( File file, String xpath, Class<T> cls, String confAreaDesc ) throws MigrationException{
        return readXmlConfigFileMulti( true, file, xpath, cls, confAreaDesc );
    }
    
    /**
     *  Reads given XML file, finds all nodes matching given XPath, and reads them into a list using given JAXB class.
     *  @param confAreaDesc  Used for exception message.
     *  @throws MigrationException wrapping any Exception.
     */
    public static <T> List<T> readXmlConfigFileMulti( boolean must, File file, String xpath, Class<T> cls, String confAreaDesc ) throws MigrationException{
        if( ! file.exists() ){
            if( must )  throw new MigrationException("File to unmarshall not found: " + file);
            else        return null;
        }
            
        try {
            return XmlUtils.unmarshallBeans( file, xpath, cls );
        } catch( Exception ex ) {
            throw new MigrationException("Failed loading "+confAreaDesc+" config from "+file.getPath()+":\n    " + ex.getMessage(), ex);
        }
    }

    public static <T> List<T> readXmlConfigFiles( File baseDir, String filesPattern, String xpath, Class<T> cls, String confAreaDesc ) throws MigrationException{
        if( ! baseDir.exists() )
            return Collections.EMPTY_LIST;
            
        List<File> files;
        try {
            //files = new PatternDirWalker( filesPattern ).list( baseDir );
            files = new DirScanner( filesPattern ).list( baseDir );
        } catch( IOException ex ) {
            throw new MigrationException("Failed finding files matching '"+filesPattern+"' in " + baseDir + ":\n  " + ex.getMessage(), ex);
        }
        
        List<T> res = new LinkedList();
        for( File file : files ) {
            try {
                res.addAll( XmlUtils.unmarshallBeans( new File(baseDir, file.getPath()), xpath, cls ) );
            } catch( Exception ex ) {
                throw new MigrationException("Failed loading "+confAreaDesc+" config from "+file.getPath()+":\n    " + ex.getMessage(), ex);
            }
        }
        return res;
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
