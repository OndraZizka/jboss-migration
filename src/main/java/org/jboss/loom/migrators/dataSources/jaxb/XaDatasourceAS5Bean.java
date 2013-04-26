package org.jboss.loom.migrators.dataSources.jaxb;

import org.jboss.loom.spi.IConfigFragment;

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

public class XaDatasourceAS5Bean implements IConfigFragment {

    @XmlElement(name = "jndi-name")
    private String jndiName;

    @XmlElement(name = "use-java-context")
    private String useJavaContext;

    @XmlElement(name = "url-delimeter")
    private String urlDelimeter;

    @XmlElement(name = "url-selector-strategy-class-name")
    private String urlSelectorStratClName;

    @XmlElement(name = "prefill")
    private String prefill;

    @XmlElement(name = "xa-datasource-class")
    private String xaDatasourceClass;

    @XmlElements(@XmlElement(name = "xa-datasource-property", type = XaDatasourcePropertyBean.class))
    private Set<XaDatasourcePropertyBean> xaDatasourceProps;


    @XmlElement(name = "user-name")
    private String userName;

    @XmlElement(name = "password")
    private String password;

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

    @XmlElement(name = "min-pool-size")
    private String minPoolSize;

    @XmlElement(name = "max-pool-size")
    private String maxPoolSize;

    @XmlElement(name = "blocking-timeout-millis")
    private String blockingTimeoutMillis;

    @XmlElement(name = "idle-timeout-minutes")
    private String idleTimeoutMinutes;

    @XmlElement(name = "new-connection-sql")
    private String newConnectionSql;

    @XmlElement(name = "check-valid-connection-sql")
    private String checkValidConSql;

    @XmlElement(name = "set-tx-query-timeout")
    private String setTxQueryTimeout;

    @XmlElement(name = "query-timeout")
    private String queryTimeout;

    @XmlElement(name = "prepared-statement-cache-size")
    private String preStatementCacheSize;

    @XmlElement(name = "security-domain")
    private String securityDomain;

    @XmlElement(name = "validate-on-match")
    private String validateOnMatch;

    @XmlElement(name = "background-validation")
    private String backgroundValid;

    @XmlElement(name = "background-validation-millis")
    private String backgroundValidMillis;

    @XmlElement(name = "exception-sorter-class-name")
    private String exSorterClassName;

    @XmlElement(name = "allocation-retry")
    private String allocationRetry;

    @XmlElement(name = "allocation-retry-wait-millis")
    private String allocRetryWaitMillis;

    @XmlElement(name = "valid-connection-checker-class-name")
    private String validConCheckerClName;

    @XmlElement(name = "stale-connection-checker-class-name")
    private String staleConCheckerClName;

    @XmlElement(name = "track-statements")
    private String trackStatements;

    @XmlElement(name = "share-prepared-statements")
    private String sharePreStatements;

    @XmlElement(name = "use-try-lock")
    private String useTryLock;

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
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

    public String getUrlSelectorStratClName() {
        return urlSelectorStratClName;
    }

    public void setUrlSelectorStratClName(String urlSelectorStratClName) {
        this.urlSelectorStratClName = urlSelectorStratClName;
    }

    public String getPrefill() {
        return prefill;
    }

    public void setPrefill(String prefill) {
        this.prefill = prefill;
    }

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getNewConnectionSql() {
        return newConnectionSql;
    }

    public void setNewConnectionSql(String newConnectionSql) {
        this.newConnectionSql = newConnectionSql;
    }

    public String getCheckValidConSql() {
        return checkValidConSql;
    }

    public void setCheckValidConSql(String checkValidConSql) {
        this.checkValidConSql = checkValidConSql;
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

    public String getPreStatementCacheSize() {
        return preStatementCacheSize;
    }

    public void setPreStatementCacheSize(String preStatementCacheSize) {
        this.preStatementCacheSize = preStatementCacheSize;
    }

    public String getSecurityDomain() {
        return securityDomain;
    }

    public void setSecurityDomain(String securityDomain) {
        this.securityDomain = securityDomain;
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

    public String getBackgroundValidMillis() {
        return backgroundValidMillis;
    }

    public void setBackgroundValidMillis(String backgroundValidMillis) {
        this.backgroundValidMillis = backgroundValidMillis;
    }

    public String getExSorterClassName() {
        return exSorterClassName;
    }

    public void setExSorterClassName(String exSorterClassName) {
        this.exSorterClassName = exSorterClassName;
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

    public String getValidConCheckerClName() {
        return validConCheckerClName;
    }

    public void setValidConCheckerClName(String validConCheckerClName) {
        this.validConCheckerClName = validConCheckerClName;
    }

    public String getStaleConCheckerClName() {
        return staleConCheckerClName;
    }

    public void setStaleConCheckerClName(String staleConCheckerClName) {
        this.staleConCheckerClName = staleConCheckerClName;
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

    public String getUseTryLock() {
        return useTryLock;
    }

    public void setUseTryLock(String useTryLock) {
        this.useTryLock = useTryLock;
    }
}
