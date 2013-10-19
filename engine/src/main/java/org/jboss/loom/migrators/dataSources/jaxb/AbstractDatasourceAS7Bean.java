/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.dataSources.jaxb;

import javax.xml.bind.annotation.XmlElement;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.jboss.loom.spi.ann.Property;

/**
 *  @author Roman Jakubco
 */
@Property.Access( Property.Access.Type.FIELD )
public abstract class AbstractDatasourceAS7Bean // TODO: extends AbstractDatasourceBean
{
    private String linkedSecurity;

    @Property(label = "driver name")
    @XmlElement(name = "driver")
    public String getDriver() { return driver; }
    public void setDriver(String driver) { this.driver = driver; }
    private String driver;

    @XmlPath("@jndi-name")
    public String getJndiName() { return jndiName; }
    public void setJndiName(String jndiName) { this.jndiName = jndiName; }
    private String jndiName;// == In base class

    @XmlPath("@pool-name")
    public String getPoolName() { return poolName; }
    public void setPoolName(String poolName) { this.poolName = poolName; }
    private String poolName;

    @XmlPath("@enabled")
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    private String enabled;

    @XmlPath("@use-java-context")
    public String getUseJavaContext() { return useJavaContext; }
    public void setUseJavaContext(String useJavaContext) { this.useJavaContext = useJavaContext; }
    private String useJavaContext;//

    @XmlElement(name = "url-delimeter")
    public String getUrlDelimeter() { return urlDelimeter; }
    public void setUrlDelimeter(String urlDelimeter) { this.urlDelimeter = urlDelimeter; }
    private String urlDelimeter;//

    @XmlElement(name = "url-selector-strategy-class-name")
    public String getUrlSelectorStrategyClassName() { return urlSelectorStrategyClassName; }
    public void setUrlSelectorStrategyClassName( String urlSelectorStrategyClassName ) { this.urlSelectorStrategyClassName = urlSelectorStrategyClassName; }
    private String urlSelectorStrategyClassName;//

    @XmlElement(name = "transaction-isolation")
    public String getTransactionIsolation() { return transactionIsolation; }
    public void setTransactionIsolation( String transactionIsolation ) { this.transactionIsolation = transactionIsolation; }
    private String transactionIsolation;//

    @XmlElement(name = "new-connection-sql")
    public String getNewConnectionSql() { return newConnectionSql; }
    public void setNewConnectionSql(String newConnectionSql) { this.newConnectionSql = newConnectionSql; }
    private String newConnectionSql;//


    // Elements in security element
    @XmlPath("/security/password/text()")
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    private String password;//

    @XmlPath("/security/user-name/text()")
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    private String userName;//

    @XmlPath("/security/security-domain/text()")
    public String getSecurityDomain() { return securityDomain; }
    public void setSecurityDomain(String securityDomain) {
        this.securityDomain = securityDomain;
        linkedSecurity = securityDomain;
    }
    private String securityDomain;//


    // Elements in validation element
    @XmlPath("/validation/check-valid-connection-sql/text()")
    public String getCheckValidConnectionSql() { return checkValidConnectionSql; }
    public void setCheckValidConnectionSql( String checkValidConnectionSql ) { this.checkValidConnectionSql = checkValidConnectionSql; }
    private String checkValidConnectionSql;//

    @XmlPath("/validation/validate-on-match/text()")
    public String getValidateOnMatch() { return validateOnMatch; }
    public void setValidateOnMatch( String validateOnMatch ) { this.validateOnMatch = validateOnMatch; }
    private String validateOnMatch;//

    @XmlPath("/validation/background-validation/text()")
    public String getBackgroundValidation() { return backgroundValidation; }
    public void setBackgroundValidation( String backgroundValidation ) { this.backgroundValidation = backgroundValidation; }
    private String backgroundValidation; //

    @XmlPath("/validation/background-validation-minutes/text()")
    public String getBackgroundValidationMinutes() { return backgroundValidationMinutes; }
    public void setBackgroundValidationMinutes( String backgroundValidationMinutes ) { this.backgroundValidationMinutes = backgroundValidationMinutes; }
    private String backgroundValidationMinutes;/// Millis

    @XmlPath("/validation/use-fast-fail/text()")
    public String getUseFastFail() { return useFastFail; }
    public void setUseFastFail( String useFastFail ) { this.useFastFail = useFastFail; }
    private String useFastFail;

    @XmlPath("/validation/exception-sorter/text()")
    public String getExceptionSorter() { return exceptionSorter; }
    public void setExceptionSorter( String exceptionSorter ) { this.exceptionSorter = exceptionSorter; }
    private String exceptionSorter; /// ~ClassName

