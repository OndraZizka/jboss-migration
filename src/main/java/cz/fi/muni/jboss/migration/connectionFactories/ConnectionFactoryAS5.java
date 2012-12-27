package cz.fi.muni.jboss.migration.connectionFactories;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * 
 * @author  Roman Jakubco
 * Date: 8/28/12
 * Time: 3:26 PM
 */
@XmlRootElement(name = "tx-connection-factory")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "tx-connection-factory")
public class ConnectionFactoryAS5 {
    @XmlElement(name = "jndi-name")
    private String jndiName;
    @XmlElement(name = "no-tx-separate-pools")
    private  String noTxSeparatePools;
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
    private String securityDomainAndApp;
    @XmlElement(name = "min-pool-size")
    private String minPoolSize;
    @XmlElement(name = "max-pool-size")
    private String maxPoolSize;
    @XmlElement(name = "background-validation")
    private String backgroundValidation;
    @XmlElement(name = "background-validation-millis")
    private String backgroundValidationMillis;
    @XmlElement(name = "idle-timeout-minutes")
    private String idleTimeoutMinutes;
    @XmlElement(name = "allocation-retry")
    private String allocationRetry;
    @XmlElement(name = "allocation-retry-wait-millis")
    private String allocationRetryWaitMillis;
    @XmlElement(name = "blocking-timeout-millis")
    private String blockingTimeoutMillis;

    //Problem with this element in jboss ds schema.
    @XmlElement(name = "use-fast-fail")
    private String useFastFail;

    //special class for storing connection-property
    @XmlElements(@XmlElement(name ="config-property", type=ConfigProperty.class))
    private Collection<ConfigProperty> configProperties;



    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
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

    public String getSecurityDomainAndApp() {
        return securityDomainAndApp;
    }

    public void setSecurityDomainAndApp(String securityDomainAndApp) {
        this.securityDomainAndApp = securityDomainAndApp;
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

    public String getBackgroundValidation() {
        return backgroundValidation;
    }

    public void setBackgroundValidation(String backgroundValidation) {
        this.backgroundValidation = backgroundValidation;
    }

    public String getBackgroundValidationMillis() {
        return backgroundValidationMillis;
    }

    public void setBackgroundValidationMillis(String backgroundValidationMillis) {
        this.backgroundValidationMillis = backgroundValidationMillis;
    }

    public String getIdleTimeoutMinutes() {
        return idleTimeoutMinutes;
    }

    public void setIdleTimeoutMinutes(String idleTimeoutMinutes) {
        this.idleTimeoutMinutes = idleTimeoutMinutes;
    }

    public String getAllocationRetry() {
        return allocationRetry;
    }

    public void setAllocationRetry(String allocationRetry) {
        this.allocationRetry = allocationRetry;
    }

    public String getAllocationRetryWaitMillis() {
        return allocationRetryWaitMillis;
    }

    public void setAllocationRetryWaitMillis(String allocationRetryWaitMillis) {
        this.allocationRetryWaitMillis = allocationRetryWaitMillis;
    }

    public String getUseFastFail() {
        return useFastFail;
    }

    public void setUseFastFail(String useFastFail) {
        this.useFastFail = useFastFail;
    }

    public Collection<ConfigProperty> getConfigProperties() {
        return configProperties;
    }

    public void setConfigProperties(Collection<ConfigProperty> configProperties) {
        this.configProperties = configProperties;
    }

    public String getBlockingTimeoutMillis() {
        return blockingTimeoutMillis;
    }

    public void setBlockingTimeoutMillis(String blockingTimeoutMillis) {
        this.blockingTimeoutMillis = blockingTimeoutMillis;
    }
}
