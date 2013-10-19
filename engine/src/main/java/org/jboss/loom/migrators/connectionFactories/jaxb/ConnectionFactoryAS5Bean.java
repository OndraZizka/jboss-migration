/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.connectionFactories.jaxb;

import javax.xml.bind.annotation.*;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;

/**
 * JAXB bean for tx-connection-factory (AS5)
 *
 * @author Roman Jakubco
 */
@ConfigPartDescriptor(
    name = "Connection factory ${jndiName}"
)
@XmlRootElement(name = "tx-connection-factory")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "tx-connection-factory")
public class ConnectionFactoryAS5Bean extends AbstractConnectionFactoryAS5Bean {

    @XmlElement(name = "local-transaction")
    private String localTransaction;

    @XmlElement(name = "xa-transaction")
    private String xaTransaction;

    @XmlElement(name = "no-tx-separate-pools")
    private String noTxSeparatePools;


    @XmlElement(name = "xa-resource-timeout")
    private String xaResourceTimeout;


    public String getXaTransaction() { return xaTransaction; }
    public void setXaTransaction(String xaTransaction) { this.xaTransaction = xaTransaction; }
    public String getLocalTransaction() { return localTransaction; }
    public void setLocalTransaction(String localTransaction) { this.localTransaction = localTransaction; }
    public String getNoTxSeparatePools() { return noTxSeparatePools; }
    public void setNoTxSeparatePools(String noTxSeparatePools) { this.noTxSeparatePools = noTxSeparatePools; }
    public String getXaResourceTimeout() { return xaResourceTimeout; }
    public void setXaResourceTimeout(String xaResourceTimeout) { this.xaResourceTimeout = xaResourceTimeout; }

}// class
