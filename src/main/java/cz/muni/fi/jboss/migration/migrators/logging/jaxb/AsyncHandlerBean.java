package cz.muni.fi.jboss.migration.migrators.logging.jaxb;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for marshalling and representing async-handler  (AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "async-handler")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "async-handler")

public class AsyncHandlerBean {

    @XmlAttribute(name = "name")
    private String name;

    @XmlPath("level/@name")
    private String level;

    @XmlPath("filter/@value")
    private String filter;

    @XmlPath("formatter/pattern-formatter/@pattern")
    private String formatter;

    @XmlPath("queue-length/@value")
    private String queueLength;

    @XmlPath("overflow-action/@value")
    private String overflowAction;

    @XmlPath("subhandlers/handler/@name")
    private Set<String> subhandlers;

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

    public String getQueueLength() {
        return queueLength;
    }

    public void setQueueLength(String queueLength) {
        this.queueLength = queueLength;
    }

    public String getOverflowAction() {
        return overflowAction;
    }

    public void setOverflowAction(String overflowAction) {
        this.overflowAction = overflowAction;
    }

    public Set<String> getSubhandlers() {
        return subhandlers;
    }

    public void setSubhandlers(Collection<String> subhandlers) {
        Set<String> temp = new HashSet();
        temp.addAll(subhandlers);
        this.subhandlers = temp;
    }
}
