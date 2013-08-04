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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.eclipse.persistence.oxm.annotations.XmlReadOnly;
import org.hibernate.validator.constraints.NotBlank;
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.migrators._ext.ActionDefs.*;
import org.jboss.loom.utils.el.EL;

/**
 *  JAXB class for *.mig.xml files.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement( name="migrator" )
@XmlAccessorType( XmlAccessType.NONE )
public class MigratorDefinition extends ContainerOfStackableDefs implements Origin.Wise {
    
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

    
    // Get/set
    public String getName() { return name; }
   
    
    
    
    // === Subelement classes === //
    
    @XmlRootElement
    @XmlAccessorType( XmlAccessType.NONE )
    public static class JaxbClassDef {
        
        @XmlAttribute(name = "file")
        //@XmlJavaTypeAdapter( StringToFileAdapter.class )
        @EL public String file;
        public File getFile() { return new File(file); }

        @Override public String toString() { return "JaxbClassDef{ " + file + " }"; }
    }
    

    //@XmlRootElement
    //@XmlDiscriminatorNode("@type") // moved to ContainerOfStackableDefs
    @XmlSeeAlso({ CliActionDef.class, ModuleActionDef.class, CopyActionDef.class, XsltActionDef.class, ManualActionDef.class })
    public static class ActionDef extends ContainerOfStackableDefs {
        
        @XmlAttribute
        @XmlReadOnly
        public String typeVal;

        @XmlAttribute(name = "var")
        public String varName;
        
        //public List<PropertyBean> properties;
        //@XmlAnyAttribute
        public Map<String, String> attribs = new HashMap();

        @Override public String toString() {
            return this.getClass().getSimpleName() + "{ type: " + typeVal + " var: " + varName + "}";
        }
    }
    
    
    
    @XmlRootElement
    public static class ForEachDef extends ContainerOfStackableDefs {
        
        @NotBlank @XmlAttribute(name = "query")
        public String queryName;
        
        @NotBlank @XmlAttribute(name = "var")
        public String variableName;

        @Override public String toString() { return "forEach $" + variableName + " in $" + queryName; }
    }

    
    @XmlRootElement
    public static class XmlFileQueryDef extends FileQueryBase {
        
        @NotNull
        @EL @XmlAttribute(name = "jaxbBean") public String jaxbBeanAlias;
        
        @EL @XmlAttribute public String xpath;
        
        @Override public String toString() { return "Unmarshall $" + jaxbBeanAlias + " from files " + this.pathMask + ": " + xpath; }
    }

    @XmlRootElement
    public static class PropFileQueryDef extends FileQueryBase {
        @EL @XmlAttribute public String propNameMask;
        
        @Override public String toString() { return "Property " + propNameMask + " from files " + this.pathMask; }
    }
    
    
    private static class FileQueryBase extends QueryBase {
        
        @EL           @XmlAttribute public String baseDir;     // What the pathMask is relative to.
        
        @EL @NotBlank @XmlAttribute public String pathMask;     // Path mask of the files to load.
    }
    
    private static class QueryBase {
        @NotBlank @XmlAttribute public String id;           // Id under which the result will be stored.
        @EL @NotBlank @XmlAttribute public String subjectLabel; // What's being loaded - for exceptions and logging.
    }
    
}// class