    @XmlPath("/validation/valid-connection-checker/text()")
    public String getValidConnectionChecker() { return validConnectionChecker; }
    public void setValidConnectionChecker( String validConnectionChecker ) { this.validConnectionChecker = validConnectionChecker; }
    private String validConnectionChecker;///~ClassName

    @XmlPath("/validation/stale-connection-checker/text()")
    public String getStaleConnectionChecker() { return staleConnectionChecker; }
    public void setStaleConnectionChecker( String staleConnectionChecker ) { this.staleConnectionChecker = staleConnectionChecker; }
    private String staleConnectionChecker;///~ClassName


    // Elements in timeout element
    @XmlPath("/timeout/blocking-timeout-millis/text()")
    public String getBlockingTimeoutMillis() { return blockingTimeoutMillis; }
    public void setBlockingTimeoutMillis( String blockingTimeoutMillis ) { this.blockingTimeoutMillis = blockingTimeoutMillis; }
    private String blockingTimeoutMillis;//

    @XmlPath("/timeout/idle-timeout-minutes/text()")
    public String getIdleTimeoutMinutes() { return idleTimeoutMinutes; }
    public void setIdleTimeoutMinutes( String idleTimeoutMinutes ) { this.idleTimeoutMinutes = idleTimeoutMinutes; }
    private String idleTimeoutMinutes;//
    
    @XmlPath("/timeout/set-tx-query-timeout/text()")
    public String getSetTxQueryTimeout() { return setTxQueryTimeout; }
    public void setSetTxQueryTimeout( String setTxQueryTimeout ) { this.setTxQueryTimeout = setTxQueryTimeout; }
    private String setTxQueryTimeout;//

    @XmlPath("/timeout/query-timeout/text()")
    public String getQueryTimeout() { return queryTimeout; }
    public void setQueryTimeout( String queryTimeout ) { this.queryTimeout = queryTimeout; }
    private String queryTimeout;//

    @XmlPath("/timeout/allocation-retry/text()")
    public String getAllocationRetry() { return allocationRetry; }
    public void setAllocationRetry( String allocationRetry ) { this.allocationRetry = allocationRetry; }
    private String allocationRetry;//

    @XmlPath("/timeout/allocation-retry-wait-millis/text()")
    public String getAllocationRetryWaitMillis() { return allocationRetryWaitMillis; }
    public void setAllocationRetryWaitMillis( String allocationRetryWaitMillis ) { this.allocationRetryWaitMillis = allocationRetryWaitMillis; }
    private String allocationRetryWaitMillis;//

    @XmlPath("/timeout/use-try-lock/text()")
    public String getUseTryLock() { return useTryLock; }
    public void setUseTryLock( String useTryLock ) { this.useTryLock = useTryLock; }
    private String useTryLock;//


    // Elements in statement element
    @XmlPath("/statement/prepared-statement-cache-size/text()")
    public String getPreparedStatementCacheSize() { return preparedStatementCacheSize; }
    public void setPreparedStatementCacheSize( String preparedStatementCacheSize ) { this.preparedStatementCacheSize = preparedStatementCacheSize; }
    private String preparedStatementCacheSize;//

    @XmlPath("/statement/track-statements/text()")
    public String getTrackStatements() { return trackStatements; }
    public void setTrackStatements( String trackStatements ) { this.trackStatements = trackStatements; }
    private String trackStatements;//

    @XmlPath("/statement/share-prepared-statements/text()")
    public String getSharePreparedStatements() { return sharePreparedStatements; }
    public void setSharePreparedStatements( String sharePreparedStatements ) { this.sharePreparedStatements = sharePreparedStatements; }
    private String sharePreparedStatements;//
    


    // Elements in pool element
    @XmlPath("/pool/min-pool-size/text()")
    public String getMinPoolSize() { return minPoolSize; }
    public void setMinPoolSize(String minPoolSize) { this.minPoolSize = minPoolSize; }
    private String minPoolSize;//
    
    @XmlPath("/pool/max-pool-size/text()")
    public String getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(String maxPoolSize) { this.maxPoolSize = maxPoolSize; }
    private String maxPoolSize;//

    @XmlPath("/pool/prefill/text()")
    public String getPrefill() { return prefill; }
    public void setPrefill(String prefill) { this.prefill = prefill; }
    private String prefill;//

    
    public String getLinkedSecurity() { return linkedSecurity; }    
    
}// class
