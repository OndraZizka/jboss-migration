package cz.muni.fi.jboss.migration.migrators.connectionFactories;

import cz.muni.fi.jboss.migration.spi.IMigratedData;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for marshalling and representing resource-adapter (AS7)
 *
 * @author Roman Jakubco
 * Date: 8/28/12
 * Time: 3:27 PM
 */

@XmlRootElement(name = "resource-adapter")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "resource-adapter")

public class ResourceAdapter implements IMigratedData {

     private String jndiName;

     @XmlElement(name = "archive")
     private String archive;

    //Problem? No link with AS5? is it required?
     @XmlElement(name = "transaction-support")
     private String transactionSupport;

    @XmlElementWrapper(name = "connection-definitions")
    @XmlElement(name = "connection-definition", type = ConnectionDefinition.class)
    private Set<ConnectionDefinition> connectionDefinitions;

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

    public Set<ConnectionDefinition> getConnectionDefinitions() {
        return connectionDefinitions;
    }

    public void setConnectionDefinitions(Collection<ConnectionDefinition> connectionDefinitions) {
        Set<ConnectionDefinition> temp = new HashSet();
        temp.addAll(connectionDefinitions);
        this.connectionDefinitions = temp;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }
}
