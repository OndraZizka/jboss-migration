package cz.fi.muni.jboss.migration.logging;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 10/2/12
 * Time: 8:11 PM
 */
@XmlRootElement(name = "logger")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "logger")
public class Logger{
    @XmlAttribute(name = "category")
    private String loggerCategory;
    @XmlPath("level/@name")
    private String loggerLevelName;
    @XmlAttribute(name = "use-parent-handlers")
    private String useParentHandlers;
    @XmlPath("handlers/handler/@name")
    private Collection<String> handlers;

    public String getLoggerCategory() {
        return loggerCategory;
    }

    public void setLoggerCategory(String loggerCategory) {
        this.loggerCategory = loggerCategory;
    }

    public String getLoggerLevelName() {
        return loggerLevelName;
    }

    public void setLoggerLevelName(String loggerLevelName) {
        this.loggerLevelName = loggerLevelName;
    }

    public String getUseParentHandlers() {
        return useParentHandlers;
    }

    public void setUseParentHandlers(String useParentHandlers) {
        this.useParentHandlers = useParentHandlers;
    }

    public Collection<String> getHandlers() {
        return handlers;
    }

    public void setHandlers(Collection<String> handlers) {
        this.handlers = handlers;
    }
}
