package org.jboss.loom.migrators._ext;


import java.io.File;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement( name="migrator" )
@XmlAccessorType( XmlAccessType.NONE )
public class MigratorDefinition extends ContainerOfStackableDefs {
    private static final Logger log = LoggerFactory.getLogger( MigratorDefinition.class );
    
    @XmlAttribute
    String name;

    //@XmlElementWrapper(name = "jaxbBeans")
    @XmlElement(name = "jaxbBean")
    //@XmlPath("jaxbBean/@file")
    List<JaxbClassDef> jaxbBeansClasses;
    
    @XmlElement(name = "xmlQuery")
    List<XmlFileQueryDef> xmlQueries;
    
    @XmlElement(name = "propQuery")
    List<PropFileQueryDef> propQueries;
    
    //@XmlElement(name = "action")
    //List<ActionDef> actions;
    
    File fileOfOrigin;
    

    
    // === Subelement classes === //
    
    
    @XmlRootElement
    @XmlAccessorType( XmlAccessType.NONE )
    public static class JaxbClassDef {
        @XmlAttribute(name = "file")
        //@XmlJavaTypeAdapter( StringToFileAdapter.class )
        public File file;
    }
    

    @XmlRootElement
    public static class ActionDef extends ContainerOfStackableDefs {
        @XmlAttribute
        public String type;
        
        //public List<PropertyBean> properties;
        @XmlAnyAttribute
        public Map<String, String> attribs;
    }
    
    @XmlRootElement
    public static class ForEachDef extends ContainerOfStackableDefs {
        
        @XmlAttribute(name = "query")
        public String queryName;
        
        @XmlAttribute(name = "var")
        public String variableName;
    }

    
    @XmlRootElement
    public static class XmlFileQueryDef extends FileQueryBase {
        public Class jaxbBean;
        public String xpath;
    }

    @XmlRootElement
    public static class PropFileQueryDef extends FileQueryBase {
        public String propNameMask;
    }
    
    
    private static class FileQueryBase extends QueryBase {
        public String pathMask;     // Path mask of the files to load.
    }
    
    private static class QueryBase {
        public String id;           // Id under which the result will be stored.
        public String subjectLabel; // What's being loaded - for exceptions and logging.
    }
    
}// class
