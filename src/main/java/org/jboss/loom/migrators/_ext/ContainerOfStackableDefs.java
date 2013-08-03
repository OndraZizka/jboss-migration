package org.jboss.loom.migrators._ext;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorNode;
import org.jboss.loom.utils.el.EL;

/**
 *  Serves as a common base for all stackable items which contain each other.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlDiscriminatorNode("@type")
public class ContainerOfStackableDefs {

    @XmlElement
    public String filter;   // A Groovy expression to filter the items.

    @EL @XmlElement
    public String warning;  // Warning to add to the current action.

    
    @XmlElement(name = "action")
    List<MigratorDefinition.ActionDef> actionDefs;

    @XmlElement(name = "forEach")
    List<MigratorDefinition.ForEachDef> forEachDefs;

    
    //@XmlLocation
    //@XmlTransient
    //public Locator location;

    
    public boolean hasForEachDefs(){ return forEachDefs != null && ! forEachDefs.isEmpty(); }
    public List<MigratorDefinition.ForEachDef> getForEachDefs() { return forEachDefs; }
    
    public boolean hasActionDefs(){ return actionDefs != null && ! actionDefs.isEmpty(); };    
    public List<MigratorDefinition.ActionDef> getActionDefs() { return actionDefs; }

}// class
