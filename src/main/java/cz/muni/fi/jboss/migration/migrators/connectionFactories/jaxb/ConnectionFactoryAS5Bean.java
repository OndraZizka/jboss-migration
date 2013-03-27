package cz.muni.fi.jboss.migration.migrators.connectionFactories.jaxb;

import cz.muni.fi.jboss.migration.spi.IConfigFragment;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing tx-connection-factory (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "tx-connection-factory")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "tx-connection-factory")

public class ConnectionFactoryAS5Bean implements IConfigFragment {

    @XmlElement(name = "jndi-name")
    private String jndiName;

    @XmlElement(name = "local-transaction")
    private String localTransaction;

    @XmlElement(name = "xa-transaction")
    private String xaTransaction;

    @XmlElement(name = "no-tx-separate-pools")
    private String noTxSeparatePools;

    @XmlElement(name = "prefill")
    private String prefill;

    @XmlElement(name = "xa-resource-timeout")
    private String xaResourceTimeout;

    @XmlElement(name = "rar-name")
    private String rarName;

    @XmlElement(name = "connection-definition")
    private String connectionDefinition;

    @XmlElement(name = "application-managed-security")
    private String applicationManagedSecurity;

    @XmlElement(name = "security-domain")
    private String securityDomain;

    @XmlElement(name = "security-domain-and-application")
    private String secDomainAndApp;

    @XmlElement(name = "min-pool-size")
    private String minPoolSize;

    @XmlElement(name = "max-pool-size")
    private String maxPoolSize;

    @XmlElement(name = "background-validation")
    private String backgroundValid;

    @XmlElement(name = "background-validation-millis")
    private String backgroundValiMillis;

    @XmlElement(name = "idle-timeout-minutes")
    private String idleTimeoutMin;

    @XmlElement(name = "allocation-retry")
    private String allocationRetry;

    @XmlElement(name = "allocation-retry-wait-millis")
    private String allocRetryWaitMillis;

    @XmlElement(name = "blocking-timeout-millis")
    private String blockingTimeoutMillis;

    // Problem with this element in jboss ds schema.
    @XmlElement(name = "use-fast-fail")
    private String useFastFail;

    // Special class for storing connection-property
    @XmlElements(@XmlElement(name = "config-property", type = ConfigPropertyBean.class))
    private Set<ConfigPropertyBean> configProperties;


    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public String getXaTransaction() {
        return xaTransaction;
    }

    public void setXaTransaction(String xaTransaction) {
        this.xaTransaction = xaTransaction;
    }

    public String getLocalTransaction() {
        return localTransaction;
    }

    public void setLocalTransaction(String localTransaction) {
        this.localTransaction = localTransaction;
    }

    public String getNoTxSeparatePools() {
        return noTxSeparatePools;
    }

    public void setNoTxSeparatePools(String noTxSeparatePools) {
        this.noTxSeparatePools = noTxSeparatePools;
    }

    public String getPrefill() {
        return prefill;
    }

    public void setPrefill(String prefill) {
        this.prefill = prefill;
    }

    public String getXaResourceTimeout() {
        return xaResourceTimeout;
    }

    public void setXaResourceTimeout(String xaResourceTimeout) {
        this.xaResourceTimeout = xaResourceTimeout;
    }

    public String getRarName() {
        return rarName;
    }

    public void setRarName(String rarName) {
        this.rarName = rarName;
    }

    public String getConnectionDefinition() {
        return connectionDefinition;
    }

    public void setConnectionDefinition(String connectionDefinition) {
        this.connectionDefinition = connectionDefinition;
    }

    public String getApplicationManagedSecurity() {
        return applicationManagedSecurity;
    }

    public void setApplicationManagedSecurity(String applicationManagedSecurity) {
        this.applicationManagedSecurity = applicationManagedSecurity;
    }

    public String getSecurityDomain() {
        return securityDomain;
    }

    public void setSecurityDomain(String securityDomain) {
        this.securityDomain = securityDomain;
    }

    public String getSecDomainAndApp() {
        return secDomainAndApp;
    }

    public void setSecDomainAndApp(String secDomainAndApp) {
        this.secDomainAndApp = secDomainAndApp;
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

    public String getBackgroundValid() {
        return backgroundValid;
    }

    public void setBackgroundValid(String backgroundValid) {
        this.backgroundValid = backgroundValid;
    }

    public String getBackgroundValiMillis() {
        return backgroundValiMillis;
    }

    public void setBackgroundValiMillis(String backgroundValiMillis) {
        this.backgroundValiMillis = backgroundValiMillis;
    }

    public String getIdleTimeoutMin() {
        return idleTimeoutMin;
    }

    public void setIdleTimeoutMin(String idleTimeoutMin) {
        this.idleTimeoutMin = idleTimeoutMin;
    }

    public String getAllocationRetry() {
        return allocationRetry;
    }

    public void setAllocationRetry(String allocationRetry) {
        this.allocationRetry = allocationRetry;
    }

    public String getAllocRetryWaitMillis() {
        return allocRetryWaitMillis;
    }

    public void setAllocRetryWaitMillis(String allocRetryWaitMillis) {
        this.allocRetryWaitMillis = allocRetryWaitMillis;
    }

    public String getUseFastFail() {
        return useFastFail;
    }

    public void setUseFastFail(String useFastFail) {
        this.useFastFail = useFastFail;
    }

    public Set<ConfigPropertyBean> getConfigProperties() {
        return configProperties;
    }

    public void setConfigProperties(Collection<ConfigPropertyBean> configProperties) {
        Set<ConfigPropertyBean> temp = new HashSet();
        temp.addAll(configProperties);
        this.configProperties = temp;
    }

    public String getBlockingTimeoutMillis() {
        return blockingTimeoutMillis;
    }

    public void setBlockingTimeoutMillis(String blockingTimeoutMillis) {
        this.blockingTimeoutMillis = blockingTimeoutMillis;
    }
}
