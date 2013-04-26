package org.jboss.loom.migrators.connectionFactories.jaxb;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for marshalling and representing resource-adapter (AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "resource-adapter")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "resource-adapter")

public class ResourceAdapterBean {

    private String jndiName;

    @XmlElement(name = "archive")
    private String archive;

    //Problem? No link with AS5? is it required?
    @XmlElement(name = "transaction-support")
    private String transactionSupport;

    @XmlElementWrapper(name = "connection-definitions")
    @XmlElement(name = "connection-definition", type = ConnectionDefinitionBean.class)
    private Set<ConnectionDefinitionBean> connectionDefinitions;

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

    public Set<ConnectionDefinitionBean> getConnectionDefinitions() {
        return connectionDefinitions;
    }

    public void setConnectionDefinitions(Collection<ConnectionDefinitionBean> connectionDefinitions) {
        Set<ConnectionDefinitionBean> temp = new HashSet();
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
