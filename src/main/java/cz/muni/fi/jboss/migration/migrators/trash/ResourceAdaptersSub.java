package cz.muni.fi.jboss.migration.migrators.trash;

import cz.muni.fi.jboss.migration.migrators.connectionFactories.ResourceAdapter;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for marshalling and representing resource-adapters subsystem (AS7)
 *
 * @author Roman Jakubco
 * Date: 8/28/12
 * Time: 3:27 PM
 */

@XmlRootElement(name = "resource-adapters")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "resource-adapters")

public class ResourceAdaptersSub {

    @XmlElements(@XmlElement(name = "resource-adapter"))
    private Set<ResourceAdapter> resourceAdapters;

    public Set<ResourceAdapter> getResourceAdapters() {
        return resourceAdapters;
    }

    public void setResourceAdapters(Collection<ResourceAdapter> resourceAdapters) {
        Set<ResourceAdapter> temp = new HashSet();
        temp.addAll(resourceAdapters);
        this.resourceAdapters = temp;
    }
}
