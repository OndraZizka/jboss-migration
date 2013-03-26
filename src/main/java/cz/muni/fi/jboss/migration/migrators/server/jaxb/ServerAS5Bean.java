package cz.muni.fi.jboss.migration.migrators.server.jaxb;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing server (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "Server")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Server")

public class ServerAS5Bean {

    @XmlElements(@XmlElement(name = "Service", type = ServiceBean.class))
    private Set<ServiceBean> services;

    public Set<ServiceBean> getServices() {
        return services;
    }

    public void setServices(Collection<ServiceBean> services) {
        Set<ServiceBean> temp = new HashSet();
        temp.addAll(services);
        this.services = temp;
    }
}
