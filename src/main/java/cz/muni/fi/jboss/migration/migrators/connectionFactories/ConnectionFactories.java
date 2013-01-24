package cz.muni.fi.jboss.migration.migrators.connectionFactories;

import cz.muni.fi.jboss.migration.spi.ILoadedData;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing connection-factories (AS5)
 *
 * @author Roman Jakubco
 * Date: 8/28/12
 * Time: 3:27 PM
 */

@XmlRootElement(name = "connection-factories")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "connection-factories")

public class ConnectionFactories {

    @XmlElement(name = "tx-connection-factory")
    private Set<ConnectionFactoryAS5> connectionFactories;

    // No idea what is this. But it can be a element in AS5
    // TODO: find more about this element. Can it be transform to As7?
    @XmlElement(name = "no-tx-connection-factory")
    private String pom;

    public Set<ConnectionFactoryAS5> getConnectionFactories() {
        return connectionFactories;
    }

    public void setConnectionFactories(Collection<ConnectionFactoryAS5> connectionFactories) {
        Set<ConnectionFactoryAS5> temp = new HashSet();
        temp.addAll(connectionFactories);
        this.connectionFactories = temp;
    }
}
