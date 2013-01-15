package cz.muni.fi.jboss.Migration.Server;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @uathor: Roman Jakubco
 * Date: 8/30/12
 * Time: 4:54 PM
 */

@XmlRootElement(name = "Server")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Server")

public class ServerAS5 {

    @XmlElements(@XmlElement(name = "Service", type =Service.class))
    private Set<Service> services;

    public Set<Service> getServices() {
        return services;
    }

    public void setServices(Collection<Service> services) {
        Set<Service> temp = new HashSet();
        temp.addAll(services);
        this.services = temp;
    }
}
