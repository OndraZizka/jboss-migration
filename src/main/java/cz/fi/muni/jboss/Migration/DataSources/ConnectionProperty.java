package cz.fi.muni.jboss.Migration.DataSources;

import javax.xml.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 10/2/12
 * Time: 8:59 PM
 */
@XmlRootElement(name = "connection-property")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "connection-property")
public class ConnectionProperty {
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

    @Override
    public String toString() {
        return "ConnectionProperty{" +
                "connectionProperty='" + connectionProperty + '\'' +
                ", connectionPropertyName='" + connectionPropertyName + '\'' +
                '}';
    }
}