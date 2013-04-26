package org.jboss.loom.migrators.logging.jaxb;

import org.jboss.loom.spi.IConfigFragment;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing category (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "category")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "category")

public class CategoryBean implements IConfigFragment {

    @XmlAttribute(name = "name")
    private String categoryName;

    @XmlPath("priority/@value")
    private String categoryValue;

    @XmlPath("priority/@appender-ref")
    private Set<String> appenderRef;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryValue() {
        return categoryValue;
    }

    public void setCategoryValue(String categoryValue) {
        this.categoryValue = categoryValue;
    }

    public Set<String> getAppenderRef() {
        return appenderRef;
    }

    public void setAppenderRef(Collection<String> appenderRef) {
        Set<String> temp = new HashSet();
        temp.addAll(appenderRef);
        this.appenderRef = temp;
    }
}
