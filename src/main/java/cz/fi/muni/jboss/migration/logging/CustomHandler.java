package cz.fi.muni.jboss.migration.logging;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * 
 * @author  Roman Jakubco
 * Date: 10/2/12
 * Time: 8:24 PM
 */
@XmlRootElement(name = "custom-handler")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "custom-handler")
public class CustomHandler{
    @XmlAttribute(name = "name")
    private String name;
    //TODO:encoding...
    @XmlPath("level/@name")
    private String level;
    @XmlPath("filter/@value")
    private String filter;
    @XmlPath("formatter/pattern-formatter/@pattern")
    private String formatter;
    @XmlAttribute(name ="class")
    private String classValue;
    @XmlAttribute(name = "module")
    private String module;
    @XmlElementWrapper(name = "properties")
    @XmlElements(@XmlElement(name = "property", type = Property.class))
    private Collection<Property> properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFormatter() {
        return formatter;
    }

    public void setFormatter(String formatter) {
        this.formatter = formatter;
    }

    public String getClassValue() {
        return classValue;
    }

    public void setClassValue(String classValue) {
        this.classValue = classValue;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Collection<Property> getProperties() {
        return properties;
    }

    public void setProperties(Collection<Property> properties) {
        this.properties = properties;
    }
}
