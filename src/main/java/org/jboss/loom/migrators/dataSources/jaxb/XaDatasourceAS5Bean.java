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
 * Class for unmarshalling and representing xa-datasource in AS5 (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "xa-datasource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "xa-datasource")
public class XaDatasourceAS5Bean extends AbstractDatasourceAS5Bean {

    @XmlElement(name = "xa-datasource-class")
    private String xaDatasourceClass;

    @XmlElements(@XmlElement(name = "xa-datasource-property", type = XaDatasourcePropertyBean.class))
    private Set<XaDatasourcePropertyBean> xaDatasourceProps;

    @XmlElement(name = "isSameRM-override-value")
    private String isSameRM;

    @XmlElement(name = "interleaving")
    private String interleaving;

    @XmlElement(name = "no-tx-separate-pools")
    private String noTxSeparatePools;

    @XmlElement(name = "xa-resource-timeout")
    private String xaResourceTimeout;


    @XmlElement(name = "transaction-isolation")
    private String transIsolation;



    public String getXaDatasourceClass() {
        return xaDatasourceClass;
    }

    public void setXaDatasourceClass(String xaDatasourceClass) {
        this.xaDatasourceClass = xaDatasourceClass;
    }

    public Collection<XaDatasourcePropertyBean> getXaDatasourceProps() {
        return xaDatasourceProps;
    }

    public void setXaDatasourceProps(Collection<XaDatasourcePropertyBean> xaDatasourceProps) {
        Set<XaDatasourcePropertyBean> temp = new HashSet();
        temp.addAll(xaDatasourceProps);
        this.xaDatasourceProps = temp;
    }


    public String getSameRM() {
        return isSameRM;
    }

    public void setSameRM(String sameRM) {
        isSameRM = sameRM;
    }

    public String getInterleaving() {
        return interleaving;
    }

    public void setInterleaving(String interleaving) {
        this.interleaving = interleaving;
    }

    public String getNoTxSeparatePools() {
        return noTxSeparatePools;
    }

    public void setNoTxSeparatePools(String noTxSeparatePools) {
        this.noTxSeparatePools = noTxSeparatePools;
    }

    public String getXaResourceTimeout() {
        return xaResourceTimeout;
    }

    public void setXaResourceTimeout(String xaResourceTimeout) {
        this.xaResourceTimeout = xaResourceTimeout;
    }

    public String getTransIsolation() {
        return transIsolation;
    }

    public void setTransIsolation(String transIsolation) {
        this.transIsolation = transIsolation;
    }

}
