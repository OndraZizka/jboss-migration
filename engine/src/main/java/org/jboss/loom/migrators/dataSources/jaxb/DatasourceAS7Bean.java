/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.dataSources.jaxb;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.*;
import org.eclipse.persistence.oxm.annotations.XmlPath;


/**
 * JAXB bean for datasource in AS7 (AS7)
 *
 * @author Roman Jakubco
 */
@XmlRootElement(name = "datasource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "datasource")
public class DatasourceAS7Bean extends AbstractDatasourceAS7Bean{

    // Basic elements in datasource element
    @XmlPath("@jta")
    private String jta;

    @XmlElement(name = "connection-url")
    private String connectionUrl;

    @XmlElements(@XmlElement(name = "connection-property", type = ConnectionPropertyBean.class))
    private Set<ConnectionPropertyBean> connectionProperties;

    
    
    public String getConnectionUrl() { return connectionUrl; }
    public void setConnectionUrl(String connectionUrl) { this.connectionUrl = connectionUrl; }

    public Set<ConnectionPropertyBean> getConnectionProperties() { return connectionProperties; }
    public void setConnectionProperties(Collection<ConnectionPropertyBean> connectionProperties) {
        Set<ConnectionPropertyBean> temp = new HashSet();
        temp.addAll(connectionProperties);
        this.connectionProperties = temp;
    }

    public String getJta() { return jta; }
    public void setJta(String jta) { this.jta = jta; }

}// class
