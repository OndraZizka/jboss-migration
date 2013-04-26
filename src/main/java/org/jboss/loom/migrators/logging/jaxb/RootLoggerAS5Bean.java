package org.jboss.loom.migrators.logging.jaxb;

import org.jboss.loom.spi.IConfigFragment;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Set;

/**
 * @author Roman Jakubco
 */

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "root")

public class RootLoggerAS5Bean implements IConfigFragment {

    @XmlPath("priority/@value")
    private String rootPriorityValue;

    @XmlPath("appender-ref/@ref")
    private Set<String> rootAppenderRefs;

    public String getRootPriorityValue() {
        return rootPriorityValue;
    }

    public void setRootPriorityValue(String rootPriorityValue) {
        this.rootPriorityValue = rootPriorityValue;
    }

    public Set<String> getRootAppenderRefs() {
        return rootAppenderRefs;
    }

    public void setRootAppenderRefs(Set<String> rootAppenderRefs) {
        this.rootAppenderRefs = rootAppenderRefs;
    }
}
