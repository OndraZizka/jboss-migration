package cz.fi.muni.jboss.Migration.ConnectionFactories;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 8/28/12
 * Time: 3:27 PM
 */
@XmlRootElement(name = "resource-adapters")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "resource-adapters")
public class ConnectionFactoriesSub {
    @XmlElementWrapper(name = "resource-adapters")
    @XmlElements(@XmlElement(name = "resource-adapter"))
    private Collection<ConnectionFactoryAS7> resourceAdapters;

    public Collection<ConnectionFactoryAS7> getResourceAdapters() {
        return resourceAdapters;
    }

    public void setResourceAdapters(Collection<ConnectionFactoryAS7> resourceAdapters) {
        this.resourceAdapters = resourceAdapters;
    }
}
