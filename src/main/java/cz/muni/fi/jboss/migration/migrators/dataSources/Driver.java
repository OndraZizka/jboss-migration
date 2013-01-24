package cz.muni.fi.jboss.migration.migrators.dataSources;

import cz.muni.fi.jboss.migration.spi.IMigratedData;

import javax.xml.bind.annotation.*;

/**
 * Class for marshalling and representing driver (AS7)
 *
 * @author Roman Jakubco
 * Date: 10/2/12
 * Time: 10:28 PM
 */

@XmlRootElement(name = "driver")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "driver")

public class Driver implements IMigratedData {

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
}
