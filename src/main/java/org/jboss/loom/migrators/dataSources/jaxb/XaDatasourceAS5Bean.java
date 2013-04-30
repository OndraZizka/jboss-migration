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
