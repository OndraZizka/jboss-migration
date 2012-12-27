package cz.fi.muni.jboss.migration.logging;

import javax.xml.bind.annotation.*;

/**
 * 
 * @author  Roman Jakubco
 * Date: 10/2/12
 * Time: 8:24 PM
*/
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "property")
public class Property{
    @XmlAttribute(name = "name")
    private String name;
    @XmlAttribute(name = "value")
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
