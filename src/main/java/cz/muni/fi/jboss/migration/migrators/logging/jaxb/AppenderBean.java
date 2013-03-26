package cz.muni.fi.jboss.migration.migrators.logging.jaxb;

import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing appender (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "appender")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "appender")

public class AppenderBean implements IConfigFragment {

    @XmlAttribute(name = "name")
    private String appenderName;

    @XmlAttribute(name = "class")
    private String appenderClass;

    @XmlElements(@XmlElement(name = "param", type = ParameterBean.class))
    private Set<ParameterBean> parameters;

    @XmlPath("appender-ref/@ref")
    private Set<String> appenderRefs;

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

    public Set<ParameterBean> getParameters() {
        return parameters;
    }

    public void setParameters(Collection<ParameterBean> parameters) {
        Set<ParameterBean> temp = new HashSet();
        temp.addAll(parameters);
        this.parameters = temp;
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

    public Set<String> getAppenderRefs() {
        return appenderRefs;
    }

    public void setAppenderRefs(Collection<String> appenderRefs) {
        Set<String> temp = new HashSet();
        temp.addAll(appenderRefs);
        this.appenderRefs = temp;
    }
}