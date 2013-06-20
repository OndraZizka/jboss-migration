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

    /*
    @XmlElement(name = "driver-class")
    public String getDriverClass() { return driverClass; }
    public void setDriverClass(String driverClass) { this.driverClass = driverClass; }
    private String driverClass;// Not in AS 7 (module instead)

    @XmlElement(name = "connection-url")
    public String getConnectionUrl() { return connectionUrl; }
    public void setConnectionUrl(String connectionUrl) { this.connectionUrl = connectionUrl; }
    private String connectionUrl; // Not in AS 7 ???
    */

    @XmlElement(name = "jndi-name")
    public String getJndiName() { return jndiName; }
    public void setJndiName(String jndiName) { this.jndiName = jndiName; }
    private String jndiName;

    @XmlElement(name = "use-java-context")
    public String getUseJavaContext() { return useJavaContext; }
    public void setUseJavaContext(String useJavaContext) { this.useJavaContext = useJavaContext; }
    private String useJavaContext;

    @XmlElement(name = "url-delimeter")
    public String getUrlDelimeter() { return urlDelimeter; }
    public void setUrlDelimeter(String urlDelimeter) { this.urlDelimeter = urlDelimeter; }
    private String urlDelimeter;

    @XmlElement(name = "url-selector-strategy-class-name")
    public String getUrlSelectorStrategyClassName() { return urlSelectorStrategyClassName; }
    public void setUrlSelectorStrategyClassName( String urlSelectorStrategyClassName ) { this.urlSelectorStrategyClassName = urlSelectorStrategyClassName; }
    private String urlSelectorStrategyClassName;

    @XmlElement(name = "transaction-isolation")
    public String getTransactionIsolation() { return transactionIsolation; }
    public void setTransactionIsolation( String transactionIsolation ) { this.transactionIsolation = transactionIsolation; }
    private String transactionIsolation;

    @XmlElement(name = "new-connection-sql")
    public String getNewConnectionSql() { return newConnectionSql; }
    public void setNewConnectionSql(String newConnectionSql) { this.newConnectionSql = newConnectionSql; }
    private String newConnectionSql;

    // Elements in AS7 security element
    @XmlElement(name = "user-name")
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    private String userName;

    @XmlElement(name = "password")
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    private String password;

    @XmlElement(name = "security-domain")
    public String getSecurityDomain() { return securityDomain; }
    public void setSecurityDomain(String securityDomain) { this.securityDomain = securityDomain; }
    private String securityDomain;

    
    // Elements in AS7 validation element
    @XmlElement(name = "check-valid-connection-sql")
    public String getCheckValidConnectionSql() { return checkValidConnectionSql; }
    public void setCheckValidConnectionSql( String checkValidConnectionSql ) { this.checkValidConnectionSql = checkValidConnectionSql; }
    private String checkValidConnectionSql;

    @XmlElement(name = "validate-on-match")
    public String getValidateOnMatch() { return validateOnMatch; }
    public void setValidateOnMatch(String validateOnMatch) { this.validateOnMatch = validateOnMatch; }
    private String validateOnMatch;

    @XmlElement(name = "background-validation")
    public String getBackgroundValidation() { return backgroundValidation; }
    public void setBackgroundValidation( String backgroundValidation ) { this.backgroundValidation = backgroundValidation; }
    private String backgroundValidation;

    @XmlElement(name = "background-validation-millis")
    public String getBackgroundValidationMillis() { return backgroundValidationMillis; }
    public void setBackgroundValidationMillis( String backgroundValidationMillis ) { this.backgroundValidationMillis = backgroundValidationMillis; }
    private String backgroundValidationMillis;

    @XmlElement(name = "exception-sorter-class-name")
    public String getExceptionSorterClassName() { return exceptionSorterClassName; }
    public void setExceptionSorterClassName( String exceptionSorterClassName ) { this.exceptionSorterClassName = exceptionSorterClassName; }
    private String exceptionSorterClassName;

    @XmlElement(name = "valid-connection-checker-class-name")
    public String getValidConnectionCheckerClassName() { return validConnectionCheckerClassName; }
    public void setValidConnectionCheckerClassName( String validConnectionCheckerClassName ) { this.validConnectionCheckerClassName = validConnectionCheckerClassName; }
    private String validConnectionCheckerClassName;

    @XmlElement(name = "stale-connection-checker-class-name")
    public String getStaleConnectionCheckerClassName() { return staleConnectionCheckerClassName; }
    public void setStaleConnectionCheckerClassName( String staleConnectionCheckerClassName ) { this.staleConnectionCheckerClassName = staleConnectionCheckerClassName; }
    private String staleConnectionCheckerClassName;

    
    // Elements in AS7 timeout element
    @XmlElement(name = "blocking-timeout-millis")
    public String getBlockingTimeoutMillis() { return blockingTimeoutMillis; }
    public void setBlockingTimeoutMillis( String blockingTimeoutMillis ) { this.blockingTimeoutMillis = blockingTimeoutMillis; }
    private String blockingTimeoutMillis;

    @XmlElement(name = "idle-timeout-minutes")
    public String getIdleTimeoutMinutes() { return idleTimeoutMinutes; }
    public void setIdleTimeoutMinutes( String idleTimeoutMinutes ) { this.idleTimeoutMinutes = idleTimeoutMinutes; }
    private String idleTimeoutMinutes;

    @XmlElement(name = "set-tx-query-timeout")
    public String getSetTxQueryTimeout() { return setTxQueryTimeout; }
    public void setSetTxQueryTimeout( String setTxQueryTimeout ) { this.setTxQueryTimeout = setTxQueryTimeout; }
    private String setTxQueryTimeout;

    @XmlElement(name = "query-timeout")
    public String getQueryTimeout() { return queryTimeout; }
    public void setQueryTimeout(String queryTimeout) { this.queryTimeout = queryTimeout; }
    private String queryTimeout;

    @XmlElement(name = "allocation-retry")
    public String getAllocationRetry() { return allocationRetry; }
    public void setAllocationRetry(String allocationRetry) { this.allocationRetry = allocationRetry; }
    private String allocationRetry;

    @XmlElement(name = "allocation-retry-wait-millis")
    public String getAllocationRetryWaitMillis() { return allocationRetryWaitMillis; }
    public void setAllocationRetryWaitMillis( String allocationRetryWaitMillis ) { this.allocationRetryWaitMillis = allocationRetryWaitMillis; }
    private String allocationRetryWaitMillis;

    @XmlElement(name = "use-try-lock")
    public String getUseTryLock() { return useTryLock; }
    public void setUseTryLock(String useTryLock) { this.useTryLock = useTryLock; }
    private String useTryLock;

    // Elements in AS7 statement element
    @XmlElement(name = "prepared-statement-cache-size")
    public String getPreparedStatementCacheSize() { return preparedStatementCacheSize; }
    public void setPreparedStatementCacheSize( String preparedStatementCacheSize ) { this.preparedStatementCacheSize = preparedStatementCacheSize; }
    private String preparedStatementCacheSize;

    @XmlElement(name = "track-statements")
    public String getTrackStatements() { return trackStatements; }
    public void setTrackStatements(String trackStatements) { this.trackStatements = trackStatements; }
    private String trackStatements;

    @XmlElement(name = "share-prepared-statements")
    public String getSharePreparedStatements() { return sharePreparedStatements; }
    public void setSharePreparedStatements( String sharePreparedStatements ) { this.sharePreparedStatements = sharePreparedStatements; }
    private String sharePreparedStatements;

    
    // Elements in pool element
    @XmlElement(name = "min-pool-size")
    public String getMinPoolSize() { return minPoolSize; }
    public void setMinPoolSize(String minPoolSize) { this.minPoolSize = minPoolSize; }
    private String minPoolSize;

    @XmlElement(name = "max-pool-size")
    public String getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(String maxPoolSize) { this.maxPoolSize = maxPoolSize; }
    private String maxPoolSize;

    @XmlElement(name = "prefill")
    public String getPrefill() { return prefill; }
    public void setPrefill(String prefill) { this.prefill = prefill; }
    private String prefill;


    
    // Connection properties
    @XmlElements(@XmlElement(name = "connection-property", type = ConnectionPropertyBean.class))
    public List<ConnectionPropertyBean> getConnectionProperties() { return connectionProperties; }
    public void setConnectionProperties(List<ConnectionPropertyBean> connectionProperties) { this.connectionProperties = connectionProperties; }
    private List<ConnectionPropertyBean> connectionProperties;

}// class
