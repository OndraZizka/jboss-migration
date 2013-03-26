package cz.muni.fi.jboss.migration.migrators.dataSources.jaxb;

import javax.xml.bind.annotation.*;

/**
 * Class for unmarshalling/marshalling and representing xa-datasource-property (AS5, AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "xa-datasource-property")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "xa-datasource-property")

public class XaDatasourcePropertyBean {

    @XmlValue
    private String xaDatasourceProp;

    @XmlAttribute(name = "name")
    private String xaDatasourcePropName;

    public String getXaDatasourceProp() {
        return xaDatasourceProp;
    }

    public void setXaDatasourceProp(String xaDatasourceProp) {
        this.xaDatasourceProp = xaDatasourceProp;
    }

    public String getXaDatasourcePropName() {
        return xaDatasourcePropName;
    }

    public void setXaDatasourcePropName(String xaDatasourcePropName) {
        this.xaDatasourcePropName = xaDatasourcePropName;
    }

}
