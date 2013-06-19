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
 * JAXB bean for connection-property (AS5, AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "connection-property")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "connection-property")

public class ConnectionPropertyBean {

    @XmlValue
    private String connectionProperty;

    @XmlAttribute(name = "name")
    private String connectionPropertyName;

    public String getConnectionProperty() {
        return connectionProperty;
    }

    public void setConnectionProperty(String connectionProperty) {
        this.connectionProperty = connectionProperty;
    }

    public String getConnectionPropertyName() {
        return connectionPropertyName;
    }

    public void setConnectionPropertyName(String connectionPropertyName) {
        this.connectionPropertyName = connectionPropertyName;
    }
}