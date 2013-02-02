package cz.muni.fi.jboss.migration.migrators.dataSources.jaxb;

import javax.xml.bind.annotation.*;

/**
 * Class for marshalling and representing connection-property (AS5, AS7)
 *
 * @author Roman Jakubco
 * Date: 10/2/12
 * Time: 8:59 PM
 */

@XmlRootElement(name = "connection-property")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "connection-property")

public class ConnectionPropertyBean {

    @XmlValue
    private String connectionProperty;

    @XmlAttribute(name = "name")
    private String connectionPropertyName;

    public String getConnectionProperty() {
        return connectionProperty;
    }

    public void setConnectionProperty(String connectionProperty) {
        this.connectionProperty = connectionProperty;
    }

    public String getConnectionPropertyName() {
        return connectionPropertyName;
    }

    public void setConnectionPropertyName(String connectionPropertyName) {
        this.connectionPropertyName = connectionPropertyName;
    }
}