package org.jboss.loom.migrators._ext;

import java.io.File;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.validator.constraints.NotBlank;
import org.jboss.loom.migrators.Origin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  JAXB class for *.mig.xml files.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement( name="migrator" )
@XmlAccessorType( XmlAccessType.NONE )
public class MigratorDefinition extends ContainerOfStackableDefs implements Origin.Wise {
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
    
    //File fileOfOrigin;
    // @XmlLocation wouldn't work if loaded through Node.
    
    private Origin origin;
    @NotNull
    @Override public Origin getOrigin() { return origin; }
    @Override public MigratorDefinition setOrigin( Origin origin ) { this.origin = origin; return this; }


    @Override
    public String toString() {
        return "MigratorDefinition '"+ name +"' " + origin;
    }

    
    
    
    // === Subelement classes === //
    
    @XmlRootElement
    @XmlAccessorType( XmlAccessType.NONE )
    public static class JaxbClassDef {
        @XmlAttribute(name = "file")
        //@XmlJavaTypeAdapter( StringToFileAdapter.class )
        public File file;

        @Override public String toString() { return "JaxbClassDef{ " + file + " }"; }
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
        
        @NotBlank @XmlAttribute(name = "query")
        public String queryName;
        
        @NotBlank @XmlAttribute(name = "var")
        public String variableName;
    }

    
    @XmlRootElement
    public static class XmlFileQueryDef extends FileQueryBase {
        @NotNull
        @XmlAttribute(name = "jaxbBean") public String jaxbBeanAlias;
        @XmlAttribute public String xpath;
    }

    @XmlRootElement
    public static class PropFileQueryDef extends FileQueryBase {
        @XmlAttribute public String propNameMask;
    }
    
    
    private static class FileQueryBase extends QueryBase {
        @NotBlank @XmlAttribute public String pathMask;     // Path mask of the files to load.
    }
    
    private static class QueryBase {
        @NotBlank @XmlAttribute public String id;           // Id under which the result will be stored.
        @NotBlank @XmlAttribute public String subjectLabel; // What's being loaded - for exceptions and logging.
    }
    
}// class
