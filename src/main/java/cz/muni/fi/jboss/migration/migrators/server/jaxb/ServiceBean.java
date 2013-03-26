package cz.muni.fi.jboss.migration.migrators.server.jaxb;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing service (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "Service")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Service")

public class ServiceBean {

    @XmlAttribute(name = "name")
    private String serviceName;

    @XmlElements(@XmlElement(name = "Connector", type = ConnectorAS5Bean.class))
    private Set<ConnectorAS5Bean> connectorAS5s;

    @XmlElement(name = "Engine")
    private EngineBean engine;


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Set<ConnectorAS5Bean> getConnectorAS5s() {
        return connectorAS5s;
    }

    public void setConnectorAS5s(Collection<ConnectorAS5Bean> connectorAS5s) {
        Set<ConnectorAS5Bean> temp = new HashSet();
        temp.addAll(connectorAS5s);
        this.connectorAS5s = temp;
    }

    public EngineBean getEngine() {
        return engine;
    }

    public void setEngine(EngineBean engine) {
        this.engine = engine;
    }
}