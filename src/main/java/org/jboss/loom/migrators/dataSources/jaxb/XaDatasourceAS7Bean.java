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
 * JAXB bean for xa-datasource in AS7 (AS7)
 *
 * @author Roman Jakubco
 */
@XmlRootElement(name = "xa-datasource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "xa-datasource")
public class XaDatasourceAS7Bean extends AbstractDatasourceAS7Bean{

    // Basic elements in datasource element
    @XmlElement(name = "xa-datasource-class")
    private String xaDatasourceClass;

    @XmlElements(@XmlElement(name = "xa-datasource-property", type = XaDatasourcePropertyBean.class))
    private Set<XaDatasourcePropertyBean> xaDatasourceProps;


    // Elements in pool element
    @XmlPath("/xa-pool/prefill/text()")
    @Override public String getPrefill() { return super.getPrefill(); }

    @XmlPath("/xa-pool/is-same-rm-override/text()")
    private String isSameRmOverride;

    @XmlPath("/timeout/xa-resource-timeout/text()")
    private String xaResourceTimeout;

    // EmptyType in scheme
    @XmlPath("/xa-pool/interleaving/text()")
    private String interleaving;

    // EmptyType in scheme
    @XmlPath("/xa-pool/no-tx-separate-pools/text()")
    private String noTxSeparatePools;


    public Set<XaDatasourcePropertyBean> getXaDatasourceProps() {
        return xaDatasourceProps;
    }

    public void setXaDatasourceProps(Collection<XaDatasourcePropertyBean> xaDatasourceProps) {
        Set<XaDatasourcePropertyBean> temp = new HashSet();
        temp.addAll(xaDatasourceProps);
        this.xaDatasourceProps = temp;
    }

    @XmlPath("/xa-pool/min-pool-size/text()")
    @Override public String getMinPoolSize() { return super.getMinPoolSize(); }
    
    @XmlPath("/xa-pool/max-pool-size/text()")
    @Override public String getMaxPoolSize() { return super.getMaxPoolSize(); }
    
    public String getSameRmOverride() { return isSameRmOverride; }
    public void setSameRmOverride(String sameRmOverride) { isSameRmOverride = sameRmOverride; }
    public String getInterleaving() { return interleaving; }
    public void setInterleaving(String interleaving) { this.interleaving = interleaving; }
    public String getNoTxSeparatePools() { return noTxSeparatePools; }
    public void setNoTxSeparatePools(String noTxSeparatePools) { this.noTxSeparatePools = noTxSeparatePools; }
    public String getXaResourceTimeout() { return xaResourceTimeout; }
    public void setXaResourceTimeout(String xaResourceTimeout) { this.xaResourceTimeout = xaResourceTimeout; }
    
    
}// class
