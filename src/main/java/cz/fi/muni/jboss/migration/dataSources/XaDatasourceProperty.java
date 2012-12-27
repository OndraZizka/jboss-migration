package cz.fi.muni.jboss.migration.dataSources;

import javax.xml.bind.annotation.*;

/**
 * 
 * @author  Roman Jakubco
 * Date: 10/2/12
 * Time: 8:59 PM
 */
@XmlRootElement(name = "xa-datasource-property")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "xa-datasource-property")
public class  XaDatasourceProperty {
    @XmlValue
    private String  xaDatasourceProperty;
    @XmlAttribute(name = "name")
    private String xaDatasourcePropertyName;

    public String getXaDatasourceProperty() {
        return xaDatasourceProperty;
    }

    public void setXaDatasourceProperty(String xaDatasourceProperty) {
        this.xaDatasourceProperty = xaDatasourceProperty;
    }

    public String getXaDatasourcePropertyName() {
        return xaDatasourcePropertyName;
    }

    public void setXaDatasourcePropertyName(String xaDatasourcePropertyName) {
        this.xaDatasourcePropertyName = xaDatasourcePropertyName;
    }

    @Override
    public String toString() {
        return "XaDatasourceProperty{" +
                "xaDatasourceProperty='" + xaDatasourceProperty + '\'' +
                ", xaDatasourcePropertyName='" + xaDatasourcePropertyName + '\'' +
                '}';
    }
}
