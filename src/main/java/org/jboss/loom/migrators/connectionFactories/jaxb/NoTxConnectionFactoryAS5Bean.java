/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.connectionFactories.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * JAXB bean for no-tx-connection-factory (AS5)
 *
 * @author Roman Jakubco
 */
@XmlRootElement(name = "no-tx-connection-factory")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "no-tx-connection-factory")

public class NoTxConnectionFactoryAS5Bean extends AbstractConnectionFactoryAS5Bean {

}
