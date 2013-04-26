package org.jboss.loom.migrators.logging.jaxb;


import org.jboss.loom.spi.IConfigFragment;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing logging configuration (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "configuration", namespace = "http://jakarta.apache.org/log4j/")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "configuration")

public class LoggingAS5Bean implements IConfigFragment {

    @XmlElements(@XmlElement(name = "appender", type = AppenderBean.class))
    Set<AppenderBean> appenders;

    @XmlElements(@XmlElement(name = "category", type = CategoryBean.class))
    private Set<CategoryBean> categories;

    @XmlElements(@XmlElement(name = "logger", type = CategoryBean.class))
    private Set<CategoryBean> loggers;

    @XmlElement(name = "root")
    private RootLoggerAS5Bean rootLoggerAS5;

    public Set<AppenderBean> getAppenders() {
        return appenders;
    }

    public void setAppenders(Collection<AppenderBean> appenders) {
        Set<AppenderBean> temp = new HashSet();
        temp.addAll(appenders);
        this.appenders = temp;
    }

    public Set<CategoryBean> getCategories() {
        return categories;
    }

    public void setCategories(Collection<CategoryBean> categories) {
        Set<CategoryBean> temp = new HashSet();
        temp.addAll(categories);
        this.categories = temp;
    }

    public RootLoggerAS5Bean getRootLoggerAS5() {
        return rootLoggerAS5;
    }

    public void setRootLoggerAS5(RootLoggerAS5Bean rootLoggerAS5) {
        this.rootLoggerAS5 = rootLoggerAS5;
    }

    public Set<CategoryBean> getLoggers() {
        return loggers;
    }

    public void setLoggers(Set<CategoryBean> loggers) {
        this.loggers = loggers;
    }
}
