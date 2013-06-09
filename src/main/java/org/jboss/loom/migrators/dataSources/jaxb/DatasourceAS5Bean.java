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
import org.jboss.loom.spi.ann.ConfigPartDescriptor;

/**
 * Class for unmarshalling and representing local-tx-datasource (AS5)
 *
 * @author Roman Jakubco
 */
@ConfigPartDescriptor(
    name = "Datasource ${jndiName}"
)
@XmlRootElement(name = "local-tx-datasource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "local-tx-datasource")
public class DatasourceAS5Bean extends AbstractDatasourceAS5Bean{


}// class
