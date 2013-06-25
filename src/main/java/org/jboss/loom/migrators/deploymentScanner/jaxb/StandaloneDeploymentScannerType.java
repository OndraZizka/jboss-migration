/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.deploymentScanner.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * User: rsearls
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
     *  Returns "default" if set null.
     */
    public String getName() { return (name == null) ? "default" : name; }

    public void setName(String value) { this.name = value; }
    public String getPath() { return path; }
    public void setPath(String value) { this.path = value; }
    public String getRelativeTo() { return relativeTo; }
    public void setRelativeTo(String value) { this.relativeTo = value; }
    /**
     *  Returns true if set null.
     */
    public boolean isScanEnabled() { return (scanEnabled == null) ? true : scanEnabled; }
    public void setScanEnabled(Boolean value) { this.scanEnabled = value; }
    /**
     * Returns 0 if set null.
     */
    public int getScanInterval() { return (scanInterval == null) ? 0 : scanInterval; }
    public void setScanInterval(Integer value) { this.scanInterval = value; }
    /**
     *  Returns true if set null.
     */
    public boolean isAutoDeployZipped() { return (autoDeployZipped == null) ? true : autoDeployZipped; }
    public void setAutoDeployZipped(Boolean value) { this.autoDeployZipped = value; }
    public boolean isAutoDeployExploded() { return (autoDeployExploded == null) ? false : autoDeployExploded; }
    public void setAutoDeployExploded(Boolean value) { this.autoDeployExploded = value; }
    public boolean isAutoDeployXml() { return (autoDeployXml == null) ? true : autoDeployXml; }
    public void setAutoDeployXml(Boolean value) { this.autoDeployXml = value; }
    public int getDeploymentTimeout() { return (deploymentTimeout == null) ? 600 : deploymentTimeout; }
    public void setDeploymentTimeout(Integer value) { this.deploymentTimeout = value; }
}
