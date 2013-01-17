package cz.muni.fi.jboss.Migration.DataSources;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing xa-datasource in AS5 (AS5)
 *
 * @author: Roman Jakubco
 * Date: 8/26/12
 * Time: 2:17 PM
 */

@XmlRootElement(name = "xa-datasource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "xa-datasource")

public class XaDatasourceAS5 {

   @XmlElement(name = "jndi-name")
    private String jndiName;
    @XmlElement(name = "use-java-context")
    private String useJavaContext;
    @XmlElement(name = "url-delimeter")
    private  String urlDelimeter;
    @XmlElement(name = "url-selector-strategy-class-name")
    private String urlSelectorStratClName;
    @XmlElement(name = "prefill")
    private String prefill;
    @XmlElement(name = "xa-datasource-class")
    private String xaDatasourceClass;

    @XmlElements(@XmlElement(name = "xa-datasource-property", type = XaDatasourceProperty.class))
    private Set<XaDatasourceProperty> xaDatasourceProps;


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


    @XmlElement(name = "transaction-isolation" )
    private String transIsolation;
    @XmlElement(name = "min-pool-size")
    private String minPoolSize;
    @XmlElement(name = "max-pool-size")
    private String maxPoolSize;
    @XmlElement(name = "blocking-timeout-millis")
    private String blockingTimeoutMillis;
    @XmlElement(name = "idle-timeout-minutes")
    private String idleTimeoutMinutes;
    @XmlElement(name = "new-connection-sql" )
    private String newConnectionSql;
    @XmlElement(name = "check-valid-connection-sql")
    private String checkValidConSql;
    @XmlElement(name = "set-tx-query-timeout")
    private String setTxQueryTimeout;
    @XmlElement(name = "query-timeout")
    private String queryTimeout;
    @XmlElement(name = "prepared-statement-cache-size")
    private String preStatementCacheSize;
    @XmlElement(name = "security-domain" )
    private String securityDomain;
    @XmlElement(name = "validate-on-match")
    private String validateOnMatch;
    @XmlElement(name = "background-validation")
    private String backgroundValid;
    @XmlElement(name = "background-validation-millis" )
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

    public Collection<XaDatasourceProperty> getXaDatasourceProps() {
        return xaDatasourceProps;
    }

    public void setXaDatasourceProps(Collection<XaDatasourceProperty> xaDatasourceProps) {
        Set<XaDatasourceProperty> temp = new HashSet();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XaDatasourceAS5)) return false;

        XaDatasourceAS5 that = (XaDatasourceAS5) o;

        if (allocationRetry != null ? !allocationRetry.equals(that.allocationRetry) : that.allocationRetry != null)
            return false;
        if (allocRetryWaitMillis != null ? !allocRetryWaitMillis.equals(that.allocRetryWaitMillis) : that.allocRetryWaitMillis != null)
            return false;
        if (backgroundValid != null ? !backgroundValid.equals(that.backgroundValid) : that.backgroundValid != null)
            return false;
        if (backgroundValidMillis != null ? !backgroundValidMillis.equals(that.backgroundValidMillis) : that.backgroundValidMillis != null)
            return false;
        if (blockingTimeoutMillis != null ? !blockingTimeoutMillis.equals(that.blockingTimeoutMillis) : that.blockingTimeoutMillis != null)
            return false;
        if (checkValidConSql != null ? !checkValidConSql.equals(that.checkValidConSql) : that.checkValidConSql != null)
            return false;
        if (exSorterClassName != null ? !exSorterClassName.equals(that.exSorterClassName) : that.exSorterClassName != null)
            return false;
        if (idleTimeoutMinutes != null ? !idleTimeoutMinutes.equals(that.idleTimeoutMinutes) : that.idleTimeoutMinutes != null)
            return false;
        if (interleaving != null ? !interleaving.equals(that.interleaving) : that.interleaving != null) return false;
        if (isSameRM != null ? !isSameRM.equals(that.isSameRM) : that.isSameRM != null) return false;
        if (jndiName != null ? !jndiName.equals(that.jndiName) : that.jndiName != null) return false;
        if (maxPoolSize != null ? !maxPoolSize.equals(that.maxPoolSize) : that.maxPoolSize != null) return false;
        if (minPoolSize != null ? !minPoolSize.equals(that.minPoolSize) : that.minPoolSize != null) return false;
        if (newConnectionSql != null ? !newConnectionSql.equals(that.newConnectionSql) : that.newConnectionSql != null)
            return false;
        if (noTxSeparatePools != null ? !noTxSeparatePools.equals(that.noTxSeparatePools) : that.noTxSeparatePools != null)
            return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (prefill != null ? !prefill.equals(that.prefill) : that.prefill != null) return false;
        if (preStatementCacheSize != null ? !preStatementCacheSize.equals(that.preStatementCacheSize) : that.preStatementCacheSize != null)
            return false;
        if (queryTimeout != null ? !queryTimeout.equals(that.queryTimeout) : that.queryTimeout != null) return false;
        if (securityDomain != null ? !securityDomain.equals(that.securityDomain) : that.securityDomain != null)
            return false;
        if (setTxQueryTimeout != null ? !setTxQueryTimeout.equals(that.setTxQueryTimeout) : that.setTxQueryTimeout != null)
            return false;
        if (sharePreStatements != null ? !sharePreStatements.equals(that.sharePreStatements) : that.sharePreStatements != null)
            return false;
        if (staleConCheckerClName != null ? !staleConCheckerClName.equals(that.staleConCheckerClName) : that.staleConCheckerClName != null)
            return false;
        if (trackStatements != null ? !trackStatements.equals(that.trackStatements) : that.trackStatements != null)
            return false;
        if (transIsolation != null ? !transIsolation.equals(that.transIsolation) : that.transIsolation != null)
            return false;
        if (urlDelimeter != null ? !urlDelimeter.equals(that.urlDelimeter) : that.urlDelimeter != null) return false;
        if (urlSelectorStratClName != null ? !urlSelectorStratClName.equals(that.urlSelectorStratClName) : that.urlSelectorStratClName != null)
            return false;
        if (useJavaContext != null ? !useJavaContext.equals(that.useJavaContext) : that.useJavaContext != null)
            return false;
        if (useTryLock != null ? !useTryLock.equals(that.useTryLock) : that.useTryLock != null) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (validConCheckerClName != null ? !validConCheckerClName.equals(that.validConCheckerClName) : that.validConCheckerClName != null)
            return false;
        if (validateOnMatch != null ? !validateOnMatch.equals(that.validateOnMatch) : that.validateOnMatch != null)
            return false;
        if (xaDatasourceClass != null ? !xaDatasourceClass.equals(that.xaDatasourceClass) : that.xaDatasourceClass != null)
            return false;
        if (xaDatasourceProps != null ? !xaDatasourceProps.equals(that.xaDatasourceProps) : that.xaDatasourceProps != null)
            return false;
        if (xaResourceTimeout != null ? !xaResourceTimeout.equals(that.xaResourceTimeout) : that.xaResourceTimeout != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = jndiName != null ? jndiName.hashCode() : 0;
        result = 31 * result + (useJavaContext != null ? useJavaContext.hashCode() : 0);
        result = 31 * result + (urlDelimeter != null ? urlDelimeter.hashCode() : 0);
        result = 31 * result + (urlSelectorStratClName != null ? urlSelectorStratClName.hashCode() : 0);
        result = 31 * result + (prefill != null ? prefill.hashCode() : 0);
        result = 31 * result + (xaDatasourceClass != null ? xaDatasourceClass.hashCode() : 0);
        result = 31 * result + (xaDatasourceProps != null ? xaDatasourceProps.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (isSameRM != null ? isSameRM.hashCode() : 0);
        result = 31 * result + (interleaving != null ? interleaving.hashCode() : 0);
        result = 31 * result + (noTxSeparatePools != null ? noTxSeparatePools.hashCode() : 0);
        result = 31 * result + (xaResourceTimeout != null ? xaResourceTimeout.hashCode() : 0);
        result = 31 * result + (transIsolation != null ? transIsolation.hashCode() : 0);
        result = 31 * result + (minPoolSize != null ? minPoolSize.hashCode() : 0);
        result = 31 * result + (maxPoolSize != null ? maxPoolSize.hashCode() : 0);
        result = 31 * result + (blockingTimeoutMillis != null ? blockingTimeoutMillis.hashCode() : 0);
        result = 31 * result + (idleTimeoutMinutes != null ? idleTimeoutMinutes.hashCode() : 0);
        result = 31 * result + (newConnectionSql != null ? newConnectionSql.hashCode() : 0);
        result = 31 * result + (checkValidConSql != null ? checkValidConSql.hashCode() : 0);
        result = 31 * result + (setTxQueryTimeout != null ? setTxQueryTimeout.hashCode() : 0);
        result = 31 * result + (queryTimeout != null ? queryTimeout.hashCode() : 0);
        result = 31 * result + (preStatementCacheSize != null ? preStatementCacheSize.hashCode() : 0);
        result = 31 * result + (securityDomain != null ? securityDomain.hashCode() : 0);
        result = 31 * result + (validateOnMatch != null ? validateOnMatch.hashCode() : 0);
        result = 31 * result + (backgroundValid != null ? backgroundValid.hashCode() : 0);
        result = 31 * result + (backgroundValidMillis != null ? backgroundValidMillis.hashCode() : 0);
        result = 31 * result + (exSorterClassName != null ? exSorterClassName.hashCode() : 0);
        result = 31 * result + (allocationRetry != null ? allocationRetry.hashCode() : 0);
        result = 31 * result + (allocRetryWaitMillis != null ? allocRetryWaitMillis.hashCode() : 0);
        result = 31 * result + (validConCheckerClName != null ? validConCheckerClName.hashCode() : 0);
        result = 31 * result + (staleConCheckerClName != null ? staleConCheckerClName.hashCode() : 0);
        result = 31 * result + (trackStatements != null ? trackStatements.hashCode() : 0);
        result = 31 * result + (sharePreStatements != null ? sharePreStatements.hashCode() : 0);
        result = 31 * result + (useTryLock != null ? useTryLock.hashCode() : 0);
        return result;
    }
}
