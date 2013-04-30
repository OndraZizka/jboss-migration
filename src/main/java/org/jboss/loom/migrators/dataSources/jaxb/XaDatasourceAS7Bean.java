package org.jboss.loom.migrators.dataSources.jaxb;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for marshalling and representing xa-datasource in AS7 (AS7)
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
    private String prefill;

    @XmlPath("/xa-pool/min-pool-size/text()")
    private String minPoolSize;

    @XmlPath("/xa-pool/max-pool-size/text()")
    private String maxPoolSize;

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

    public String getPrefill() {
        return prefill;
    }

    public void setPrefill(String prefill) {
        this.prefill = prefill;
    }

    public String getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(String minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public String getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(String maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public String getSameRmOverride() {
        return isSameRmOverride;
    }

    public void setSameRmOverride(String sameRmOverride) {
        isSameRmOverride = sameRmOverride;
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
}
