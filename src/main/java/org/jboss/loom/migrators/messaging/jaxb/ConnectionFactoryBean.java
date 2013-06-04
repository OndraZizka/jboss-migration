package org.jboss.loom.migrators.messaging.jaxb;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.jboss.loom.migrators.MBeanJaxbBase;

/**
 *  $JBOSS_HOME/server/$PROFILE/deploy/messaging/connection-factories-service.xml
 * 
    <mbean code="org.jboss.jms.server.connectionfactory.ConnectionFactory"
            name="jboss.messaging.connectionfactory:service=MyExampleConnectionFactory"
            xmbean-dd="xmdesc/ConnectionFactory-xmbean.xml">
      
        <constructor>
           <!-- You can specify the default Client ID to use for connections created using this factory --> 
           <arg type="java.lang.String" value="MyClientID"/>
        </constructor>

        <depends optional-attribute-name="ServerPeer">jboss.messaging:service=ServerPeer</depends>

        <!-- The transport to use - can be bisocket, sslbisocket or http -->
        <depends optional-attribute-name="Connector">jboss.messaging:service=Connector,transport=http</depends>
        <depends>jboss.messaging:service=PostOffice</depends>

        <!-- PrefetchSize determines the approximate maximum number of messages the client consumer will buffer locally -->
        <attribute name="PrefetchSize">150</attribute>

        <!-- Paging params to be used for temporary queues -->
        <attribute name="DefaultTempQueueFullSize">200000</attribute>
        <attribute name="DefaultTempQueuePageSizeSize">2000</attribute>
        <attribute name="DefaultTempQueueDownCacheSize">2000</attribute>
        <!-- The batch size to use when using the DUPS_OK_ACKNOWLEDGE acknowledgement mode -->
        <attribute name="DupsOKBatchSize">5000</attribute>
        <!-- Does this connection factory support automatic failover? -->
        <attribute name="SupportsFailover">false</attribute>
        <!-- Does this connection factory support automatic client side load balancing? -->
        <attribute name="SupportsLoadBalancing">false</attribute>  
        <!-- The class name of the factory used to create the load balancing policy to use on the client side -->
        <attribute name="LoadBalancingFactory">org.jboss.jms.client.plugin.RoundRobinLoadBalancingFactory</attribute>  
        <!-- Whether we should be strict TCK compliant, i.e. how we deal with foreign messages, defaults to false- ->
        <attribute name="StrictTck">true</attribute>
        <!-- Should acknowledgements be sent asynchronously? -->
        <attribute name="SendAcksAsync">false</attribute>
        <!-- Disable JBoss Remoting Connector sanity checks - There is rarely a good reason to set this to true -->
        <attribute name="DisableRemotingChecks">false</attribute>
        <!-- The connection factory will be bound in the following places in JNDI -->

        <attribute name="JNDIBindings">
           <bindings>
              <binding>/acme/MyExampleConnectionFactory</binding>
              <binding>/acme/MyExampleConnectionFactoryDupe</binding>
              <binding>java:/xyz/CF1</binding>
              <binding>java:/connectionfactories/acme/connection_factory</binding>
           </bindings>
        </attribute>   
    </mbean>

 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name = "mbean")
@XmlAccessorType(XmlAccessType.NONE)
public class ConnectionFactoryBean extends MBeanJaxbBase<ConnectionFactoryBean> {

    @XmlPath("constructor/arg[1]/@value")
       private String clientID;  // for connections created using this factory

    @XmlPath("depends[@optional-attribute-name='ServerPeer')]/text()") private String ServerPeer;

    // The transport to use - can be bisocket, sslbisocket or http
    @XmlPath("depends[@optional-attribute-name='Connector')]/text()") private String Connector;
    
    // <depends>jboss.messaging:service=PostOffice</depends>
    @XmlPath("/depends[contains(text(), 'jboss.messaging:service')]/text()") private String postOfficeService;
    

    // PrefetchSize determines the approximate maximum number of messages the client consumer will buffer locally
    @XmlPath("attribute[@name='PrefetchSize']/text()") private String PrefetchSize; // 150

    // Paging params to be used for temporary queues
    @XmlPath("attribute[@name='DefaultTempQueueFullSize']/text()") private String DefaultTempQueueFullSize; // 200000
    @XmlPath("attribute[@name='DefaultTempQueuePageSizeSize']/text()") private String DefaultTempQueuePageSizeSize; // 2000
    @XmlPath("attribute[@name='DefaultTempQueueDownCacheSize']/text()") private String DefaultTempQueueDownCacheSize; // 2000
    // The batch size to use when using the DUPS_OK_ACKNOWLEDGE acknowledgement mode
    @XmlPath("attribute[@name='DupsOKBatchSize']/text()") private String DupsOKBatchSize; // 5000
    // Does this connection factory support automatic failover?
    @XmlPath("attribute[@name='SupportsFailover']/text()") private String SupportsFailover; // false
    // Does this connection factory support automatic client side load balancing?
    @XmlPath("attribute[@name='SupportsLoadBalancing']/text()") private String SupportsLoadBalancing; // false  
    // The class name of the factory used to create the load balancing policy to use on the client side
    @XmlPath("attribute[@name='LoadBalancingFactory']/text()") private String LoadBalancingFactory; // org.jboss.jms.client.plugin.RoundRobinLoadBalancingFactory  
    // Whether we should be strict TCK compliant, i.e. how we deal with foreign messages, defaults to false- ->
    @XmlPath("attribute[@name='StrictTck']/text()") private String StrictTck; // true
    // Should acknowledgements be sent asynchronously?
    @XmlPath("attribute[@name='SendAcksAsync']/text()") private String SendAcksAsync; // false
    // Disable JBoss Remoting Connector sanity checks - There is rarely a good reason to set this to true
    @XmlPath("attribute[@name='DisableRemotingChecks']/text()") private String DisableRemotingChecks; // false
    // The connection factory will be bound in the following places in JNDI

    @XmlPath("attribute[@name='JNDIBindings']/bindings/binding")
    private List<String> jndiBindings;

    
    public String getClientID() { return clientID; }
    public String getServerPeer() { return ServerPeer; }
    public String getConnector() { return Connector; }
    public String getPostOfficeService() { return postOfficeService; }
    public String getPrefetchSize() { return PrefetchSize; }
    public String getDefaultTempQueueFullSize() { return DefaultTempQueueFullSize; }
    public String getDefaultTempQueuePageSizeSize() { return DefaultTempQueuePageSizeSize; }
    public String getDefaultTempQueueDownCacheSize() { return DefaultTempQueueDownCacheSize; }
    public String getDupsOKBatchSize() { return DupsOKBatchSize; }
    public String getSupportsFailover() { return SupportsFailover; }
    public String getSupportsLoadBalancing() { return SupportsLoadBalancing; }
    public String getLoadBalancingFactory() { return LoadBalancingFactory; }
    public String getStrictTck() { return StrictTck; }
    public String getSendAcksAsync() { return SendAcksAsync; }
    public String getDisableRemotingChecks() { return DisableRemotingChecks; }
    public List<String> getJndiBindings() { return jndiBindings; }

}// class
