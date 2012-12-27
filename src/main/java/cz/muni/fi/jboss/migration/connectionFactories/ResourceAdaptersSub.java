package cz.muni.fi.jboss.migration.connectionFactories;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * 
 * @author  Roman Jakubco
 * Date: 8/28/12
 * Time: 3:27 PM
 */
@XmlRootElement(name = "resource-adapters")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "resource-adapters")
public class ResourceAdaptersSub {
    @XmlElements(@XmlElement(name = "resource-adapter"))
    private Collection<ResourceAdapter> resourceAdapters;

    public Collection<ResourceAdapter> getResourceAdapters() {
        return resourceAdapters;
    }

    public void setResourceAdapters(Collection<ResourceAdapter> resourceAdapters) {
        this.resourceAdapters = resourceAdapters;
    }
}
