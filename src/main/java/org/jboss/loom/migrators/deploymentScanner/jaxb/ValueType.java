/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.deploymentScanner.jaxb;

import org.jboss.loom.spi.IConfigFragment;

import javax.xml.bind.annotation.*;

/**
 * Date: 4/17/13
 */

@XmlRootElement(name = "value")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "valueType", propOrder = {})
public class ValueType implements IConfigFragment {

    @XmlValue
    protected String value;

    @XmlTransient
    private String URL_PREFIX = "file://";

    @XmlTransient
    private int scanPeriod;


    /**
     * Gets the value of the value property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setValue(String value) {
        this.value = value;
    }


    /* ---------------- non schema related utility functions ------*/
    /**
     * https://community.jboss.org/wiki/HowToDeployMyApplicationInAnExternalDirectoryInJBoss-5
     * states, "all specified folders need to be fully qualified url.".  The example is
     *       <value>file:///home/jpai/test/deploy</value>
     *
     * Report such a URL was found.
     *
     * @return
     */
    public boolean isExternalDir(){
        String v = (value == null)? "" : value.trim();
        return v.startsWith(URL_PREFIX);
    }

    /**
     * AS7 uses dir path names not URLs.  Preform the conversion.
     *
     * ? How are Windows paths specified?
     *
     * @return
     */
    public String getDeployPath() {
        String v = (value == null) ? "" : value.trim();
        String path = v;
        if (v.startsWith(URL_PREFIX)) {
            path = v.substring(URL_PREFIX.length());
        }
        return path;
    }



    public int getScanPeriod(){
        return this.scanPeriod;
    }

    public void setScanPeriod(int scanPeriod){
        this.scanPeriod = scanPeriod;
    }
}

