/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.dataSources.jaxb;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlElement;
import org.jboss.loom.spi.ann.Property;

/**
 *  @author Roman Jakubco
 */
@Property.Access( Property.Access.Type.FIELD )
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
    private String transactionIsolation;

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


    // Elements in timeout element
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


    // Elements in statement element
    @XmlPath("/statement/prepared-statement-cache-size/text()")
    private String preparedStatementCacheSize;

    @XmlPath("/statement/track-statements/text()")
    private String trackStatements;

    @XmlPath("/statement/share-prepared-statements/text()")
    private String sharePreparedStatements;
    


    @XmlPath("/pool/min-pool-size/text()")
    public String getMinPoolSize() { return minPoolSize; }
    public void setMinPoolSize(String minPoolSize) { this.minPoolSize = minPoolSize; }
    private String minPoolSize;
    
    @XmlPath("/pool/max-pool-size/text()")
    public String getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(String maxPoolSize) { this.maxPoolSize = maxPoolSize; }
    private String maxPoolSize;

    // Elements in pool element
    @XmlPath("/pool/prefill/text()")
    public String getPrefill() { return prefill; }
    public void setPrefill(String prefill) { this.prefill = prefill; }
    private String prefill;

    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getLinkedSecurity() { return linkedSecurity; }
    public String getJndiName() { return jndiName; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public void setJndiName(String jndiName) { this.jndiName = jndiName; }
    public String getPoolName() { return poolName; }
    public void setPoolName(String poolName) { this.poolName = poolName; }
    public String getUseJavaContext() { return useJavaContext; }
    public void setUseJavaContext(String useJavaContext) { this.useJavaContext = useJavaContext; }
    public String getUrlDelimeter() { return urlDelimeter; }
    public void setUrlDelimeter(String urlDelimeter) { this.urlDelimeter = urlDelimeter; }
    public String getUrlSelector() { return urlSelector; }
    public void setUrlSelector(String urlSelector) { this.urlSelector = urlSelector; }
    public String getDriver() { return driver; }
    public void setDriver(String driver) { this.driver = driver; }
    public String getTransIsolation() { return transactionIsolation; }
    public void setTransIsolation(String transIsolation) { this.transactionIsolation = transIsolation; }
    public String getNewConnectionSql() { return newConnectionSql; }
    public void setNewConnectionSql(String newConnectionSql) { this.newConnectionSql = newConnectionSql; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getSecurityDomain() { return securityDomain; }
    public void setSecurityDomain(String securityDomain) {
        this.securityDomain = securityDomain;
        linkedSecurity = securityDomain;
    }
    public String getBlockingTimeoutMillis() { return blockingTimeoutMillis; }
    public void setBlockingTimeoutMillis(String blockingTimeoutMillis) { this.blockingTimeoutMillis = blockingTimeoutMillis; }
    public String getIdleTimeoutMin() { return idleTimeoutMinutes; }
    public void setIdleTimeoutMin(String idleTimeoutMin) { this.idleTimeoutMinutes = idleTimeoutMin; }
    public String getSetTxQueryTimeout() { return setTxQueryTimeout; }
    public void setSetTxQueryTimeout(String setTxQueryTimeout) { this.setTxQueryTimeout = setTxQueryTimeout; }
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
    public String getBackgroundValidMin() { return backgroundValidationMinutes; }
    public void setBackgroundValidMin(String backgroundValidMin) { this.backgroundValidationMinutes = backgroundValidMin; }
    public String getUseFastFail() { return useFastFail; }
    public void setUseFastFail(String useFastFail) { this.useFastFail = useFastFail; }
    public String getExceptionSorter() { return exceptionSorter; }
    public void setExceptionSorter(String exceptionSorter) { this.exceptionSorter = exceptionSorter; }
    public String getValidConChecker() { return validConnectionChecker; }
    public void setValidConChecker(String validConChecker) { this.validConnectionChecker = validConChecker; }
    public String getStaleConChecker() { return staleConnectionChecker; }
    public void setStaleConChecker(String staleConChecker) { this.staleConnectionChecker = staleConChecker; }
    public String getPreStatementCacheSize() { return preparedStatementCacheSize; }
    public void setPreStatementCacheSize(String preStatementCacheSize) { this.preparedStatementCacheSize = preStatementCacheSize; }
    public String getTrackStatements() { return trackStatements; }
    public void setTrackStatements(String trackStatements) { this.trackStatements = trackStatements; }
    public String getSharePreStatements() { return sharePreparedStatements; }
    public void setSharePreStatements(String sharePreStatements) { this.sharePreparedStatements = sharePreStatements; }
    //</editor-fold>
    
}// class
