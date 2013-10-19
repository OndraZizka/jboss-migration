package org.jboss.loom.migrators.ejb3;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.jboss.loom.migrators.MBeanJaxbBase;
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.spi.IConfigFragment;

/**
 * $JBOSS_HOME/server/all/deploy/juddi-service.sar/META-INF/jboss-service.xml

    <invoker-proxy-binding>
      <name>jms-message-inflow-driven-bean</name>
      <invoker-mbean>default</invoker-mbean>
      <proxy-factory>org.jboss.ejb.plugins.inflow.JBossJMSMessageEndpointFactory</proxy-factory>
      <proxy-factory-config>
        <activation-config>
           <activation-config-property>
              <activation-config-property-name>providerAdapterJNDI</activation-config-property-name>
              <activation-config-property-value>DefaultJMSProvider</activation-config-property-value>
           </activation-config-property>
           <activation-config-property>
              <activation-config-property-name>minSession</activation-config-property-name>
              <activation-config-property-value>1</activation-config-property-value>
           </activation-config-property>
           <activation-config-property>
              <activation-config-property-name>maxSession</activation-config-property-name>
              <activation-config-property-value>15</activation-config-property-value>
           </activation-config-property>
           <activation-config-property>
              <activation-config-property-name>keepAlive</activation-config-property-name>
              <activation-config-property-value>60000</activation-config-property-value>
           </activation-config-property>
           <activation-config-property>
              <activation-config-property-name>maxMessages</activation-config-property-name>
              <activation-config-property-value>1</activation-config-property-value>
           </activation-config-property>
           <activation-config-property>
              <activation-config-property-name>reconnectInterval</activation-config-property-name>
              <activation-config-property-value>10</activation-config-property-value>
           </activation-config-property>
           <activation-config-property>
              <activation-config-property-name>useDLQ</activation-config-property-name>
              <activation-config-property-value>true</activation-config-property-value>
           </activation-config-property>
           <activation-config-property>
              <activation-config-property-name>DLQHandler</activation-config-property-name>
              <activation-config-property-value>org.jboss.resource.adapter.jms.inflow.dlq.GenericDLQHandler</activation-config-property-value>
           </activation-config-property>
           <activation-config-property>
              <activation-config-property-name>DLQJNDIName</activation-config-property-name>
              <activation-config-property-value>queue/DLQ</activation-config-property-value>
           </activation-config-property>
           <activation-config-property>
              <activation-config-property-name>DLQMaxResent</activation-config-property-name>
              <activation-config-property-value>10</activation-config-property-value>
           </activation-config-property>
        </activation-config>
        <endpoint-interceptors>
          <interceptor>org.jboss.proxy.ClientMethodInterceptor</interceptor>
          <interceptor>org.jboss.ejb.plugins.inflow.MessageEndpointInterceptor</interceptor>
          <interceptor>org.jboss.proxy.TransactionInterceptor</interceptor>
          <interceptor>org.jboss.invocation.InvokerInterceptor</interceptor>
        </endpoint-interceptors>
      </proxy-factory-config>
    </invoker-proxy-binding>
 * 
 * @Jira: MIGR-39
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name = "invoker-proxy-binding")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "invoker-proxy-binding")
public final class InvokerProxyBindingBean extends MBeanJaxbBase<InvokerProxyBindingBean> implements IConfigFragment, Origin.Wise {

    @XmlElement
    private String name;
    
    @XmlElement(name="invoker-mbean")
    private String invokerMbean;
    
    
    @XmlPath("proxy-factory-config/activation-config-property[activation-config-property-name/text()='providerAdapterJNDI']/activation-config-property-value/text()")
    private String providerAdapterJNDI;
    
    @XmlPath("proxy-factory-config/activation-config-property[activation-config-property-name/text()='minSession']/activation-config-property-value/text()")
    private String minSession;
    
    @XmlPath("proxy-factory-config/activation-config-property[activation-config-property-name/text()='maxSession']/activation-config-property-value/text()")
    private String maxSession;
    
    @XmlPath("proxy-factory-config/activation-config-property[activation-config-property-name/text()='keepAlive']/activation-config-property-value/text()")
    private String keepAlive;
    
    @XmlPath("proxy-factory-config/activation-config-property[activation-config-property-name/text()='maxMessages']/activation-config-property-value/text()")
    private String maxMessages;
    
    @XmlPath("proxy-factory-config/activation-config-property[activation-config-property-name/text()='reconnectInterval']/activation-config-property-value/text()")
    private String reconnectInterval;
    
    @XmlPath("proxy-factory-config/activation-config-property[activation-config-property-name/text()='useDLQ']/activation-config-property-value/text()")
    private String useDLQ;
    
    @XmlPath("proxy-factory-config/activation-config-property[activation-config-property-name/text()='DLQHandler']/activation-config-property-value/text()")
    private String DLQHandler;
    
    @XmlPath("proxy-factory-config/activation-config-property[activation-config-property-name/text()='DLQJNDIName']/activation-config-property-value/text()")
    private String DLQJNDIName;
    
    @XmlPath("proxy-factory-config/activation-config-property[activation-config-property-name/text()='DLQMaxResent']/activation-config-property-value/text()")
    private String DLQMaxResent;


    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getName() { return name; }
    public void setName( String name ) { this.name = name; }
    public String getInvokerMbean() { return invokerMbean; }
    public void setInvokerMbean( String invokerMbean ) { this.invokerMbean = invokerMbean; }
    
    public String getProviderAdapterJNDI() { return providerAdapterJNDI; }
    public void setProviderAdapterJNDI( String providerAdapterJNDI ) { this.providerAdapterJNDI = providerAdapterJNDI; }
    public String getMinSession() { return minSession; }
    public void setMinSession( String minSession ) { this.minSession = minSession; }
    public String getMaxSession() { return maxSession; }
    public void setMaxSession( String maxSession ) { this.maxSession = maxSession; }
    public String getKeepAlive() { return keepAlive; }
    public void setKeepAlive( String keepAlive ) { this.keepAlive = keepAlive; }
    public String getMaxMessages() { return maxMessages; }
    public void setMaxMessages( String maxMessages ) { this.maxMessages = maxMessages; }
    public String getReconnectInterval() { return reconnectInterval; }
    public void setReconnectInterval( String reconnectInterval ) { this.reconnectInterval = reconnectInterval; }
    public String getUseDLQ() { return useDLQ; }
    public void setUseDLQ( String useDLQ ) { this.useDLQ = useDLQ; }
    public String getDLQHandler() { return DLQHandler; }
    public void setDLQHandler( String DLQHandler ) { this.DLQHandler = DLQHandler; }
    public String getDLQJNDIName() { return DLQJNDIName; }
    public void setDLQJNDIName( String DLQJNDIName ) { this.DLQJNDIName = DLQJNDIName; }
    public String getDLQMaxResent() { return DLQMaxResent; }
    public void setDLQMaxResent( String DLQMaxResent ) { this.DLQMaxResent = DLQMaxResent; }
    //</editor-fold>
    
}// class
