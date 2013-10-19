/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.connectionFactories.jaxb;

import javax.xml.bind.annotation.*;

/**
 * JAXB bean for of config-property (AS5, AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "config-property")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "config-property")

public class ConfigPropertyBean {

    @XmlValue
    private String configProperty;
    @XmlAttribute(name = "name")

    private String configPropertyName;
    @XmlAttribute(name = "type")

    private String type;

    public String getConfigProperty() {
        return configProperty;
    }

    public void setConfigProperty(String configProperty) {
        this.configProperty = configProperty;
    }

    public String getConfigPropertyName() {
        return configPropertyName;
    }

    public void setConfigPropertyName(String configPropertyName) {
        this.configPropertyName = configPropertyName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
