package org.jboss.loom.migrators.connectionFactories.jaxb;

import javax.xml.bind.annotation.*;

/**
 * Class for unmarshalling and representing of config-property (AS5, AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "config-property")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "config-property")

public class ConfigPropertyBean {

    @XmlValue
    private String configProperty;
    @XmlAttribute(name = "name")

    private String configPropertyName;
    @XmlAttribute(name = "type")

    private String type;

    public String getConfigProperty() {
        return configProperty;
    }

    public void setConfigProperty(String configProperty) {
        this.configProperty = configProperty;
    }

    public String getConfigPropertyName() {
        return configPropertyName;
    }

    public void setConfigPropertyName(String configPropertyName) {
        this.configPropertyName = configPropertyName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
