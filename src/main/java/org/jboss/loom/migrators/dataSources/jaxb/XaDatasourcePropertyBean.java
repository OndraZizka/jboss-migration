/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.dataSources.jaxb;

import javax.xml.bind.annotation.*;

/**
 * Class for unmarshalling/marshalling and representing xa-datasource-property (AS5, AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "xa-datasource-property")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "xa-datasource-property")

public class XaDatasourcePropertyBean {

    @XmlValue
    private String xaDatasourceProp;

    @XmlAttribute(name = "name")
    private String xaDatasourcePropName;

    public String getXaDatasourceProp() {
        return xaDatasourceProp;
    }

    public void setXaDatasourceProp(String xaDatasourceProp) {
        this.xaDatasourceProp = xaDatasourceProp;
    }

    public String getXaDatasourcePropName() {
        return xaDatasourcePropName;
    }

    public void setXaDatasourcePropName(String xaDatasourcePropName) {
        this.xaDatasourcePropName = xaDatasourcePropName;
    }

}
