package cz.muni.fi.jboss.migration.migrators.logging;

import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Set;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 3:38 PM
 */

@XmlRootElement(name = "root" )
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "root")

public class RootLoggerAS5 implements IConfigFragment {

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
