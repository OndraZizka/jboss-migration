package org.jboss.loom.migrators.dataSources.jaxb;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlElement;

/**
 * User: Roman Jakubco
 * Date: 4/28/13
 */
public abstract class AbstractDatasourceAS7Bean{
    private String linkedSecurity;

    @XmlPath("@jndi-name")
    private String jndiName;

    @XmlPath("@pool-name")
    private String poolName;

    @XmlPath("@enabled")
    private String enabled;

    @XmlPath("@use-java-context")
    private String useJavaContext;

    @XmlElement(name = "url-delimeter")
    private String urlDelimeter;

    @XmlElement(name = "url-selector-strategy-class-name")
    private String urlSelector;

    @XmlElement(name = "driver")
    private String driver;

    @XmlElement(name = "transaction-isolation")
    private String transIsolation;

    @XmlElement(name = "new-connection-sql")
    private String newConnectionSql;


    // Elements in security element
    @XmlPath("/security/password/text()")
    private String password;

    @XmlPath("/security/user-name/text()")
    private String userName;

    @XmlPath("/security/security-domain/text()")
    private String securityDomain;


    // Elements in validation element
    @XmlPath("/validation/check-valid-connection-sql/text()")
    private String checkValidConSql;

    @XmlPath("/validation/validate-on-match/text()")
    private String validateOnMatch;

    @XmlPath("/validation/background-validation/text()")
    private String backgroundValid;

    @XmlPath("/validation/background-validation-minutes/text()")
    private String backgroundValidMin;

    @XmlPath("/validation/use-fast-fail/text()")
    private String useFastFail;

    @XmlPath("/validation/exception-sorter/text()")
    private String exceptionSorter;

    @XmlPath("/validation/valid-connection-checker/text()")
    private String validConChecker;

    @XmlPath("/validation/stale-connection-checker/text()")
    private String staleConChecker;


    // Elements in timeout element
    @XmlPath("/timeout/blocking-timeout-millis/text()")
    private String blockingTimeoutMillis;

    @XmlPath("/timeout/idle-timeout-minutes/text()")
    private String idleTimeoutMin;

    @XmlPath("/timeout/set-tx-query-timeout/text()")
    private String setTxQueryTimeout;

    @XmlPath("/timeout/query-timeout/text()")
    private String queryTimeout;

    @XmlPath("/timeout/allocation-retry/text()")
    private String allocationRetry;

    @XmlPath("/timeout/allocation-retry-wait-millis/text()")
    private String allocRetryWaitMillis;

    @XmlPath("/timeout/use-try-lock/text()")
    private String useTryLock;


    // Elements in statement element
    @XmlPath("/statement/prepared-statement-cache-size/text()")
    private String preStatementCacheSize;

    @XmlPath("/statement/track-statements/text()")
    private String trackStatements;

    @XmlPath("/statement/share-prepared-statements/text()")
    private String sharePreStatements;




    public String getLinkedSecurity() {
        return linkedSecurity;
    }

    public String getJndiName() {
        return jndiName;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
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

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getTransIsolation() {
        return transIsolation;
    }

    public void setTransIsolation(String transIsolation) {
        this.transIsolation = transIsolation;
    }

    public String getNewConnectionSql() {
        return newConnectionSql;
    }

    public void setNewConnectionSql(String newConnectionSql) {
        this.newConnectionSql = newConnectionSql;
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
        linkedSecurity = securityDomain;
    }

    public String getBlockingTimeoutMillis() {
        return blockingTimeoutMillis;
    }

    public void setBlockingTimeoutMillis(String blockingTimeoutMillis) {
        this.blockingTimeoutMillis = blockingTimeoutMillis;
    }

    public String getIdleTimeoutMin() {
        return idleTimeoutMin;
    }

    public void setIdleTimeoutMin(String idleTimeoutMin) {
        this.idleTimeoutMin = idleTimeoutMin;
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

    public String getAllocRetryWaitMillis() {
        return allocRetryWaitMillis;
    }

    public void setAllocRetryWaitMillis(String allocRetryWaitMillis) {
        this.allocRetryWaitMillis = allocRetryWaitMillis;
    }

    public String getUseTryLock() {
        return useTryLock;
    }

    public void setUseTryLock(String useTryLock) {
        this.useTryLock = useTryLock;
    }

    public String getCheckValidConSql() {
        return checkValidConSql;
    }

    public void setCheckValidConSql(String checkValidConSql) {
        this.checkValidConSql = checkValidConSql;
    }

    public String getValidateOnMatch() {
        return validateOnMatch;
    }

    public void setValidateOnMatch(String validateOnMatch) {
        this.validateOnMatch = validateOnMatch;
    }

    public String getBackgroundValid() {
        return backgroundValid;
    }

    public void setBackgroundValid(String backgroundValid) {
        this.backgroundValid = backgroundValid;
    }

    public String getBackgroundValidMin() {
        return backgroundValidMin;
    }

    public void setBackgroundValidMin(String backgroundValidMin) {
        this.backgroundValidMin = backgroundValidMin;
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

    public String getValidConChecker() {
        return validConChecker;
    }

    public void setValidConChecker(String validConChecker) {
        this.validConChecker = validConChecker;
    }

    public String getStaleConChecker() {
        return staleConChecker;
    }

    public void setStaleConChecker(String staleConChecker) {
        this.staleConChecker = staleConChecker;
    }

    public String getPreStatementCacheSize() {
        return preStatementCacheSize;
    }

    public void setPreStatementCacheSize(String preStatementCacheSize) {
        this.preStatementCacheSize = preStatementCacheSize;
    }

    public String getTrackStatements() {
        return trackStatements;
    }

    public void setTrackStatements(String trackStatements) {
        this.trackStatements = trackStatements;
    }

    public String getSharePreStatements() {
        return sharePreStatements;
    }

    public void setSharePreStatements(String sharePreStatements) {
        this.sharePreStatements = sharePreStatements;
    }
}
