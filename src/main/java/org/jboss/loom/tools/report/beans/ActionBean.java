package org.jboss.loom.tools.report.beans;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.spi.IMigrator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jboss.loom.tools.report.adapters.ToHashCodeAdapter;

/**
 *   Wraps an action.
 *   Because JAXB needs getters (can't read from just any methods),
 *   Weaving it with JAXB required too much changes to the interface and actions.
 */
@XmlRootElement(name="action")
@XmlAccessorType( XmlAccessType.NONE )
public class ActionBean {

    @XmlElement(name = "id")
    @XmlID
    private String hashCode;
    
    @XmlElement(name = "originMsg")
    private String originMessage;
    
    @XmlAttribute(name = "fromMigrator")
    private Class<? extends IMigrator> fromMigrator;
    
    @XmlElementWrapper(name = "dependencies")
    @XmlElement(name = "dep")
    @XmlJavaTypeAdapter( ToHashCodeAdapter.class )
    private List<IMigrationAction> dependencies;
    
    @XmlElement(name = "desc")
    private String description;

    
    @XmlElementWrapper(name="warnings")
    @XmlElement(name="warning")
    private List<String> warnings;


    public ActionBean() { }
    public ActionBean( IMigrationAction action ) {
        this.hashCode = Integer.toHexString( action.hashCode() );
        this.setDescription( action.toDescription() );
        this.setOriginMessage( action.getOriginMessage() );
        this.setFromMigrator( action.getFromMigrator() );
        this.setDependencies( action.getDependencies() );
        this.setWarnings( action.getWarnings() );
    }


    public String getOriginMessage() { return originMessage; }
    public void setOriginMessage( String originMessage ) { this.originMessage = originMessage; }
    public Class<? extends IMigrator> getFromMigrator() { return fromMigrator; }
    public void setFromMigrator( Class<? extends IMigrator> fromMigrator ) { this.fromMigrator = fromMigrator; }
    public List<IMigrationAction> getDependencies() { return dependencies; }
    public void setDependencies( List<IMigrationAction> dependencies ) { this.dependencies = dependencies; }
    public String getDescription() { return description; }
    public void setDescription( String description ) { this.description = description; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings( List<String> warnings ) { this.warnings = warnings; }
    
}// class
