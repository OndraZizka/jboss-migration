/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.connectionFactories.jaxb;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing connection-factories (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "connection-factories")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "connection-factories")

public class ConnectionFactoriesBean {

    @XmlElement(name = "tx-connection-factory")
    private Set<ConnectionFactoryAS5Bean> connectionFactories;

    @XmlElement(name = "no-tx-connection-factory")
    private Set<NoTxConnectionFactoryAS5Bean> noTxConnectionFactories;

    public Set<ConnectionFactoryAS5Bean> getConnectionFactories() {
        return connectionFactories;
    }

    public void setConnectionFactories(Collection<ConnectionFactoryAS5Bean> connectionFactories) {
        Set<ConnectionFactoryAS5Bean> temp = new HashSet();
        temp.addAll(connectionFactories);
        this.connectionFactories = temp;
    }

    public Set<NoTxConnectionFactoryAS5Bean> getNoTxConnectionFactories() {
        return noTxConnectionFactories;
    }

    public void setNoTxConnectionFactories(Collection<NoTxConnectionFactoryAS5Bean> noTxConnectionFactories) {
        Set<NoTxConnectionFactoryAS5Bean> temp = new HashSet();
        temp.addAll(noTxConnectionFactories);
        this.noTxConnectionFactories = temp;
    }
}
