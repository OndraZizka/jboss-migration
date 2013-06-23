package org.jboss.loom.migrators._ext;


import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.oxm.annotations.XmlLocation;
import org.xml.sax.Locator;

/**
 *  Serves as a common base for all stackable items which contain each other.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ContainerOfStackableDefs {

    @XmlElement
    public String filter;   // A Groovy expression to filter the items.

    @XmlElement
    public String warning;  // Warning to add to the current action.

    @XmlElement(name = "action")
    List<MigratorDefinition.ActionDef> actionDefs;

    @XmlElement(name = "forEach")
    List<MigratorDefinition.ForEachDef> forEachDefs;

    @XmlLocation
    @XmlTransient
    Locator location;
    
    boolean hasForEachDefs(){ return forEachDefs != null && forEachDefs.isEmpty(); }
    boolean hasActionDefs(){ return actionDefs != null && actionDefs.isEmpty(); };    

}// class
