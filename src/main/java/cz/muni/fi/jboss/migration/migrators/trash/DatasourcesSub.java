package cz.muni.fi.jboss.migration.migrators.trash;

import cz.muni.fi.jboss.migration.migrators.dataSources.jaxb.DatasourceAS7Bean;
import cz.muni.fi.jboss.migration.migrators.dataSources.jaxb.DriverBean;
import cz.muni.fi.jboss.migration.migrators.dataSources.jaxb.XaDatasourceAS7Bean;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for marshalling and representing datasource subsystem (AS7)
 *
 * @author Roman Jakubco
 *         Date: 8/27/12
 *         Time: 6:25 PM
 */

@XmlRootElement(name = "datasources")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "datasources")

public class DatasourcesSub {

    @XmlElement(name = "datasource")
    private Set<DatasourceAS7Bean> datasource;

    @XmlElement(name = "xa-datasource")
    private Set<XaDatasourceAS7Bean> xaDatasource;

    @XmlElementWrapper(name = "drivers")
    @XmlElements(@XmlElement(name = "driver", type = DriverBean.class))
    private Set<DriverBean> drivers;

    @XmlElements(@XmlElement(name = "xa-datasource-class", type = DriverBean.class))
    private Set<DriverBean> xaDsClasses;

    public Set<DatasourceAS7Bean> getDatasource() {
        return datasource;
    }

    public void setDatasource(Collection<DatasourceAS7Bean> datasource) {
        Set<DatasourceAS7Bean> temp = new HashSet();
        temp.addAll(datasource);
        this.datasource = temp;
    }

    public Set<XaDatasourceAS7Bean> getXaDatasource() {
        return xaDatasource;
    }

    public void setXaDatasource(Collection<XaDatasourceAS7Bean> xaDatasource) {
        Set<XaDatasourceAS7Bean> temp = new HashSet();
        temp.addAll(xaDatasource);
        this.xaDatasource = temp;
    }

    public Set<DriverBean> getDrivers() {
        return drivers;
    }

    public void setDrivers(Collection<DriverBean> drivers) {
        Set<DriverBean> temp = new HashSet();
        temp.addAll(drivers);
        this.drivers = temp;
    }

    public Set<DriverBean> getXaDsClasses() {
        return xaDsClasses;
    }

    public void setXaDsClasses(Collection<DriverBean> xaDsClasses) {
        Set<DriverBean> temp = new HashSet();
        temp.addAll(xaDsClasses);
        this.xaDsClasses = temp;
    }
}
