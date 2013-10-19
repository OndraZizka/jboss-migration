package org.jboss.loom.migrators.ejb3;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.jboss.loom.migrators.OriginWiseJaxbBase;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;

/**
 *  Docs: https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/5/html-single/Administration_And_Configuration_Guide/index.html#Container_configuration_information-The_jboss_4_0_DTD_elements_related_to_container_configuration.
    <container-configuration>
        <container-name>Clustered CMP 2.x EntityBean</container-name>
        <call-logging>false</call-logging>
        <invoker-proxy-binding-name>clustered-entity-unified-invoker</invoker-proxy-binding-name>
        <sync-on-commit-only>false</sync-on-commit-only>
        <insert-after-ejb-post-create>false</insert-after-ejb-post-create>
        <container-interceptors>
            <interceptor>org.jboss.ejb.plugins.ProxyFactoryFinderInterceptor</interceptor>
        </container-interceptors>
        <instance-pool>org.jboss.ejb.plugins.EntityInstancePool</instance-pool>
        <instance-cache>org.jboss.ejb.plugins.EntityInstanceCache</instance-cache>
        <persistence-manager>org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager</persistence-manager>
        <locking-policy>org.jboss.ejb.plugins.lock.QueuedPessimisticEJBLock</locking-policy>
        <container-cache-conf>
            <cache-policy>org.jboss.ejb.plugins.LRUEnterpriseContextCachePolicy</cache-policy>
            <cache-policy-conf>
                <min-capacity>50</min-capacity>
                <max-capacity>1000000</max-capacity>
                <overager-period>300</overager-period>
                <max-bean-age>600</max-bean-age>
                <resizer-period>400</resizer-period>
                <max-cache-miss-period>60</max-cache-miss-period>
                <min-cache-miss-period>1</min-cache-miss-period>
                <cache-load-factor>0.75</cache-load-factor>
            </cache-policy-conf>
        </container-cache-conf>
        <container-pool-conf>
            <MaximumSize>100</MaximumSize>
        </container-pool-conf>
        <commit-option>B</commit-option>
        <cluster-config>
            <partition-name>${jboss.partition.name:DefaultPartition}</partition-name>
            <home-load-balance-policy>org.jboss.ha.framework.interfaces.RoundRobin</home-load-balance-policy>
            <bean-load-balance-policy>org.jboss.ha.framework.interfaces.FirstAvailable</bean-load-balance-policy>
        </cluster-config>
    </container-configuration>
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@ConfigPartDescriptor(
    name = "EJB container ${containerName}"
)
@XmlRootElement(name = "container-configuration")
public class ContainerConfigBean extends OriginWiseJaxbBase<ContainerConfigBean> {

    @XmlElement(name="container-name/text()")               String containerName;
    @XmlElement(name="call-logging/text()")                 String callLogging;
    @XmlElement(name="invoker-proxy-binding-name/text()")   String invokerProxyBindingName;
    @XmlElement(name="sync-on-commit-only/text()")          String syncOnCommitOnly;
    @XmlElement(name="insert-after-ejb-post-create/text()") String insertAfterEjbPostCreate;
    @XmlElement(name="locking-policy/text()")               String lockingPolicy;
    
    // Cache config
    private CacheConfig cache;
    
    // Pool config
    @XmlPath("container-pool-conf/MaximumSize/text()")
    private String poolMaximumSize; // 100
    
    // Commit option
    @XmlPath("commit-option/text()")
    private String commitOption; // B


    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getContainerName() { return containerName; }
    public void setContainerName( String containerName ) { this.containerName = containerName; }
    public String getCallLogging() { return callLogging; }
    public void setCallLogging( String callLogging ) { this.callLogging = callLogging; }
    public String getInvokerProxyBindingName() { return invokerProxyBindingName; }
    public void setInvokerProxyBindingName( String invokerProxyBindingName ) { this.invokerProxyBindingName = invokerProxyBindingName; }
    public String getSyncOnCommitOnly() { return syncOnCommitOnly; }
    public void setSyncOnCommitOnly( String syncOnCommitOnly ) { this.syncOnCommitOnly = syncOnCommitOnly; }
    public String getInsertAfterEjbPostCreate() { return insertAfterEjbPostCreate; }
    public void setInsertAfterEjbPostCreate( String insertAfterEjbPostCreate ) { this.insertAfterEjbPostCreate = insertAfterEjbPostCreate; }
    public String getLockingPolicy() { return lockingPolicy; }
    public void setLockingPolicy( String lockingPolicy ) { this.lockingPolicy = lockingPolicy; }
    public CacheConfig getCache() { return cache; }
    public void setCache( CacheConfig cache ) { this.cache = cache; }
    public String getPoolMaximumSize() { return poolMaximumSize; }
    public void setPoolMaximumSize( String poolMaximumSize ) { this.poolMaximumSize = poolMaximumSize; }
    public String getCommitOption() { return commitOption; }
    public void setCommitOption( String commitOption ) { this.commitOption = commitOption; }
    //</editor-fold>
            

    /**
     *  Cache config
     */
    @XmlRootElement(name = "container-cache-conf")
    public static class CacheConfig{
        
        @XmlPath("cache-policy-conf/min-capacity/text()")
        private String minCapacity; // 50

        @XmlPath("cache-policy-conf/max-capacity/text()")
        private String maxCapacity; // 1000000

        @XmlPath("cache-policy-conf/overager-period/text()")
        private String overagerPeriod; // 300

        @XmlPath("cache-policy-conf/max-bean-age/text()")
        private String maxBeanAge; // 600

        @XmlPath("cache-policy-conf/resizer-period/text()")
        private String resizerPeriod; // 400

        @XmlPath("cache-policy-conf/max-cache-miss-period/text()")
        private String maxCacheMissPeriod; // 60

        @XmlPath("cache-policy-conf/min-cache-miss-period/text()")
        private String minCacheMissPeriod; // 1

        @XmlPath("cache-policy-conf/cache-load-factor/text()")
        private String cacheLoadFactor; // 0.75

        //<editor-fold defaultstate="collapsed" desc="get/set">
        public String getMinCapacity() { return minCapacity; }
        public void setMinCapacity( String minCapacity ) { this.minCapacity = minCapacity; }
        public String getMaxCapacity() { return maxCapacity; }
        public void setMaxCapacity( String maxCapacity ) { this.maxCapacity = maxCapacity; }
        public String getOveragerPeriod() { return overagerPeriod; }
        public void setOveragerPeriod( String overagerPeriod ) { this.overagerPeriod = overagerPeriod; }
        public String getMaxBeanAge() { return maxBeanAge; }
        public void setMaxBeanAge( String maxBeanAge ) { this.maxBeanAge = maxBeanAge; }
        public String getResizerPeriod() { return resizerPeriod; }
        public void setResizerPeriod( String resizerPeriod ) { this.resizerPeriod = resizerPeriod; }
        public String getMaxCacheMissPeriod() { return maxCacheMissPeriod; }
        public void setMaxCacheMissPeriod( String maxCacheMissPeriod ) { this.maxCacheMissPeriod = maxCacheMissPeriod; }
        public String getMinCacheMissPeriod() { return minCacheMissPeriod; }
        public void setMinCacheMissPeriod( String minCacheMissPeriod ) { this.minCacheMissPeriod = minCacheMissPeriod; }
        public String getCacheLoadFactor() { return cacheLoadFactor; }
        public void setCacheLoadFactor( String cacheLoadFactor ) { this.cacheLoadFactor = cacheLoadFactor; }
        //</editor-fold>

    }

    
}// class
