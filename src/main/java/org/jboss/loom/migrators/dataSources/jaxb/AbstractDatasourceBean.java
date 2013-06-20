/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.dataSources.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.List;

/**
 *  Datasource base class.
 * 
 *  @author  Ondrej Zizka
 */
public abstract class AbstractDatasourceBean {
    
    @XmlElement(name = "jndi-name")
    private String jndiName;

    @XmlElement(name = "use-java-context")
    private String useJavaContext;

    @XmlElement(name = "url-delimeter")
    private String urlDelimeter;

    @XmlElement(name = "url-selector-strategy-class-name")
    private String urlSelectorStrategyClassName;

    @XmlElement(name = "driver-class")
    private String driverClass;// Not in AS 7

    @XmlElement(name = "transaction-isolation")
    private String transactionIsolation;

    @XmlElement(name = "new-connection-sql")
    private String newConnectionSql;

    // Elements in AS7 security element
    @XmlElement(name = "user-name")
    private String userName;

    @XmlElement(name = "password")
    private String password;

    @XmlElement(name = "security-domain")
    private String securityDomain;

    
    // Elements in AS7 validation element
    @XmlElement(name = "check-valid-connection-sql")
    private String checkValidConnectionSql;

    @XmlElement(name = "validate-on-match")
    private String validateOnMatch;

    @XmlElement(name = "background-validation")
    private String backgroundValidation;

    @XmlElement(name = "background-validation-millis")
    private String backgroundValidationMillis;

    @XmlElement(name = "exception-sorter-class-name")
    private String exceptionSorterClassName;

    @XmlElement(name = "valid-connection-checker-class-name")
    private String validConnectionCheckerClassName;

    @XmlElement(name = "stale-connection-checker-class-name")
    private String staleConnectionCheckerClassName;

    
    // Elements in AS7 timeout element
    @XmlElement(name = "blocking-timeout-millis")
    private String blockingTimeoutMillis;

    @XmlElement(name = "idle-timeout-minutes")
    private String idleTimeoutMinutes;

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

    // Elements in AS7 statement element
    @XmlElement(name = "prepared-statement-cache-size")
    private String preparedStatementCacheSize;

    @XmlElement(name = "track-statements")
    private String trackStatements;

    @XmlElement(name = "share-prepared-statements")
    private String sharePreparedStatements;

    
    // Elements in pool element
    @XmlElement(name = "min-pool-size")
    private String minPoolSize;

    @XmlElement(name = "max-pool-size")
    private String maxPoolSize;

    @XmlElement(name = "prefill")
    private String prefill;

    


    @XmlElement(name = "connection-url")
    private String connectionUrl;

    // Special class for storing connection-property
    @XmlElements(@XmlElement(name = "connection-property", type = ConnectionPropertyBean.class))
    private List<ConnectionPropertyBean> connectionProperties;


