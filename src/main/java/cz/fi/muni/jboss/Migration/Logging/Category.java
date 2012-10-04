package cz.fi.muni.jboss.Migration.Logging;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 10/2/12
 * Time: 8:10 PM
 */
@XmlRootElement(name = "category")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "category")
public class Category {
    @XmlAttribute(name = "name")
    private String categoryName;
    @XmlPath("priority/@value")
    private String categoryValue;
    @XmlPath("priority/@appender-ref")
    private Collection<String> appenderRef;


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

    public Collection<String> getAppenderRef() {
        return appenderRef;
    }

    public void setAppenderRef(Collection<String> appenderRef) {
        this.appenderRef = appenderRef;
    }
}
