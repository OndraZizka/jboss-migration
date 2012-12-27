package cz.fi.muni.jboss.migration.dataSources; /**
 * 
 * @author  Roman Jakubco
 * Date: 8/26/12
 * Time: 2:17 PM
 */

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.List;

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
    private String urlSelectorStrategyClassName;
    @XmlElement(name = "connection-url")
    private String connectionUrl;
    @XmlElement(name = "driver-class")
    private String driverClass;

    @XmlElement(name = "transaction-isolation" )
    private String transactionIsolation;
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
    private String blockingTimeoutMillis;
    @XmlElement(name = "idle-timeout-minutes")
    private String idletimeoutMinutes;
    @XmlElement(name = "set-tx-query-timeout")
    private String setTxQueryTimeout;
    @XmlElement(name = "query-timeout")
    private String queryTimeout;
    @XmlElement(name = "allocation-retry")
    private String allocationRetry;
    @XmlElement(name = "allocation-retry-wait-millis")
    private String allocationRetryWaitMillis;
    @XmlElement(name = "use-try-lock")
    private String useTryLock;

    @XmlElement(name = "check-valid-connection-sql")
    private String checkValidConnectionSql;
    @XmlElement(name = "validate-on-match")
    private String validateOnMatch;
    @XmlElement(name = "background-validation")
    private String backgroundValidation;
    @XmlElement(name = "background-validation-millis" )
    private String backgroundValidationMillis;
    @XmlElement(name = "exception-sorter-class-name")
    private String exceptionSorterClassName;
    @XmlElement(name = "valid-connection-checker-class-name")
    private String validConnectionCheckerClassName;
    @XmlElement(name = "stale-connection-checker-class-name")
    private String staleConnectionCheckerClassName;

    @XmlElement(name = "prepared-statement-cache-size")
    private String preparedStatementCacheSize;
    @XmlElement(name = "track-statements")
    private String trackStatements;
    @XmlElement(name = "share-prepared-statements")
    private String sharePreparedStatements;

    //special class for storing connection-property
    @XmlElements(@XmlElement(name ="connection-property", type=ConnectionProperty.class))
    private List<ConnectionProperty> connectionProperties;




    public Collection<ConnectionProperty> getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(List<ConnectionProperty> connectionProperties) {
        this.connectionProperties = connectionProperties;
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

    public String getUrlSelectorStrategyClassName() {
        return urlSelectorStrategyClassName;
    }

    public void setUrlSelectorStrategyClassName(String urlSelectorStrategyClassName) {
        this.urlSelectorStrategyClassName = urlSelectorStrategyClassName;
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



    public String getTransactionIsolation() {
        return transactionIsolation;
    }

    public void setTransactionIsolation(String transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
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
        return idletimeoutMinutes;
    }

    public void setIdletimeoutMinutes(String idleTimeoutMinutes) {
        this.idletimeoutMinutes = idleTimeoutMinutes;
    }

    public String getNewConnectionSql() {
        return newConnectionSql;
    }

    public void setNewConnectionSql(String newConnectionSql) {
        this.newConnectionSql = newConnectionSql;
    }

    public String getCheckValidConnectionSql() {
        return checkValidConnectionSql;
    }

    public void setCheckValidConnectionSql(String checkValidConnectionSql) {
        this.checkValidConnectionSql = checkValidConnectionSql;
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

    public String getUseTryLock() {
        return useTryLock;
    }

    public void setUseTryLock(String useTryLock) {
        this.useTryLock = useTryLock;
    }

    public String getPreparedStatementCacheSize() {
        return preparedStatementCacheSize;
    }

    public void setPreparedStatementCacheSize(String preparedStatementCacheSize) {
        this.preparedStatementCacheSize = preparedStatementCacheSize;
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

    public String getExceptionSorterClassName() {
        return exceptionSorterClassName;
    }

    public void setExceptionSorterClassName(String exceptionSorterClassName) {
        this.exceptionSorterClassName = exceptionSorterClassName;
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

    public String getValidConnectionCheckerClassName() {
        return validConnectionCheckerClassName;
    }

    public void setValidConnectionCheckerClassName(String validConnectionCheckerClassName) {
        this.validConnectionCheckerClassName = validConnectionCheckerClassName;
    }

    public String getStaleConnectionCheckerClassName() {
        return staleConnectionCheckerClassName;
    }

    public void setStaleConnectionCheckerClassName(String staleConnectionCheckerClassName) {
        this.staleConnectionCheckerClassName = staleConnectionCheckerClassName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatasourceAS5)) return false;

        DatasourceAS5 that = (DatasourceAS5) o;

        if (allocationRetry != null ? !allocationRetry.equals(that.allocationRetry) : that.allocationRetry != null)
            return false;
        if (allocationRetryWaitMillis != null ? !allocationRetryWaitMillis.equals(that.allocationRetryWaitMillis) : that.allocationRetryWaitMillis != null)
            return false;
        if (backgroundValidation != null ? !backgroundValidation.equals(that.backgroundValidation) : that.backgroundValidation != null)
            return false;
        if (backgroundValidationMillis != null ? !backgroundValidationMillis.equals(that.backgroundValidationMillis) : that.backgroundValidationMillis != null)
            return false;
        if (blockingTimeoutMillis != null ? !blockingTimeoutMillis.equals(that.blockingTimeoutMillis) : that.blockingTimeoutMillis != null)
            return false;
        if (checkValidConnectionSql != null ? !checkValidConnectionSql.equals(that.checkValidConnectionSql) : that.checkValidConnectionSql != null)
            return false;
        if (connectionProperties != null ? !connectionProperties.equals(that.connectionProperties) : that.connectionProperties != null)
            return false;
        if (connectionUrl != null ? !connectionUrl.equals(that.connectionUrl) : that.connectionUrl != null)
            return false;
        if (driverClass != null ? !driverClass.equals(that.driverClass) : that.driverClass != null) return false;
        if (exceptionSorterClassName != null ? !exceptionSorterClassName.equals(that.exceptionSorterClassName) : that.exceptionSorterClassName != null)
            return false;
        if (idletimeoutMinutes != null ? !idletimeoutMinutes.equals(that.idletimeoutMinutes) : that.idletimeoutMinutes != null)
            return false;
        if (maxPoolSize != null ? !maxPoolSize.equals(that.maxPoolSize) : that.maxPoolSize != null) return false;
        if (minPoolSize != null ? !minPoolSize.equals(that.minPoolSize) : that.minPoolSize != null) return false;
        if (jndiName != null ? !jndiName.equals(that.jndiName) : that.jndiName != null) return false;
        if (newConnectionSql != null ? !newConnectionSql.equals(that.newConnectionSql) : that.newConnectionSql != null)
            return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (prefill != null ? !prefill.equals(that.prefill) : that.prefill != null) return false;
        if (preparedStatementCacheSize != null ? !preparedStatementCacheSize.equals(that.preparedStatementCacheSize) : that.preparedStatementCacheSize != null)
            return false;
        if (queryTimeout != null ? !queryTimeout.equals(that.queryTimeout) : that.queryTimeout != null) return false;
        if (securityDomain != null ? !securityDomain.equals(that.securityDomain) : that.securityDomain != null)
            return false;
        if (setTxQueryTimeout != null ? !setTxQueryTimeout.equals(that.setTxQueryTimeout) : that.setTxQueryTimeout != null)
            return false;
        if (sharePreparedStatements != null ? !sharePreparedStatements.equals(that.sharePreparedStatements) : that.sharePreparedStatements != null)
            return false;
        if (staleConnectionCheckerClassName != null ? !staleConnectionCheckerClassName.equals(that.staleConnectionCheckerClassName) : that.staleConnectionCheckerClassName != null)
            return false;
        if (trackStatements != null ? !trackStatements.equals(that.trackStatements) : that.trackStatements != null)
            return false;
        if (transactionIsolation != null ? !transactionIsolation.equals(that.transactionIsolation) : that.transactionIsolation != null)
            return false;
        if (urlDelimeter != null ? !urlDelimeter.equals(that.urlDelimeter) : that.urlDelimeter != null) return false;
        if (urlSelectorStrategyClassName != null ? !urlSelectorStrategyClassName.equals(that.urlSelectorStrategyClassName) : that.urlSelectorStrategyClassName != null)
            return false;
        if (useJavaContext != null ? !useJavaContext.equals(that.useJavaContext) : that.useJavaContext != null)
            return false;
        if (useTryLock != null ? !useTryLock.equals(that.useTryLock) : that.useTryLock != null) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (validConnectionCheckerClassName != null ? !validConnectionCheckerClassName.equals(that.validConnectionCheckerClassName) : that.validConnectionCheckerClassName != null)
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
        result = 31 * result + (urlSelectorStrategyClassName != null ? urlSelectorStrategyClassName.hashCode() : 0);
        result = 31 * result + (connectionUrl != null ? connectionUrl.hashCode() : 0);
        result = 31 * result + (driverClass != null ? driverClass.hashCode() : 0);
        result = 31 * result + (transactionIsolation != null ? transactionIsolation.hashCode() : 0);
        result = 31 * result + (newConnectionSql != null ? newConnectionSql.hashCode() : 0);
        result = 31 * result + (prefill != null ? prefill.hashCode() : 0);
        result = 31 * result + (minPoolSize != null ? minPoolSize.hashCode() : 0);
        result = 31 * result + (maxPoolSize != null ? maxPoolSize.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (securityDomain != null ? securityDomain.hashCode() : 0);
        result = 31 * result + (blockingTimeoutMillis != null ? blockingTimeoutMillis.hashCode() : 0);
        result = 31 * result + (idletimeoutMinutes != null ? idletimeoutMinutes.hashCode() : 0);
        result = 31 * result + (setTxQueryTimeout != null ? setTxQueryTimeout.hashCode() : 0);
        result = 31 * result + (queryTimeout != null ? queryTimeout.hashCode() : 0);
        result = 31 * result + (allocationRetry != null ? allocationRetry.hashCode() : 0);
        result = 31 * result + (allocationRetryWaitMillis != null ? allocationRetryWaitMillis.hashCode() : 0);
        result = 31 * result + (useTryLock != null ? useTryLock.hashCode() : 0);
        result = 31 * result + (checkValidConnectionSql != null ? checkValidConnectionSql.hashCode() : 0);
        result = 31 * result + (validateOnMatch != null ? validateOnMatch.hashCode() : 0);
        result = 31 * result + (backgroundValidation != null ? backgroundValidation.hashCode() : 0);
        result = 31 * result + (backgroundValidationMillis != null ? backgroundValidationMillis.hashCode() : 0);
        result = 31 * result + (exceptionSorterClassName != null ? exceptionSorterClassName.hashCode() : 0);
        result = 31 * result + (validConnectionCheckerClassName != null ? validConnectionCheckerClassName.hashCode() : 0);
        result = 31 * result + (staleConnectionCheckerClassName != null ? staleConnectionCheckerClassName.hashCode() : 0);
        result = 31 * result + (preparedStatementCacheSize != null ? preparedStatementCacheSize.hashCode() : 0);
        result = 31 * result + (trackStatements != null ? trackStatements.hashCode() : 0);
        result = 31 * result + (sharePreparedStatements != null ? sharePreparedStatements.hashCode() : 0);
        result = 31 * result + (connectionProperties != null ? connectionProperties.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DatasourceAS5{" +
                "jndiName='" + jndiName + '\'' +
                ", useJavaContext=" + useJavaContext +
                ", urlDelimeter='" + urlDelimeter + '\'' +
                ", urlSelectorStrategyClassName='" + urlSelectorStrategyClassName + '\'' +
                ", connectionUrl='" + connectionUrl + '\'' +
                ", driverClass='" + driverClass + '\'' +
                ", transactionIsolation='" + transactionIsolation + '\'' +
                ", newConnectionSql='" + newConnectionSql + '\'' +
                ", prefill=" + prefill +
                ", minPoolSize=" + minPoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", securityDomain='" + securityDomain + '\'' +
                ", blockingTimeoutMillis=" + blockingTimeoutMillis +
                ", idletimeoutMinutes=" + idletimeoutMinutes +
                ", setTxQueryTimeout=" + setTxQueryTimeout +
                ", queryTimeout=" + queryTimeout +
                ", allocationRetry=" + allocationRetry +
                ", allocationRetryWaitMillis=" + allocationRetryWaitMillis +
                ", useTryLock=" + useTryLock +
                ", checkValidConnectionSql='" + checkValidConnectionSql + '\'' +
                ", validateOnMatch=" + validateOnMatch +
                ", backgroundValidation=" + backgroundValidation +
                ", backgroundValidationMillis=" + backgroundValidationMillis +
                ", exceptionSorterClassName='" + exceptionSorterClassName + '\'' +
                ", validConnectionCheckerClassName='" + validConnectionCheckerClassName + '\'' +
                ", staleConnectionCheckerClassName='" + staleConnectionCheckerClassName + '\'' +
                ", preparedStatementCacheSize=" + preparedStatementCacheSize +
                ", trackStatements='" + trackStatements + '\'' +
                ", sharePreparedStatements=" + sharePreparedStatements +
                ", connectionProperties=" + connectionProperties +
                '}';
    }
}
