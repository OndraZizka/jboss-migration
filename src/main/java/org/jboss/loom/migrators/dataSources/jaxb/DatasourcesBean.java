/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.dataSources.jaxb;


import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * JAXB bean for datasources (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "datasources")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "datasources")

public class DatasourcesBean {

    @XmlElements(@XmlElement(name = "no-tx-datasource", type = NoTxDatasourceAS5Bean.class))
    private Set<NoTxDatasourceAS5Bean> noTxDatasourceAS5s;

    @XmlElements(@XmlElement(name = "local-tx-datasource", type = DatasourceAS5Bean.class))
    private Set<DatasourceAS5Bean> localDatasourceAS5s;

    @XmlElements(@XmlElement(name = "xa-datasource", type = XaDatasourceAS5Bean.class))
    private Set<XaDatasourceAS5Bean> xaDatasourceAS5s;

    public Set<XaDatasourceAS5Bean> getXaDatasourceAS5s() {
        return xaDatasourceAS5s;
    }

    public void setXaDatasourceAS5s(Collection<XaDatasourceAS5Bean> xaDatasourceAS5s) {
        Set<XaDatasourceAS5Bean> temp = new HashSet();
        temp.addAll(xaDatasourceAS5s);
        this.xaDatasourceAS5s = temp;
    }

    public Set<DatasourceAS5Bean> getLocalDatasourceAS5s() {
        return localDatasourceAS5s;
    }

    public void setLocalDatasourceAS5s(Collection<DatasourceAS5Bean> localDatasourceAS5s) {
        Set<DatasourceAS5Bean> temp = new HashSet();
        temp.addAll(localDatasourceAS5s);
        this.localDatasourceAS5s = temp;
    }

    public Set<NoTxDatasourceAS5Bean> getNoTxDatasourceAS5s() {
        return noTxDatasourceAS5s;
    }

    public void setNoTxDatasourceAS5s(Collection<NoTxDatasourceAS5Bean> noTxDatasourceAS5s) {
        Set<NoTxDatasourceAS5Bean> temp = new HashSet();
        temp.addAll(noTxDatasourceAS5s);
        this.noTxDatasourceAS5s = temp;
    }
}
