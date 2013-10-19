/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.deploymentScanner.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.*;


/**
 * User: rsearls
 * Date: 4/17/13
 */
@XmlRootElement(name = "subsystem")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "subsystem", propOrder = {
    "deploymentScanner"
})
public class Subsystem {

    @XmlElement(name = "deployment-scanner")
    protected List<StandaloneDeploymentScannerType> deploymentScanner;

    @XmlAttribute(name = "xmlns")
    protected String xmlns;


    public String getXmlns() {
        if (xmlns == null) {
            return "urn:jboss:domain:deployment-scanner:1.1";
        } else {
            return xmlns;
        }
    }


    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }


    /**
     * Gets the value of the deploymentScanner property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the deploymentScanner property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeploymentScanner().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StandaloneDeploymentScannerType }
     *
     *
     */
    public List<StandaloneDeploymentScannerType> getDeploymentScanner() {
        if (deploymentScanner == null) {
            deploymentScanner = new ArrayList<StandaloneDeploymentScannerType>();
        }
        return this.deploymentScanner;
    }

}
