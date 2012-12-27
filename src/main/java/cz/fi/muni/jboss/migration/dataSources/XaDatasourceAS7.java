package cz.fi.muni.jboss.migration.dataSources;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * 
 * @author  Roman Jakubco
 * Date: 8/27/12
 * Time: 6:25 PM
 */
@XmlRootElement(name = "xa-datasource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "xa-datasource")
public class XaDatasourceAS7 {
    //basic elements in datasource element
    @XmlPath("@jndi-name")
    private String jndiName;
    @XmlPath("@pool-name")
    private String poolName;
    @XmlPath("@use-java-context")
    private String useJavaContext;
    @XmlPath("@enabled")
    private String enabled;
    @XmlElement(name = "url-delimeter")
    private String urlDelimeter;
    @XmlElement(name = "driver")
    private String driver;
    @XmlElement(name = "url-selector-strategy-class-name")
    private String urlSelector;
    @XmlElement(name = "transaction-isolation")
    private String transactionIsolation;
    @XmlElement(name = "new-connection-sql")
    private String newConnectionSql;

    @XmlElement(name = "xa-datasource-class")
    private String xaDatasourceClass;

    @XmlElements(@XmlElement(name = "xa-datasource-property", type= XaDatasourceProperty.class))
    private Collection<XaDatasourceProperty> xaDatasourceProperties;

    //elements in pool element
    @XmlPath("/xa-pool/prefill/text()")
    private String prefill;
    @XmlPath("/xa-pool/min-pool-size/text()")
    private String minPoolSize;
    @XmlPath("/xa-pool/max-pool-size/text()")
    private String maxPoolSize;
    @XmlPath("/xa-pool/is-same-rm-override/text()")
    private String isSameRmOverride;
    //emptyType in scheme
    @XmlPath("/xa-pool/interleaving/text()")
    private String interleaving;
    //emptyType in scheme
    @XmlPath("/xa-pool/no-tx-separate-pools/text()")
    private String noTxSeparatePools;

    //elements in security element
    @XmlPath("/security/password/text()")
    private String password;
    @XmlPath("/security/user-name/text()")
    private String userName;
    @XmlPath("/security/security-domain/text()")
    private String securityDomain;

    //elements in validation element
    @XmlPath("/validation/check-valid-connection-sql/text()")
    private String checkValidConnectionSql;
    @XmlPath("/validation/validate-on-match/text()")
    private String validateOnMatch;
    @XmlPath("/validation/background-validation/text()")
    private String backgroundValidation;
    @XmlPath("/validation/background-validation-minutes/text()")
    private String backgroundValidationMinutes;
    @XmlPath("/validation/use-fast-fail/text()")
    private String useFastFail;
    @XmlPath("/validation/exception-sorter/text()")
    private String exceptionSorter;
    @XmlPath("/validation/valid-connection-checker/text()")
    private String validConnectionChecker;
    @XmlPath("/validation/stale-connection-checker/text()")
    private String staleConnectionChecker;

    //elemnts in timeout element
    @XmlPath("/timeout/blocking-timeout-millis/text()")
    private String blockingTimeoutMillis;
    @XmlPath("/timeout/idle-timeout-minutes/text()")
    private String idleTimeoutMinutes;
    @XmlPath("/timeout/set-tx-query-timeout/text()")
    private String setTxQueryTimeout;
    @XmlPath("/timeout/query-timeout/text()")
    private String queryTimeout;
    @XmlPath("/timeout/allocation-retry/text()")
    private String allocationRetry;
    @XmlPath("/timeout/allocation-retry-wait-millis/text()")
    private String allocationRetryWaitMillis;
    @XmlPath("/timeout/use-try-lock/text()")
    private String useTryLock;
    @XmlPath("/timeout/xa-resource-timeout/text()")
    private String xaResourceTimeout;

    //elements in statement element
    @XmlPath("/statement/prepared-statement-cache-size/text()")
    private String preparedStatementCacheSize;
    @XmlPath("/statement/track-statements/text()")
    private String trackStatements;
    @XmlPath("/statement/share-prepared-statements/text()")
    private String sharePreparedStatements;

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getUseJavaContext() {
        return useJavaContext;
    }

    public void setUseJavaContext(String useJavaContext) {
        this.useJavaContext = useJavaContext;
    }

    public String getUrlDelimeter() {
        return urlDelimeter;
    }

    public void setUrlDelimeter(String urlDelimeter) {
        this.urlDelimeter = urlDelimeter;
    }

    public String getUrlSelector() {
        return urlSelector;
    }

    public void setUrlSelector(String urlSelector) {
        this.urlSelector = urlSelector;
    }

    public String getTransactionIsolation() {
        return transactionIsolation;
    }

    public void setTransactionIsolation(String transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }

    public String getNewConnectionSql() {
        return newConnectionSql;
    }

    public void setNewConnectionSql(String newConnectionSql) {
        this.newConnectionSql = newConnectionSql;
    }

    public String getXaDatasourceClass() {
        return xaDatasourceClass;
    }

    public void setXaDatasourceClass(String xaDatasourceClass) {
        this.xaDatasourceClass = xaDatasourceClass;
    }

    public Collection<XaDatasourceProperty> getXaDatasourceProperties() {
        return xaDatasourceProperties;
    }

    public void setXaDatasourceProperties(Collection<XaDatasourceProperty> xaDatasourceProperties) {
        this.xaDatasourceProperties = xaDatasourceProperties;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSecurityDomain() {
        return securityDomain;
    }

    public void setSecurityDomain(String securityDomain) {
        this.securityDomain = securityDomain;
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

    public String getBlockingTimeoutMillis() {
        return blockingTimeoutMillis;
    }

    public void setBlockingTimeoutMillis(String blockingTimeoutMillis) {
        this.blockingTimeoutMillis = blockingTimeoutMillis;
    }

    public String getIdleTimeoutMinutes() {
        return idleTimeoutMinutes;
    }

    public void setIdleTimeoutMinutes(String idleTimeoutMinutes) {
        this.idleTimeoutMinutes = idleTimeoutMinutes;
    }

    public String getSetTxQueryTimeout() {
        return setTxQueryTimeout;
    }

    public void setSetTxQueryTimeout(String setTxQueryTimeout) {
        this.setTxQueryTimeout = setTxQueryTimeout;
    }

    public String getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(String queryTimeout) {
        this.queryTimeout = queryTimeout;
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

    public String getUseTryLock() {
        return useTryLock;
    }

    public void setUseTryLock(String useTryLock) {
        this.useTryLock = useTryLock;
    }

    public String getXaResourceTimeout() {
        return xaResourceTimeout;
    }

    public void setXaResourceTimeout(String xaResourceTimeout) {
        this.xaResourceTimeout = xaResourceTimeout;
    }

    public String getCheckValidConnectionSql() {
        return checkValidConnectionSql;
    }

    public void setCheckValidConnectionSql(String checkValidConnectionSql) {
        this.checkValidConnectionSql = checkValidConnectionSql;
    }

    public String getValidateOnMatch() {
        return validateOnMatch;
    }

    public void setValidateOnMatch(String validateOnMatch) {
        this.validateOnMatch = validateOnMatch;
    }

    public String getBackgroundValidation() {
        return backgroundValidation;
    }

    public void setBackgroundValidation(String backgroundValidation) {
        this.backgroundValidation = backgroundValidation;
    }

    public String getBackgroundValidationMinutes() {
        return backgroundValidationMinutes;
    }

    public void setBackgroundValidationMinutes(String backgroundValidationMinutes) {
        this.backgroundValidationMinutes = backgroundValidationMinutes;
    }

    public String getUseFastFail() {
        return useFastFail;
    }

    public void setUseFastFail(String useFastFail) {
        this.useFastFail = useFastFail;
    }

    public String getExceptionSorter() {
        return exceptionSorter;
    }

    public void setExceptionSorter(String exceptionSorter) {
        this.exceptionSorter = exceptionSorter;
    }

    public String getValidConnectionChecker() {
        return validConnectionChecker;
    }

    public void setValidConnectionChecker(String validConnectionChecker) {
        this.validConnectionChecker = validConnectionChecker;
    }

    public String getStaleConnectionChecker() {
        return staleConnectionChecker;
    }

    public void setStaleConnectionChecker(String staleConnectionChecker) {
        this.staleConnectionChecker = staleConnectionChecker;
    }

    public String getPreparedStatementCacheSize() {
        return preparedStatementCacheSize;
    }

    public void setPreparedStatementCacheSize(String preparedStatementCacheSize) {
        this.preparedStatementCacheSize = preparedStatementCacheSize;
    }

    public String getTrackStatements() {
        return trackStatements;
    }

    public void setTrackStatements(String trackStatements) {
        this.trackStatements = trackStatements;
    }

    public String getSharePreparedStatements() {
        return sharePreparedStatements;
    }

    public void setSharePreparedStatements(String sharePreparedStatements) {
        this.sharePreparedStatements = sharePreparedStatements;
    }
}
