
package cz.muni.fi.jboss.migration.logging;


import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing logging configuration (AS5)
 *
 * @author Roman Jakubco
 * Date: 9/21/12
 * Time: 7:52 PM
 */

@XmlRootElement(name = "configuration" )
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "configuration")

public class LoggingAS5 {

    @XmlElements(@XmlElement(name = "appender", type = Appender.class))
    Set<Appender> appenders;

    @XmlElements(@XmlElement(name = "category", type = Category.class))
    private Set<Category> categories;

    @XmlPath("root/priority/@value")
    private String rootPriorityValue;

    @XmlPath("root/appender-ref/@ref")
    private Set<String> rootAppenderRefs;

    public Set<Appender> getAppenders() {
        return appenders;
    }

    public void setAppenders(Collection<Appender> appenders) {
        Set<Appender> temp = new HashSet();
        temp.addAll(appenders);
        this.appenders = temp;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Collection<Category> categories) {
        Set<Category> temp = new HashSet();
        temp.addAll(categories);
        this.categories = temp;
    }

    public String getRootPriorityValue() {
        return rootPriorityValue;
    }

    public void setRootPriorityValue(String rootPriorityValue) {
        this.rootPriorityValue = rootPriorityValue;
    }

    public Set<String> getRootAppenderRefs() {
        return rootAppenderRefs;
    }

    public void setRootAppenderRefs(Collection<String> rootAppenderRefs) {
        Set<String> temp = new HashSet();
        temp.addAll(rootAppenderRefs);
        this.rootAppenderRefs = temp;
    }









}