    public String getTransIsolation() { return transactionIsolation; }
    public void setTransIsolation(String transIsolation) { this.transactionIsolation = transIsolation; }
    public String getConnectionUrl() { return connectionUrl; }
    public void setConnectionUrl(String connectionUrl) { this.connectionUrl = connectionUrl; }
    public List<ConnectionPropertyBean> getConnectionProperties() { return connectionProperties; }
    public void setConnectionProperties(List<ConnectionPropertyBean> connectionProperties) { this.connectionProperties = connectionProperties; }

    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getJndiName() { return jndiName; }
    public void setJndiName(String jndiName) { this.jndiName = jndiName; }
    public String getUseJavaContext() { return useJavaContext; }
    public void setUseJavaContext(String useJavaContext) { this.useJavaContext = useJavaContext; }
    public String getUrlDelimeter() { return urlDelimeter; }
    public void setUrlDelimeter(String urlDelimeter) { this.urlDelimeter = urlDelimeter; }
    public String getUrlSelectStratClName() { return urlSelectorStrategyClassName; }
    public void setUrlSelectStratClName(String urlSelectStratClName) { this.urlSelectorStrategyClassName = urlSelectStratClName; }
    public String getDriverClass() { return driverClass; }
    public void setDriverClass(String driverClass) { this.driverClass = driverClass; }
    public String getNewConnectionSql() { return newConnectionSql; }
    public void setNewConnectionSql(String newConnectionSql) { this.newConnectionSql = newConnectionSql; }
    public String getPrefill() { return prefill; }
    public void setPrefill(String prefill) { this.prefill = prefill; }
    public String getMinPoolSize() { return minPoolSize; }
    public void setMinPoolSize(String minPoolSize) { this.minPoolSize = minPoolSize; }
    public String getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(String maxPoolSize) { this.maxPoolSize = maxPoolSize; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getSecurityDomain() { return securityDomain; }
    public void setSecurityDomain(String securityDomain) { this.securityDomain = securityDomain; }
    public String getBlockingTimeMillis() { return blockingTimeoutMillis; }
    public void setBlockingTimeMillis(String blockingTimeMillis) { this.blockingTimeoutMillis = blockingTimeMillis; }
    public String getIdleTimeoutMin() { return idleTimeoutMinutes; }
    public void setIdleTimeoutMin(String idleTimeoutMin) { this.idleTimeoutMinutes = idleTimeoutMin; }
    public String getSetTxQueryTime() { return setTxQueryTimeout; }
    public void setSetTxQueryTime(String setTxQueryTime) { this.setTxQueryTimeout = setTxQueryTime; }
    public String getQueryTimeout() { return queryTimeout; }
    public void setQueryTimeout(String queryTimeout) { this.queryTimeout = queryTimeout; }
    public String getAllocationRetry() { return allocationRetry; }
    public void setAllocationRetry(String allocationRetry) { this.allocationRetry = allocationRetry; }
    public String getAllocRetryWaitMillis() { return allocationRetryWaitMillis; }
    public void setAllocRetryWaitMillis(String allocRetryWaitMillis) { this.allocationRetryWaitMillis = allocRetryWaitMillis; }
    public String getUseTryLock() { return useTryLock; }
    public void setUseTryLock(String useTryLock) { this.useTryLock = useTryLock; }
    public String getCheckValidConSql() { return checkValidConnectionSql; }
    public void setCheckValidConSql(String checkValidConSql) { this.checkValidConnectionSql = checkValidConSql; }
    public String getValidateOnMatch() { return validateOnMatch; }
    public void setValidateOnMatch(String validateOnMatch) { this.validateOnMatch = validateOnMatch; }
    public String getBackgroundValid() { return backgroundValidation; }
    public void setBackgroundValid(String backgroundValid) { this.backgroundValidation = backgroundValid; }
    public String getBackgroundValidMillis() { return backgroundValidationMillis; }
    public void setBackgroundValidMillis(String backgroundValidMillis) { this.backgroundValidationMillis = backgroundValidMillis; }
    public String getExcepSorterClName() { return exceptionSorterClassName; }
    public void setExcepSorterClName(String excepSorterClName) { this.exceptionSorterClassName = excepSorterClName; }
    public String getValidConCheckerClName() { return validConnectionCheckerClassName; }
    public void setValidConCheckerClName(String validConCheckerClName) { this.validConnectionCheckerClassName = validConCheckerClName; }
    public String getStaleConCheckerClName() { return staleConnectionCheckerClassName; }
    public void setStaleConCheckerClName(String staleConCheckerClName) { this.staleConnectionCheckerClassName = staleConCheckerClName; }
    public String getPreStatementCacheSize() { return preparedStatementCacheSize; }
    public void setPreStatementCacheSize(String preStatementCacheSize) { this.preparedStatementCacheSize = preStatementCacheSize; }
    public String getTrackStatements() { return trackStatements; }
    public void setTrackStatements(String trackStatements) { this.trackStatements = trackStatements; }
    public String getSharePreStatements() { return sharePreparedStatements; }
    public void setSharePreStatements(String sharePreStatements) { this.sharePreparedStatements = sharePreStatements; }
    //</editor-fold>
}
