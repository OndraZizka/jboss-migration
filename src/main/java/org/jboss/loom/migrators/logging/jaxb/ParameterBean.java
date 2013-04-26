package org.jboss.loom.migrators.logging.jaxb;

import javax.xml.bind.annotation.*;

/**
 * Class for unmarshalling and representing parameter (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "parameter")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "parameter")

public class ParameterBean {

    @XmlAttribute(name = "name")
    private String paramName;

    @XmlAttribute(name = "value")
    private String paramValue;

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }
}
