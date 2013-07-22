package org.jboss.loom.migrators._ext;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;
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
        public String file;
        public File getFile() { return new File(file); }

        @Override public String toString() { return "JaxbClassDef{ " + file + " }"; }
    }
    

    //@XmlRootElement
    //@XmlDiscriminatorNode("@type") // moved to ContainerOfStackableDefs
    @XmlSeeAlso({ CliActionDef.class, ModuleActionDef.class, CopyActionDef.class, XsltActionDef.class, ManualActionDef.class })
    public static class ActionDef extends ContainerOfStackableDefs {
        //@XmlAttribute
        //@XmlReadOnly
        public String typeVal;
        
        //public List<PropertyBean> properties;
        //@XmlAnyAttribute
        public Map<String, String> attribs = new HashMap();
    }
    
    @XmlRootElement
    @XmlDiscriminatorValue("cli")
    public static class CliActionDef extends ActionDef {
        /** CLI command. EL. */
        @XmlAttribute public String command;
    }

    @XmlRootElement
    @XmlDiscriminatorValue("manual")
    public static class ManualActionDef extends ActionDef {
    }

    @XmlRootElement
    @XmlDiscriminatorValue("module")
    public static class ModuleActionDef extends ActionDef {
        /** Module name, eg "com.mysql.jdbc.driver". EL. */
        @XmlAttribute public String name;
        /** Path to a .jar file of the module. EL. */
        @XmlAttribute public String jarPath;
        /** List of dependencies. EL. */
        @XmlAttribute @XmlList List<String> deps;
    }

    public static class FileBasedActionDef extends ActionDef {
        /** Path mask. Ant-like wildcards, EL. */
        @XmlAttribute public String pathMask;
        
        /** Where to store the result. May be a dir or a file. EL. */
        @XmlAttribute public String dest;
    }

    @XmlRootElement
    @XmlDiscriminatorValue("copy")
    public static class CopyActionDef extends FileBasedActionDef {
        @Override public String toString() { return "Copy from " + this.pathMask + " to " + this.dest; }
    }

    @XmlRootElement
    @XmlDiscriminatorValue("xslt")
    public static class XsltActionDef extends FileBasedActionDef {
        /** XSLT template path. */
        @XmlAttribute public String xslt;
        
        @Override public String toString() { return "XSLT from " + this.pathMask + " to " + this.dest + " using " + xslt; }
    }
    
    
    
    @XmlRootElement
    public static class ForEachDef extends ContainerOfStackableDefs {
        
        @NotBlank @XmlAttribute(name = "query")
        public String queryName;
        
        @NotBlank @XmlAttribute(name = "var")
        public String variableName;

        @Override public String toString() { return "forEach " + variableName + " in " + queryName; }
    }

    
    @XmlRootElement
    public static class XmlFileQueryDef extends FileQueryBase {
        @NotNull
        @XmlAttribute(name = "jaxbBean") public String jaxbBeanAlias;
        @XmlAttribute public String xpath;
        
        @Override public String toString() { return "XPath $" + jaxbBeanAlias + " from files " + this.pathMask + " " + xpath; }
    }

    @XmlRootElement
    public static class PropFileQueryDef extends FileQueryBase {
        @XmlAttribute public String propNameMask;
        
        @Override public String toString() { return "Property " + propNameMask + " from files " + this.pathMask; }
    }
    
    
    private static class FileQueryBase extends QueryBase {
        @NotBlank @XmlAttribute public String pathMask;     // Path mask of the files to load.
    }
    
    private static class QueryBase {
        @NotBlank @XmlAttribute public String id;           // Id under which the result will be stored.
        @NotBlank @XmlAttribute public String subjectLabel; // What's being loaded - for exceptions and logging.
    }
    
}// class
