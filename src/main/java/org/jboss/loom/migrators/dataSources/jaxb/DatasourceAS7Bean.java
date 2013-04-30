package org.jboss.loom.migrators.dataSources.jaxb;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Class for marshalling and representing datasource in AS7 (AS7)
 *
 * @author Roman Jakubco
 */
@XmlRootElement(name = "datasource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "datasource")

public class DatasourceAS7Bean extends AbstractDatasourceAS7Bean{

    // Basic elements in datasource element
    @XmlPath("@jta")
    private String jta;

    @XmlElement(name = "connection-url")
    private String connectionUrl;

    @XmlElements(@XmlElement(name = "connection-property", type = ConnectionPropertyBean.class))
    private Set<ConnectionPropertyBean> connectionProperties;

    // Elements in pool element
    @XmlPath("/pool/prefill/text()")
    private String prefill;

    @XmlPath("/pool/min-pool-size/text()")
    private String minPoolSize;

    @XmlPath("/pool/max-pool-size/text()")
    private String maxPoolSize;

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }


    public Set<ConnectionPropertyBean> getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(Collection<ConnectionPropertyBean> connectionProperties) {
        Set<ConnectionPropertyBean> temp = new HashSet();
        temp.addAll(connectionProperties);
        this.connectionProperties = temp;
    }

    public String getJta() {
        return jta;
    }

    public void setJta(String jta) {
        this.jta = jta;
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


}
