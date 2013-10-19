package org.jboss.loom.spi;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Marker interface for classes of information loaded from source server config.
 *
 * @author Roman Jakubco
 */
@XmlRootElement
public interface IConfigFragment {
    
    @XmlAttribute(name = "desc")
    @Override
    public String toString();
    
}
