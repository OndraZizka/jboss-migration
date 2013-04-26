package org.jboss.loom.migrators.deploymentScanner.jaxb;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


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
