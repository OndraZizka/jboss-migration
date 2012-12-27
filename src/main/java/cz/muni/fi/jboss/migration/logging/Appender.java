package cz.muni.fi.jboss.migration.logging;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * 
 * @author  Roman Jakubco
 * Date: 10/2/12
 * Time: 7:53 PM
 */
@XmlRootElement(name = "appender")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "appender")
public class Appender{
    @XmlAttribute(name = "name")
    private String appenderName;
    @XmlAttribute(name = "class")
    private String appenderClass;
    @XmlElements(@XmlElement(name = "param",type = Parameter.class))
    private Collection<Parameter> parameters;
    @XmlPath("appender-ref/@ref")
    private Collection<String>  appenderRefs;
    @XmlPath("layout/param/@name")
    private String layoutParamName;
    @XmlPath("layout/param/@value")
    private String layoutParamValue;

    public String getAppenderName() {
        return appenderName;
    }

    public void setAppenderName(String appenderName) {
        this.appenderName = appenderName;
    }

    public String getAppenderClass() {
        return appenderClass;
    }

    public void setAppenderClass(String appenderClass) {
        this.appenderClass = appenderClass;
    }

    public Collection<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(Collection<Parameter> parameters) {
        this.parameters = parameters;
    }

    public String getLayoutParamName() {
        return layoutParamName;
    }

    public void setLayoutParamName(String layoutParamName) {
        this.layoutParamName = layoutParamName;
    }

    public String getLayoutParamValue() {
        return layoutParamValue;
    }

    public void setLayoutParamValue(String layoutParamValue) {
        this.layoutParamValue = layoutParamValue;
    }

    public Collection<String> getAppenderRefs() {
        return appenderRefs;
    }

    public void setAppenderRefs(Collection<String> appenderRefs) {
        this.appenderRefs = appenderRefs;
    }
}