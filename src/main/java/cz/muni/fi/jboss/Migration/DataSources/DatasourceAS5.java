package cz.muni.fi.jboss.Migration.DataSources;


import cz.muni.fi.jboss.Migration.ConnectionFactories.ConfigProperty;

import javax.xml.bind.annotation.*;
import java.util.*;

/**
 * Class for unmarshalling and representing local-tx-datasource (AS5)
 *
 * @author: Roman Jakubco
 * Date: 8/26/12
 * Time: 2:17 PM
 */

@XmlRootElement(name = "local-tx-datasource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "local-tx-datasource")

public class DatasourceAS5 {

    @XmlElement(name = "jndi-name")
    private String jndiName;
    @XmlElement(name = "use-java-context")
    private String useJavaContext;
    @XmlElement(name = "url-delimeter")
    private  String urlDelimeter;
    @XmlElement(name = "url-selector-strategy-class-name")
    private String urlSelectStratClName;
    @XmlElement(name = "connection-url")
    private String connectionUrl;
    @XmlElement(name = "driver-class")
    private String driverClass;

    @XmlElement(name = "transaction-isolation" )
    private String transIsolation;
    @XmlElement(name = "new-connection-sql" )
    private String newConnectionSql;

    @XmlElement(name = "prefill")
    private String prefill;
    @XmlElement(name = "min-pool-size")
    private String minPoolSize;
    @XmlElement(name = "max-pool-size")
    private String maxPoolSize;

    @XmlElement(name = "user-name")
    private String userName;
    @XmlElement(name = "password")
    private String password;
    @XmlElement(name = "security-domain" )
    private String securityDomain;

    @XmlElement(name = "blocking-timeout-millis")
    private String blockingTimeMillis;
    @XmlElement(name = "idle-timeout-minutes")
    private String idleTimeoutMin;
    @XmlElement(name = "set-tx-query-timeout")
    private String setTxQueryTime;
    @XmlElement(name = "query-timeout")
    private String queryTimeout;
    @XmlElement(name = "allocation-retry")
    private String allocationRetry;
    @XmlElement(name = "allocation-retry-wait-millis")
    private String allocRetryWaitMillis;
    @XmlElement(name = "use-try-lock")
    private String useTryLock;

    @XmlElement(name = "check-valid-connection-sql")
    private String checkValidConSql;
    @XmlElement(name = "validate-on-match")
    private String validateOnMatch;
    @XmlElement(name = "background-validation")
    private String backgroundValid;
    @XmlElement(name = "background-validation-millis" )
    private String backgroundValidMillis;
    @XmlElement(name = "exception-sorter-class-name")
    private String excepSorterClName;
    @XmlElement(name = "valid-connection-checker-class-name")
    private String validConCheckerClName;
    @XmlElement(name = "stale-connection-checker-class-name")
    private String staleConCheckerClName;

    @XmlElement(name = "prepared-statement-cache-size")
    private String preStatementCacheSize;
    @XmlElement(name = "track-statements")
    private String trackStatements;
    @XmlElement(name = "share-prepared-statements")
    private String sharePreStatements;

    // special class for storing connection-property
    @XmlElements(@XmlElement(name = "connection-property", type = ConnectionProperty.class))
    private List<ConnectionProperty> connectionProperties;




    public List<ConnectionProperty> getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(Collection<ConnectionProperty> connectionProperties) {
        List<ConnectionProperty> temp = new ArrayList();
        temp.addAll(connectionProperties);
        this.connectionProperties = temp;
    }


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

    public String getUrlSelectStratClName() {
        return urlSelectStratClName;
    }

    public void setUrlSelectStratClName(String urlSelectStratClName) {
        this.urlSelectStratClName = urlSelectStratClName;
    }

    public String getPrefill() {
        return prefill;
    }

    public void setPrefill(String prefill) {
        this.prefill = prefill;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
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

    public String getBlockingTimeMillis() {
        return blockingTimeMillis;
    }

    public void setBlockingTimeMillis(String blockingTimeMillis) {
        this.blockingTimeMillis = blockingTimeMillis;
    }

    public String getIdleTimeoutMinutes() {
        return idleTimeoutMin;
    }

    public void setIdleTimeoutMin(String idleTimeoutMinutes) {
        this.idleTimeoutMin = idleTimeoutMinutes;
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

    public String getSetTxQueryTime() {
        return setTxQueryTime;
    }

    public void setSetTxQueryTime(String setTxQueryTime) {
        this.setTxQueryTime = setTxQueryTime;
    }

    public String getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(String queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public String getUseTryLock() {
        return useTryLock;
    }

    public void setUseTryLock(String useTryLock) {
        this.useTryLock = useTryLock;
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

    public String getExcepSorterClName() {
        return excepSorterClName;
    }

    public void setExcepSorterClName(String excepSorterClName) {
        this.excepSorterClName = excepSorterClName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatasourceAS5)) return false;

        DatasourceAS5 that = (DatasourceAS5) o;

        if (allocationRetry != null ? !allocationRetry.equals(that.allocationRetry) : that.allocationRetry != null)
            return false;
        if (allocRetryWaitMillis != null ? !allocRetryWaitMillis.equals(that.allocRetryWaitMillis) : that.allocRetryWaitMillis != null)
            return false;
        if (backgroundValid != null ? !backgroundValid.equals(that.backgroundValid) : that.backgroundValid != null)
            return false;
        if (backgroundValidMillis != null ? !backgroundValidMillis.equals(that.backgroundValidMillis) : that.backgroundValidMillis != null)
            return false;
        if (blockingTimeMillis != null ? !blockingTimeMillis.equals(that.blockingTimeMillis) : that.blockingTimeMillis != null)
            return false;
        if (checkValidConSql != null ? !checkValidConSql.equals(that.checkValidConSql) : that.checkValidConSql != null)
            return false;
        if (connectionProperties != null ? !connectionProperties.equals(that.connectionProperties) : that.connectionProperties != null)
            return false;
        if (connectionUrl != null ? !connectionUrl.equals(that.connectionUrl) : that.connectionUrl != null)
            return false;
        if (driverClass != null ? !driverClass.equals(that.driverClass) : that.driverClass != null) return false;
        if (excepSorterClName != null ? !excepSorterClName.equals(that.excepSorterClName) : that.excepSorterClName != null)
            return false;
        if (idleTimeoutMin != null ? !idleTimeoutMin.equals(that.idleTimeoutMin) : that.idleTimeoutMin != null)
            return false;
        if (maxPoolSize != null ? !maxPoolSize.equals(that.maxPoolSize) : that.maxPoolSize != null) return false;
        if (minPoolSize != null ? !minPoolSize.equals(that.minPoolSize) : that.minPoolSize != null) return false;
        if (jndiName != null ? !jndiName.equals(that.jndiName) : that.jndiName != null) return false;
        if (newConnectionSql != null ? !newConnectionSql.equals(that.newConnectionSql) : that.newConnectionSql != null)
            return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (prefill != null ? !prefill.equals(that.prefill) : that.prefill != null) return false;
        if (preStatementCacheSize != null ? !preStatementCacheSize.equals(that.preStatementCacheSize) : that.preStatementCacheSize != null)
            return false;
        if (queryTimeout != null ? !queryTimeout.equals(that.queryTimeout) : that.queryTimeout != null) return false;
        if (securityDomain != null ? !securityDomain.equals(that.securityDomain) : that.securityDomain != null)
            return false;
        if (setTxQueryTime != null ? !setTxQueryTime.equals(that.setTxQueryTime) : that.setTxQueryTime != null)
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
        if (urlSelectStratClName != null ? !urlSelectStratClName.equals(that.urlSelectStratClName) : that.urlSelectStratClName != null)
            return false;
        if (useJavaContext != null ? !useJavaContext.equals(that.useJavaContext) : that.useJavaContext != null)
            return false;
        if (useTryLock != null ? !useTryLock.equals(that.useTryLock) : that.useTryLock != null) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (validConCheckerClName != null ? !validConCheckerClName.equals(that.validConCheckerClName) : that.validConCheckerClName != null)
            return false;
        if (validateOnMatch != null ? !validateOnMatch.equals(that.validateOnMatch) : that.validateOnMatch != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = jndiName != null ? jndiName.hashCode() : 0;
        result = 31 * result + (useJavaContext != null ? useJavaContext.hashCode() : 0);
        result = 31 * result + (urlDelimeter != null ? urlDelimeter.hashCode() : 0);
        result = 31 * result + (urlSelectStratClName != null ? urlSelectStratClName.hashCode() : 0);
        result = 31 * result + (connectionUrl != null ? connectionUrl.hashCode() : 0);
        result = 31 * result + (driverClass != null ? driverClass.hashCode() : 0);
        result = 31 * result + (transIsolation != null ? transIsolation.hashCode() : 0);
        result = 31 * result + (newConnectionSql != null ? newConnectionSql.hashCode() : 0);
        result = 31 * result + (prefill != null ? prefill.hashCode() : 0);
        result = 31 * result + (minPoolSize != null ? minPoolSize.hashCode() : 0);
        result = 31 * result + (maxPoolSize != null ? maxPoolSize.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (securityDomain != null ? securityDomain.hashCode() : 0);
        result = 31 * result + (blockingTimeMillis != null ? blockingTimeMillis.hashCode() : 0);
        result = 31 * result + (idleTimeoutMin != null ? idleTimeoutMin.hashCode() : 0);
        result = 31 * result + (setTxQueryTime != null ? setTxQueryTime.hashCode() : 0);
        result = 31 * result + (queryTimeout != null ? queryTimeout.hashCode() : 0);
        result = 31 * result + (allocationRetry != null ? allocationRetry.hashCode() : 0);
        result = 31 * result + (allocRetryWaitMillis != null ? allocRetryWaitMillis.hashCode() : 0);
        result = 31 * result + (useTryLock != null ? useTryLock.hashCode() : 0);
        result = 31 * result + (checkValidConSql != null ? checkValidConSql.hashCode() : 0);
        result = 31 * result + (validateOnMatch != null ? validateOnMatch.hashCode() : 0);
        result = 31 * result + (backgroundValid != null ? backgroundValid.hashCode() : 0);
        result = 31 * result + (backgroundValidMillis != null ? backgroundValidMillis.hashCode() : 0);
        result = 31 * result + (excepSorterClName != null ? excepSorterClName.hashCode() : 0);
        result = 31 * result + (validConCheckerClName != null ? validConCheckerClName.hashCode() : 0);
        result = 31 * result + (staleConCheckerClName != null ? staleConCheckerClName.hashCode() : 0);
        result = 31 * result + (preStatementCacheSize != null ? preStatementCacheSize.hashCode() : 0);
        result = 31 * result + (trackStatements != null ? trackStatements.hashCode() : 0);
        result = 31 * result + (sharePreStatements != null ? sharePreStatements.hashCode() : 0);
        result = 31 * result + (connectionProperties != null ? connectionProperties.hashCode() : 0);
        return result;
    }

}
