package cz.fi.muni.jboss.migration.connectionFactories;

import javax.xml.bind.annotation.*;

/**
 * 
 * @author  Roman Jakubco
 * Date: 10/2/12
 * Time: 8:52 PM
 */
@XmlRootElement(name = "config-property")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "config-property")
public class ConfigProperty {
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

    @Override
    public String toString() {
        return "ConfigProperty{" +
                "configProperty='" + configProperty + '\'' +
                ", configPropertyName='" + configPropertyName + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
