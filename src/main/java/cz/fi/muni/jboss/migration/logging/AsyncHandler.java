package cz.fi.muni.jboss.migration.logging;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * 
 * @author  Roman Jakubco
 * Date: 10/2/12
 * Time: 8:14 PM
 */
@XmlRootElement(name = "async-handler")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "async-handler")
public class  AsyncHandler{
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
    private Collection<String> subhandlers;

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

    public Collection<String> getSubhandlers() {
        return subhandlers;
    }

    public void setSubhandlers(Collection<String> subhandlers) {
        this.subhandlers = subhandlers;
    }
}
