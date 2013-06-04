package org.jboss.loom.migrators.messaging.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.jboss.loom.migrators.MBeanJaxbBase;
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.spi.IConfigFragment;

/**
 *
    $JBOSS_HOME/server/$PROFILE/deploy/messaging/messaging-service.xml

    <mbean code="org.jboss.jms.server.ServerPeer" name="jboss.messaging:service=ServerPeer" xmbean-dd="xmdesc/ServerPeer-xmbean.xml">

        <!-- The unique id of the server peer - in a cluster each node MUST have a unique value - must be an integer -->
        <attribute name="ServerPeerID">${jboss.messaging.ServerPeerID:0}</attribute>

        <!-- The default JNDI context to use for queues when they are deployed without specifying one --> 
        <attribute name="DefaultQueueJNDIContext">/queue</attribute>

        <!-- The default JNDI context to use for topics when they are deployed without specifying one --> 
        <attribute name="DefaultTopicJNDIContext">/topic</attribute>

        <attribute name="PostOffice">jboss.messaging:service=PostOffice</attribute>

        <!-- The default Dead Letter Queue (DLQ) to use for destinations.
             This can be overridden on a per destinatin basis -->
        <attribute name="DefaultDLQ">jboss.messaging.destination:service=Queue,name=DLQ</attribute>

        <!-- The default maximum number of times to attempt delivery of a message before sending to the DLQ (if configured).
             This can be overridden on a per destinatin basis -->
        <attribute name="DefaultMaxDeliveryAttempts">10</attribute>

        <!-- The default Expiry Queue to use for destinations. This can be overridden on a per destinatin basis -->
        <attribute name="DefaultExpiryQueue">jboss.messaging.destination:service=Queue,name=ExpiryQueue</attribute>

        <!-- The default redelivery delay to impose. This can be overridden on a per destination basis -->
        <attribute name="DefaultRedeliveryDelay">0</attribute>

        <!-- The periodicity of the message counter manager enquiring on queues for statistics -->
        <attribute name="MessageCounterSamplePeriod">5000</attribute>

        <!-- The maximum amount of time for a client to wait for failover to start on the server side after
             it has detected failure -->
        <attribute name="FailoverStartTimeout">60000</attribute>

        <!-- The maximum amount of time for a client to wait for failover to complete on the server side after
             it has detected failure -->
        <attribute name="FailoverCompleteTimeout">300000</attribute>
        <attribute name="StrictTck">false</attribute>

        <!-- The maximum number of days results to maintain in the message counter history -->
        <attribute name="DefaultMessageCounterHistoryDayLimit">-1</attribute>

        <!-- The name of the connection factory to use for creating connections between nodes to pull messages -->
        <attribute name="ClusterPullConnectionFactoryName">jboss.messaging.connectionfactory:service=ClusterPullConnectionFactory</attribute>

        <!-- When redistributing messages in the cluster. Do we need to preserve the order of messages received
              by a particular consumer from a particular producer? -->
        <attribute name="DefaultPreserveOrdering">false</attribute>

        <!-- Max. time to hold previously delivered messages back waiting for clients to reconnect after failover -->
        <attribute name="RecoverDeliveriesTimeout">300000</attribute>

        <!-- Set to true to enable message counters that can be viewed via JMX -->
        <attribute name="EnableMessageCounters">false</attribute>

        <!-- The password used by the message sucker connections to create connections.
             THIS SHOULD ALWAYS BE CHANGED AT INSTALL TIME TO SECURE SYSTEM
        <attribute name="SuckerPassword"></attribute>
        -->

        <!-- The name of the server aspects configuration resource
        <attribute name="ServerAopConfig">aop/jboss-aop-messaging-server.xml</attribute>
        -->
        <!-- The name of the client aspects configuration resource
          <attribute name="ClientAopConfig">aop/jboss-aop-messaging-client.xml</attribute>
        -->

        <depends optional-attribute-name="PersistenceManager">jboss.messaging:service=PersistenceManager</depends>

        <depends optional-attribute-name="JMSUserManager">jboss.messaging:service=JMSUserManager</depends>

        <depends>jboss.messaging:service=Connector,transport=bisocket</depends>
        <depends optional-attribute-name="SecurityStore"
            proxy-type="org.jboss.jms.server.SecurityStore">jboss.messaging:service=SecurityStore</depends>
    </mbean>
 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name = "mbean")
@XmlAccessorType(XmlAccessType.NONE)
public final class ServerPeerBean extends MBeanJaxbBase<ServerPeerBean> implements IConfigFragment, Origin.Wise {

    public ServerPeerBean() {
    }

    @XmlPath("attribute[@name='ServerPeerID']/text()") private String ServerPeerID; // ${jboss.messaging.ServerPeerID:0}
    @XmlPath("attribute[@name='DefaultQueueJNDIContext']/text()") private String DefaultQueueJNDIContext; // /queue
    @XmlPath("attribute[@name='DefaultTopicJNDIContext']/text()") private String DefaultTopicJNDIContext; // /topic
    @XmlPath("attribute[@name='PostOffice']/text()") private String PostOffice; // jboss.messaging:service=PostOffice
    @XmlPath("attribute[@name='DefaultDLQ']/text()") private String DefaultDLQ; // jboss.messaging.destination:service=Queue,name=DLQ
    @XmlPath("attribute[@name='DefaultMaxDeliveryAttempts']/text()") private String DefaultMaxDeliveryAttempts; // 10
    @XmlPath("attribute[@name='DefaultExpiryQueue']/text()") private String DefaultExpiryQueue; // jboss.messaging.destination:service=Queue,name=ExpiryQueue
    @XmlPath("attribute[@name='DefaultRedeliveryDelay']/text()") private String DefaultRedeliveryDelay; // 0
    @XmlPath("attribute[@name='MessageCounterSamplePeriod']/text()") private String MessageCounterSamplePeriod; // 5000
    @XmlPath("attribute[@name='FailoverStartTimeout']/text()") private String FailoverStartTimeout; // 60000
    @XmlPath("attribute[@name='FailoverCompleteTimeout']/text()") private String FailoverCompleteTimeout; // 300000
    @XmlPath("attribute[@name='StrictTck']/text()") private String StrictTck; // false
    @XmlPath("attribute[@name='DefaultMessageCounterHistoryDayLimit']/text()") private String DefaultMessageCounterHistoryDayLimit; // -1
    @XmlPath("attribute[@name='ClusterPullConnectionFactoryName']/text()") private String ClusterPullConnectionFactoryName; // jboss.messaging.connectionfactory:service=ClusterPullConnectionFactory
    @XmlPath("attribute[@name='DefaultPreserveOrdering']/text()") private String DefaultPreserveOrdering; // false
    @XmlPath("attribute[@name='RecoverDeliveriesTimeout']/text()") private String RecoverDeliveriesTimeout; // 300000
    @XmlPath("attribute[@name='EnableMessageCounters']/text()") private String EnableMessageCounters; // false
    @XmlPath("attribute[@name='SuckerPassword']/text()") private String SuckerPassword; // 
    @XmlPath("attribute[@name='ServerAopConfig']/text()") private String ServerAopConfig; // aop/jboss-aop-messaging-server.xml
    @XmlPath("attribute[@name='ClientAopConfig']/text()") private String ClientAopConfig; // aop/jboss-aop-messaging-client.xml

    @XmlPath("depends[@optional-attribute-name='PersistenceManager']/text()") private String PersistenceManager; // jboss.messaging:service=PersistenceManager
    @XmlPath("depends[@optional-attribute-name='JMSUserManager']/text()") private String JMSUserManager; // jboss.messaging:service=JMSUserManager
    @XmlPath("depends[@optional-attribute-name='SecurityStore']/text()") private String SecurityStore; // jboss.messaging:service=SecurityStore


    public String getServerPeerID() { return ServerPeerID; }
    public void setServerPeerID( String ServerPeerID ) { this.ServerPeerID = ServerPeerID; }
    public String getDefaultQueueJNDIContext() { return DefaultQueueJNDIContext; }
    public void setDefaultQueueJNDIContext( String DefaultQueueJNDIContext ) { this.DefaultQueueJNDIContext = DefaultQueueJNDIContext; }
    public String getDefaultTopicJNDIContext() { return DefaultTopicJNDIContext; }
    public void setDefaultTopicJNDIContext( String DefaultTopicJNDIContext ) { this.DefaultTopicJNDIContext = DefaultTopicJNDIContext; }
    public String getPostOffice() { return PostOffice; }
    public void setPostOffice( String PostOffice ) { this.PostOffice = PostOffice; }
    public String getDefaultDLQ() { return DefaultDLQ; }
    public void setDefaultDLQ( String DefaultDLQ ) { this.DefaultDLQ = DefaultDLQ; }
    public String getDefaultMaxDeliveryAttempts() { return DefaultMaxDeliveryAttempts; }
    public void setDefaultMaxDeliveryAttempts( String DefaultMaxDeliveryAttempts ) { this.DefaultMaxDeliveryAttempts = DefaultMaxDeliveryAttempts; }
    public String getDefaultExpiryQueue() { return DefaultExpiryQueue; }
    public void setDefaultExpiryQueue( String DefaultExpiryQueue ) { this.DefaultExpiryQueue = DefaultExpiryQueue; }
    public String getDefaultRedeliveryDelay() { return DefaultRedeliveryDelay; }
    public void setDefaultRedeliveryDelay( String DefaultRedeliveryDelay ) { this.DefaultRedeliveryDelay = DefaultRedeliveryDelay; }
    public String getMessageCounterSamplePeriod() { return MessageCounterSamplePeriod; }
    public void setMessageCounterSamplePeriod( String MessageCounterSamplePeriod ) { this.MessageCounterSamplePeriod = MessageCounterSamplePeriod; }
    public String getFailoverStartTimeout() { return FailoverStartTimeout; }
    public void setFailoverStartTimeout( String FailoverStartTimeout ) { this.FailoverStartTimeout = FailoverStartTimeout; }
    public String getFailoverCompleteTimeout() { return FailoverCompleteTimeout; }
    public void setFailoverCompleteTimeout( String FailoverCompleteTimeout ) { this.FailoverCompleteTimeout = FailoverCompleteTimeout; }
    public String getStrictTck() { return StrictTck; }
    public void setStrictTck( String StrictTck ) { this.StrictTck = StrictTck; }
    public String getDefaultMessageCounterHistoryDayLimit() { return DefaultMessageCounterHistoryDayLimit; }
    public void setDefaultMessageCounterHistoryDayLimit( String DefaultMessageCounterHistoryDayLimit ) { this.DefaultMessageCounterHistoryDayLimit = DefaultMessageCounterHistoryDayLimit; }
    public String getClusterPullConnectionFactoryName() { return ClusterPullConnectionFactoryName; }
    public void setClusterPullConnectionFactoryName( String ClusterPullConnectionFactoryName ) { this.ClusterPullConnectionFactoryName = ClusterPullConnectionFactoryName; }
    public String getDefaultPreserveOrdering() { return DefaultPreserveOrdering; }
    public void setDefaultPreserveOrdering( String DefaultPreserveOrdering ) { this.DefaultPreserveOrdering = DefaultPreserveOrdering; }
    public String getRecoverDeliveriesTimeout() { return RecoverDeliveriesTimeout; }
    public void setRecoverDeliveriesTimeout( String RecoverDeliveriesTimeout ) { this.RecoverDeliveriesTimeout = RecoverDeliveriesTimeout; }
    public String getEnableMessageCounters() { return EnableMessageCounters; }
    public void setEnableMessageCounters( String EnableMessageCounters ) { this.EnableMessageCounters = EnableMessageCounters; }
    public String getSuckerPassword() { return SuckerPassword; }
    public void setSuckerPassword( String SuckerPassword ) { this.SuckerPassword = SuckerPassword; }
    public String getServerAopConfig() { return ServerAopConfig; }
    public void setServerAopConfig( String ServerAopConfig ) { this.ServerAopConfig = ServerAopConfig; }
    public String getClientAopConfig() { return ClientAopConfig; }
    public void setClientAopConfig( String ClientAopConfig ) { this.ClientAopConfig = ClientAopConfig; }
    public String getPersistenceManager() { return PersistenceManager; }
    public void setPersistenceManager( String PersistenceManager ) { this.PersistenceManager = PersistenceManager; }
    public String getJMSUserManager() { return JMSUserManager; }
    public void setJMSUserManager( String JMSUserManager ) { this.JMSUserManager = JMSUserManager; }
    public String getSecurityStore() { return SecurityStore; }
    public void setSecurityStore( String SecurityStore ) { this.SecurityStore = SecurityStore; }
  
}// class
