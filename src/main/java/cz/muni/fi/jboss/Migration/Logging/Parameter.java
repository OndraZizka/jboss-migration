package cz.muni.fi.jboss.Migration.Logging;

import javax.xml.bind.annotation.*;

/**
 * @author: Roman Jakubco
 * Date: 10/2/12
 * Time: 8:08 PM
 */

@XmlRootElement(name = "parameter")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "parameter")

public class Parameter {

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
