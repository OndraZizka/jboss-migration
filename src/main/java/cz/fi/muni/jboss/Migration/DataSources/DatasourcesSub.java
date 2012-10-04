package cz.fi.muni.jboss.Migration.DataSources;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 8/27/12
 * Time: 6:25 PM
 */
@XmlRootElement(name = "datasources")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "datasources")
public class DatasourcesSub {
    @XmlElement(name = "datasource")
    private Collection<DatasourceAS7> datasource;
    @XmlElement(name = "xa-datasource")
    private Collection<XaDatasourceAS7> xaDatasource;
    @XmlElementWrapper(name = "drivers")
    @XmlElements(@XmlElement(name = "driver", type = Driver.class))
    private Collection<Driver> drivers;
    @XmlElements(@XmlElement(name = "xa-datasource-class", type = Driver.class))
    private Collection<Driver> xaDatasourceClasses;

    public Collection<DatasourceAS7> getDatasource() {
        return datasource;
    }

    public void setDatasource(Collection<DatasourceAS7> datasource) {
        this.datasource = datasource;
    }

    public Collection<XaDatasourceAS7> getXaDatasource() {
        return xaDatasource;
    }

    public void setXaDatasource(Collection<XaDatasourceAS7> xaDatasource) {
        this.xaDatasource = xaDatasource;
    }

    public Collection<Driver> getDrivers() {
        return drivers;
    }

    public void setDrivers(Collection<Driver> drivers) {
        this.drivers = drivers;
    }

    public Collection<Driver> getXaDatasourceClasses() {
        return xaDatasourceClasses;
    }

    public void setXaDatasourceClasses(Collection<Driver> xaDatasourceClasses) {
        this.xaDatasourceClasses = xaDatasourceClasses;
    }
}
