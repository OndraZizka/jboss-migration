package cz.muni.fi.jboss.Migration.Logging;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: Roman Jakubco
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
    private Set<String> handlersName;

    public String getRootLoggerLevelName() {
        return rootLoggerLevelName;
    }

    public void setRootLoggerLevelName(String rootLoggerLevelName) {
        this.rootLoggerLevelName = rootLoggerLevelName;
    }

    public Set<String> getHandlersName() {
        return handlersName;
    }

    public void setHandlersName(Collection<String> handlersName) {
        Set<String> temp = new HashSet();
        temp.addAll(handlersName);
        this.handlersName = temp;
    }
}
