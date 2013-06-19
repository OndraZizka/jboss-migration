/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.connectionFactories.jaxb;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * JAXB bean for connection-definition ( AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "connection-definition")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "connection-definition")

public class ConnectionDefinitionBean {

    @XmlAttribute(name = "class-name")
    private String className;

    @XmlAttribute(name = "enabled")
    private String enabled;

    @XmlAttribute(name = "jndi-name")
    private String jndiName;

    @XmlAttribute(name = "pool-name")
    private String poolName;

    @XmlAttribute(name = "use-ccm")
    private String useCcm;

    @XmlAttribute(name = "use-java-context")
    private String useJavaCont;

    @XmlElements(@XmlElement(name = "config-property", type = ConfigPropertyBean.class))
    private Set<ConfigPropertyBean> configProperties;

    @XmlPath("/pool/prefill/text()")
    private String prefill;

    @XmlPath("/pool/use-strict-min/text()")
    private String useStrictMin;

    @XmlPath("/pool/flush-strategy/text()")
    private String flushStrategy;

    @XmlPath("/pool/min-pool-size/text()")
    private String minPoolSize;

    @XmlPath("/pool/max-pool-size/text()")
    private String maxPoolSize;

    @XmlPath("/xa-pool/prefill/text()")
    private String xaPrefill;

    @XmlPath("/xa-pool/use-strict-min/text()")
    private String xaUseStrictMin;

    @XmlPath("/xa-pool/flush-strategy/text()")
    private String xaFlushStrategy;

    @XmlPath("/xa-pool/min-pool-size/text()")
    private String xaMinPoolSize;

    @XmlPath("/xa-pool/max-pool-size/text()")
    private String xaMaxPoolSize;

    @XmlPath("/xa-pool/no-tx-separate-pools/text()")
    private String xaNoTxSeparatePools;

    @XmlPath("/security/security-domain/text()")
    private String securityDomain;

    @XmlPath("/security/security-domain-and-application/text()")
    private String secDomainAndApp;

    @XmlPath("/security/application-managed-security/text()")
    private String appManagedSec;

    @XmlPath("/validation/background-validation/text()")
    private String backgroundValidation;

    @XmlPath("/validation/background-validation-millis/text()")
    private String backgroundValiMillis;

    @XmlPath("/timeout/blocking-timeout-millis/text()")
    private String blockingTimeoutMillis;

    @XmlPath("/timeout/idle-timeout-minutes/text()")
    private String idleTimeoutMinutes;

    @XmlPath("/timeout/allocation-retry/text()")
    private String allocationRetry;

    @XmlPath("/timeout/allocation-retry-wait-millis/text()")
    private String allocRetryWaitMillis;

    @XmlPath("/timeout/xa-resource-timeout/text()")
    private String xaResourceTimeout;

    public Set<ConfigPropertyBean> getConfigProperties() {
        return configProperties;
    }

    public void setConfigProperties(Collection<ConfigPropertyBean> configProperties) {
        Set<ConfigPropertyBean> temp = new HashSet();
        temp.addAll(configProperties);
        this.configProperties = temp;

    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getJndiName() {
        return jndiName;
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

    public String getUseCcm() {
        return useCcm;
    }

    public void setUseCcm(String useCcm) {
        this.useCcm = useCcm;
    }

    public String getUseJavaCont() {
        return useJavaCont;
    }

    public void setUseJavaCont(String useJavaCont) {
        this.useJavaCont = useJavaCont;
    }

    public String getPrefill() {
        return prefill;
    }

    public void setPrefill(String prefill) {
        this.prefill = prefill;
    }

    public String getUseStrictMin() {
        return useStrictMin;
    }

    public void setUseStrictMin(String useStrictMin) {
        this.useStrictMin = useStrictMin;
    }

    public String getFlushStrategy() {
        return flushStrategy;
    }

    public void setFlushStrategy(String flushStrategy) {
        this.flushStrategy = flushStrategy;
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

    public String getSecurityDomain() {
        return securityDomain;
    }

    public void setSecurityDomain(String securityDomain) {
        this.securityDomain = securityDomain;
    }

    public String getSecDomainAndApp() {
        return secDomainAndApp;
    }

    public void setSecDomainAndApp(String secDomainAndApp) {
        this.secDomainAndApp = secDomainAndApp;
    }

    public String getAppManagedSec() {
        return appManagedSec;
    }

    public void setAppManagedSec(String appManagedSec) {
        this.appManagedSec = appManagedSec;
    }

    public String getBackgroundValidation() {
        return backgroundValidation;
    }

    public void setBackgroundValidation(String backgroundValidation) {
        this.backgroundValidation = backgroundValidation;
    }

    public String getBackgroundValiMillis() {
        return backgroundValiMillis;
    }

    public void setBackgroundValiMillis(String backgroundValiMillis) {
        this.backgroundValiMillis = backgroundValiMillis;
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

    public String getAllocationRetry() {
        return allocationRetry;
    }

    public String getAllocRetryWaitMillis() {
        return allocRetryWaitMillis;
    }

    public void setAllocRetryWaitMillis(String allocRetryWaitMillis) {
        this.allocRetryWaitMillis = allocRetryWaitMillis;
    }

    public void setAllocationRetry(String allocationRetry) {
        this.allocationRetry = allocationRetry;
    }

    public String getXaResourceTimeout() {
        return xaResourceTimeout;
    }

    public void setXaResourceTimeout(String xaResourceTimeout) {
        this.xaResourceTimeout = xaResourceTimeout;
    }

    public void setConfigProperties(Set<ConfigPropertyBean> configProperties) {
        this.configProperties = configProperties;
    }

    public String getXaPrefill() {
        return xaPrefill;
    }

    public void setXaPrefill(String xaPrefill) {
        this.xaPrefill = xaPrefill;
    }

    public String getXaUseStrictMin() {
        return xaUseStrictMin;
    }

    public void setXaUseStrictMin(String xaUseStrictMin) {
        this.xaUseStrictMin = xaUseStrictMin;
    }

    public String getXaFlushStrategy() {
        return xaFlushStrategy;
    }

    public void setXaFlushStrategy(String xaFlushStrategy) {
        this.xaFlushStrategy = xaFlushStrategy;
    }

    public String getXaMinPoolSize() {
        return xaMinPoolSize;
    }

    public void setXaMinPoolSize(String xaMinPoolSize) {
        this.xaMinPoolSize = xaMinPoolSize;
    }

    public String getXaMaxPoolSize() {
        return xaMaxPoolSize;
    }

    public void setXaMaxPoolSize(String xaMaxPoolSize) {
        this.xaMaxPoolSize = xaMaxPoolSize;
    }

    public String getXaNoTxSeparatePools() {
        return xaNoTxSeparatePools;
    }

    public void setXaNoTxSeparatePools(String xaNoTxSeparatePools) {
        this.xaNoTxSeparatePools = xaNoTxSeparatePools;
    }
}