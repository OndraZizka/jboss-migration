package cz.muni.fi.jboss.Migration.DataSources;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: Roman Jakubco
 * Date: 8/27/12
 * Time: 6:25 PM
 */
@XmlRootElement(name = "datasources")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "datasources")

public class DatasourcesSub {

    @XmlElement(name = "datasource")
    private Set<DatasourceAS7> datasource;
    @XmlElement(name = "xa-datasource")
    private Set<XaDatasourceAS7> xaDatasource;
    @XmlElementWrapper(name = "drivers")
    @XmlElements(@XmlElement(name = "driver", type = Driver.class))
    private Set<Driver> drivers;
    @XmlElements(@XmlElement(name = "xa-datasource-class", type = Driver.class))
    private Set<Driver> xaDsClasses;

    public Set<DatasourceAS7> getDatasource() {
        return datasource;
    }

    public void setDatasource(Collection<DatasourceAS7> datasource) {
        Set<DatasourceAS7> temp = new HashSet();
        temp.addAll(datasource);
        this.datasource = temp;
    }

    public Set<XaDatasourceAS7> getXaDatasource() {
        return xaDatasource;
    }

    public void setXaDatasource(Collection<XaDatasourceAS7> xaDatasource) {
        Set<XaDatasourceAS7> temp = new HashSet();
        temp.addAll(xaDatasource);
        this.xaDatasource = temp;
    }

    public Set<Driver> getDrivers() {
        return drivers;
    }

    public void setDrivers(Collection<Driver> drivers) {
        Set<Driver> temp = new HashSet();
        temp.addAll(drivers);
        this.drivers = temp;
    }

    public Set<Driver> getXaDsClasses() {
        return xaDsClasses;
    }

    public void setXaDsClasses(Collection<Driver> xaDsClasses) {
        Set<Driver> temp = new HashSet();
        temp.addAll(xaDsClasses);
        this.xaDsClasses = temp;
    }
}
