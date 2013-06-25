/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.server.jaxb;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.*;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;

/**
 * JAXB bean for service (AS5)
 *
 * @author Roman Jakubco
 */
@ConfigPartDescriptor(
    name = "JBoss Web service ${serviceName}"
)
@XmlRootElement(name = "Service")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Service")
public class ServiceBean {

    @XmlAttribute(name = "name")
    private String serviceName;

    @XmlElements(@XmlElement(name = "Connector", type = ConnectorAS5Bean.class))
    private Set<ConnectorAS5Bean> connectorAS5s;

    @XmlElement(name = "Engine")
    private EngineBean engine;


    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public Set<ConnectorAS5Bean> getConnectorAS5s() { return connectorAS5s; }
    public void setConnectorAS5s(Collection<ConnectorAS5Bean> connectorAS5s) {
        Set<ConnectorAS5Bean> temp = new HashSet();
        temp.addAll(connectorAS5s);
        this.connectorAS5s = temp;
    }
    public EngineBean getEngine() { return engine; }
    public void setEngine(EngineBean engine) { this.engine = engine; }
    
}// class