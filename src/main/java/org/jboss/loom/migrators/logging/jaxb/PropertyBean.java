package org.jboss.loom.migrators.logging.jaxb;

import javax.xml.bind.annotation.*;

/**
 * Class for marshalling and representing property (AS7)
 *
 * @author Roman Jakubco
 */
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "property")

public class PropertyBean {

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
