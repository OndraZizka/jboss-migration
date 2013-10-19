/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.dataSources.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * JAXB bean for no-tx-datasource (AS5)
 *
 * @author Roman Jakubco
 * 
 *  TODO: Many of properties are identical across 3 types of datasources.
 *        Move them into a parent class.
 */
@XmlRootElement(name = "no-tx-datasource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "no-tx-datasource")
public class NoTxDatasourceAS5Bean extends AbstractDatasourceAS5Bean {

}