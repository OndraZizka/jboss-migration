package org.jboss.loom.migrators._ext;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;

/**
 *  Built-in action definition JAXB bean classes.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ActionDefs {

    @XmlRootElement
    @XmlDiscriminatorValue("cli")
    public static class CliActionDef extends MigratorDefinition.ActionDef {
        /** CLI command. EL. */
        @XmlAttribute public String command;
    }

    
    @XmlRootElement
    @XmlDiscriminatorValue("manual")
    public static class ManualActionDef extends MigratorDefinition.ActionDef {
    }

    
    @XmlRootElement
    @XmlDiscriminatorValue("module")
    public static class ModuleActionDef extends MigratorDefinition.ActionDef {
        
        /** Module name, eg "com.mysql.jdbc.driver". EL. */
        @XmlAttribute public String name;
        
        /** Path to a .jar file of the module. EL. */
        @XmlAttribute(name = "jar") public String jarPath;
        
        /** List of dependencies. EL. */
        @XmlAttribute @XmlList public List<String> deps;
        
        /** What to do if the destination file already exists. */
        @XmlAttribute public String ifExists;
    }

    public static class FileBasedActionDef extends MigratorDefinition.ActionDef {
        
        /** Path mask. Ant-like wildcards, EL. */
        @XmlAttribute public String pathMask;
        
        /** Where to store the result. May be a dir or a file. EL. */
        @XmlAttribute public String dest;
        
        /** What to do if the destination file already exists. */
        @XmlAttribute public String ifExists;
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
    
}// class
