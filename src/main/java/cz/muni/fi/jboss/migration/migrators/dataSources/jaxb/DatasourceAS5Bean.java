package cz.muni.fi.jboss.migration.migrators.dataSources.jaxb;


import cz.muni.fi.jboss.migration.spi.IConfigFragment;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Class for unmarshalling and representing local-tx-datasource (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "local-tx-datasource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "local-tx-datasource")

public class DatasourceAS5Bean implements IConfigFragment {

    @XmlElement(name = "jndi-name")
    private String jndiName;

    @XmlElement(name = "use-java-context")
    private String useJavaContext;

    @XmlElement(name = "url-delimeter")
    private String urlDelimeter;

    @XmlElement(name = "url-selector-strategy-class-name")
    private String urlSelectStratClName;

    @XmlElement(name = "connection-url")
    private String connectionUrl;

    @XmlElement(name = "driver-class")
    private String driverClass;

    @XmlElement(name = "new-connection-sql")
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

    @XmlElement(name = "security-domain")
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

    @XmlElement(name = "background-validation-millis")
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
    

    // Special class for storing connection-property
    @XmlElements(@XmlElement(name = "connection-property", type = ConnectionPropertyBean.class))
    private List<ConnectionPropertyBean> connectionProperties;

    
    // TODO: All props above are the same as in NoTxDatasourceAS5Bean.
    
    
    @XmlElement(name = "transaction-isolation")
    private String transIsolation;


    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    
    public List<ConnectionPropertyBean> getConnectionProperties() {
        return connectionProperties;
    }
    
    public void setConnectionProperties(Collection<ConnectionPropertyBean> connectionProperties) {
        List<ConnectionPropertyBean> temp = new LinkedList();
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
    //</editor-fold>
    
}// class
