package cz.muni.fi.jboss.migration.migrators.server;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing service (AS5)
 *
 * @author Roman Jakubco
 * Date: 10/2/12
 * Time: 9:12 PM
 */

@XmlRootElement(name = "Service")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Service")

public class Service{

    @XmlAttribute(name = "name")
    private String serviceName;

    @XmlElements(@XmlElement(name = "Connector", type = ConnectorAS5.class))
    private Set<ConnectorAS5> connectorAS5s;

    @XmlElement(name = "Engine")
    private Engine engine;



    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Set<ConnectorAS5> getConnectorAS5s() {
        return connectorAS5s;
    }

    public void setConnectorAS5s(Collection<ConnectorAS5> connectorAS5s) {
        Set<ConnectorAS5> temp = new HashSet();
        temp.addAll(connectorAS5s);
        this.connectorAS5s = temp;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}