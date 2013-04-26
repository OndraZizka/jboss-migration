package org.jboss.loom.migrators.deploymentScanner.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * User: rsearls
 * Date: 4/17/13
 */
@XmlRootElement(name = "deployment-scanner")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "standalone-deployment-scanner-type")
public class StandaloneDeploymentScannerType {

    @XmlAttribute(name = "name")
    protected String name;

    @XmlAttribute(name = "path", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String path;

    @XmlAttribute(name = "relative-to")
    protected String relativeTo;

    @XmlAttribute(name = "scan-enabled")
    protected Boolean scanEnabled;

    @XmlAttribute(name = "scan-interval")
    protected Integer scanInterval;

    @XmlAttribute(name = "auto-deploy-zipped")
    protected Boolean autoDeployZipped;

    @XmlAttribute(name = "auto-deploy-exploded")
    protected Boolean autoDeployExploded;

    @XmlAttribute(name = "auto-deploy-xml")
    protected Boolean autoDeployXml;

    @XmlAttribute(name = "deployment-timeout")
    protected Integer deploymentTimeout;



    public StandaloneDeploymentScannerType(){
    }

    public StandaloneDeploymentScannerType(ValueType v){
        setPath(v.getDeployPath());
        setScanInterval(v.getScanPeriod());
    }


    /**
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName() {
        if (name == null) {
            return "default";
        } else {
            return name;
        }
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the path property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPath(String value) {
        this.path = value;
    }

    /**
     * Gets the value of the relativeTo property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getRelativeTo() {
        return relativeTo;
    }

    /**
     * Sets the value of the relativeTo property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setRelativeTo(String value) {
        this.relativeTo = value;
    }

    /**
     * Gets the value of the scanEnabled property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public boolean isScanEnabled() {
        if (scanEnabled == null) {
            return true;
        } else {
            return scanEnabled;
        }
    }

    /**
     * Sets the value of the scanEnabled property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setScanEnabled(Boolean value) {
        this.scanEnabled = value;
    }

    /**
     * Gets the value of the scanInterval property.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public int getScanInterval() {
        if (scanInterval == null) {
            return  0;
        } else {
            return scanInterval;
        }
    }

    /**
     * Sets the value of the scanInterval property.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setScanInterval(Integer value) {
        this.scanInterval = value;
    }

    /**
     * Gets the value of the autoDeployZipped property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public boolean isAutoDeployZipped() {
        if (autoDeployZipped == null) {
            return true;
        } else {
            return autoDeployZipped;
        }
    }

    /**
     * Sets the value of the autoDeployZipped property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAutoDeployZipped(Boolean value) {
        this.autoDeployZipped = value;
    }

    /**
     * Gets the value of the autoDeployExploded property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public boolean isAutoDeployExploded() {
        if (autoDeployExploded == null) {
            return false;
        } else {
            return autoDeployExploded;
        }
    }

    /**
     * Sets the value of the autoDeployExploded property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAutoDeployExploded(Boolean value) {
        this.autoDeployExploded = value;
    }

    /**
     * Gets the value of the autoDeployXml property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public boolean isAutoDeployXml() {
        if (autoDeployXml == null) {
            return true;
        } else {
            return autoDeployXml;
        }
    }

    /**
     * Sets the value of the autoDeployXml property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAutoDeployXml(Boolean value) {
        this.autoDeployXml = value;
    }

    /**
     * Gets the value of the deploymentTimeout property.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public int getDeploymentTimeout() {
        if (deploymentTimeout == null) {
            return  600;
        } else {
            return deploymentTimeout;
        }
    }

    /**
     * Sets the value of the deploymentTimeout property.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setDeploymentTimeout(Integer value) {
        this.deploymentTimeout = value;
    }

}

