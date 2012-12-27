package cz.fi.muni.jboss.migration.connectionFactories;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * 
 * @author  Roman Jakubco
 * Date: 8/28/12
 * Time: 3:27 PM
 */
@XmlRootElement(name = "resource-adapter")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "resource-adapter")
public class ResourceAdapter {
      private String jndiName;
     @XmlElement(name = "archive")
     private String archive;
    //Problem? No link with AS5? is it required?
     @XmlElement(name = "transaction-support")
     private String transactionSupport;

    @XmlElementWrapper(name = "connection-definitions")
    @XmlElement(name = "connection-definition" ,type=ConnectionDefinition.class)
    private Collection<ConnectionDefinition> connectionDefinitions;

    public String getArchive() {
        return archive;
    }

    public void setArchive(String archive) {
        this.archive = archive;
    }

    public String getTransactionSupport() {
        return transactionSupport;
    }

    public void setTransactionSupport(String transactionSupport) {
        this.transactionSupport = transactionSupport;
    }

    public Collection<ConnectionDefinition> getConnectionDefinitions() {
        return connectionDefinitions;
    }

    public void setConnectionDefinitions(Collection<ConnectionDefinition> connectionDefinitions) {
        this.connectionDefinitions = connectionDefinitions;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }
}
