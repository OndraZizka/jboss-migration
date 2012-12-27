package cz.muni.fi.jboss.migration.logging;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collection;

/**
 * 
 * @author  Roman Jakubco
 * Date: 10/2/12
 * Time: 8:11 PM
 */
@XmlRootElement(name = "root-logger")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "root-logger")
public  class RootLogger{
    @XmlPath("level/@name")
    private String rootLoggerLevelName;
    @XmlPath("handlers/handler/@name")
    private Collection<String> handlersName;

    public String getRootLoggerLevelName() {
        return rootLoggerLevelName;
    }

    public void setRootLoggerLevelName(String rootLoggerLevelName) {
        this.rootLoggerLevelName = rootLoggerLevelName;
    }

    public Collection<String> getHandlersName() {
        return handlersName;
    }

    public void setHandlersName(Collection<String> handlersName) {
        this.handlersName = handlersName;
    }
}
