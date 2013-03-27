package cz.muni.fi.jboss.migration.migrators.dataSources.jaxb;

import javax.xml.bind.annotation.*;

/**
 * Class for marshalling and representing driver (AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "driver")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "driver")

public class DriverBean {

    @XmlAttribute(name = "name")
    private String driverName;

    @XmlAttribute(name = "module")
    private String driverModule;

    @XmlAttribute(name = "major-version")
    private String majorVersion;

    @XmlAttribute(name = "minor-version")
    private String minorVersion;

    @XmlElement(name = "driver-class")
    private String driverClass;

    @XmlElement(name = "xa-datasource-class")
    private String xaDatasourceClass;

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverModule() {
        return driverModule;
    }

    public void setDriverModule(String driverModule) {
        this.driverModule = driverModule;
    }

    public String getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(String majorVersion) {
        this.majorVersion = majorVersion;
    }

    public String getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(String minorVersion) {
        this.minorVersion = minorVersion;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getXaDatasourceClass() {
        return xaDatasourceClass;
    }

    public void setXaDatasourceClass(String xaDatasourceClass) {
        this.xaDatasourceClass = xaDatasourceClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriverBean)) return false;

        DriverBean that = (DriverBean) o;

        if (driverClass != null ? !driverClass.equals(that.driverClass) : that.driverClass != null) return false;
        if (xaDatasourceClass != null ? !xaDatasourceClass.equals(that.xaDatasourceClass) : that.xaDatasourceClass != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = driverClass != null ? driverClass.hashCode() : 0;
        result = 31 * result + (xaDatasourceClass != null ? xaDatasourceClass.hashCode() : 0);
        return result;
    }
}
