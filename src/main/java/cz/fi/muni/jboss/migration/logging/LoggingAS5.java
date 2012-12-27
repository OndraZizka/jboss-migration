
package cz.fi.muni.jboss.migration.logging;


import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * 
 * @author  Roman Jakubco
 * Date: 9/21/12
 * Time: 7:52 PM
 */


@XmlRootElement(name = "configuration" )
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name ="configuration")
public class LoggingAS5 {

    @XmlElements(@XmlElement(name = "appender", type = Appender.class))
    Collection<Appender> appenders;
    @XmlElements(@XmlElement(name = "category", type = Category.class))
    private Collection<Category> categories;
    @XmlPath("root/priority/@value")
    private String rootPriorityValue;
    @XmlPath("root/appender-ref/@ref")
    private Collection<String> rootAppenderRefs;

    public Collection<Appender> getAppenders() {
        return appenders;
    }

    public void setAppenders(Collection<Appender> appenders) {
        this.appenders = appenders;
    }

    public Collection<Category> getCategories() {
        return categories;
    }

    public void setCategories(Collection<Category> categories) {
        this.categories = categories;
    }

    public String getRootPriorityValue() {
        return rootPriorityValue;
    }

    public void setRootPriorityValue(String rootPriorityValue) {
        this.rootPriorityValue = rootPriorityValue;
    }

    public Collection<String> getRootAppenderRefs() {
        return rootAppenderRefs;
    }

    public void setRootAppenderRefs(Collection<String> rootAppenderRefs) {
        this.rootAppenderRefs = rootAppenderRefs;
    }









}
