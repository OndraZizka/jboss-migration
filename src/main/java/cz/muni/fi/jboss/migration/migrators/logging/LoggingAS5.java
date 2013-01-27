
package cz.muni.fi.jboss.migration.migrators.logging;


import cz.muni.fi.jboss.migration.spi.IConfigFragment;

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

@XmlRootElement(name = "configuration" , namespace = "http://jakarta.apache.org/log4j/")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "configuration")

public class LoggingAS5 implements IConfigFragment {

    @XmlElements(@XmlElement(name = "appender", type = Appender.class))
    Set<Appender> appenders;

    @XmlElements(@XmlElement(name = "category", type = Category.class))
    private Set<Category> categories;

    @XmlElement(name = "root")
    private RootLoggerAS5 rootLoggerAS5;

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

    public RootLoggerAS5 getRootLoggerAS5() {
        return rootLoggerAS5;
    }

    public void setRootLoggerAS5(RootLoggerAS5 rootLoggerAS5) {
        this.rootLoggerAS5 = rootLoggerAS5;
    }
}
