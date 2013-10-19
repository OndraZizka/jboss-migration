package org.jboss.loom.migrators.messaging.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.spi.IConfigFragment;

/**
 *
    $JBOSS_HOME/server/$PROFILE/deploy/messaging/*-persistence-service.xml

    <mbean code="org.jboss.messaging.core.jmx.MessagingPostOfficeService" name="jboss.messaging:service=PostOffice" xmbean-dd="xmdesc/MessagingPostOffice-xmbean.xml">

       <depends optional-attribute-name="ServerPeer">jboss.messaging:service=ServerPeer</depends>
       <depends>jboss.jca:service=DataSourceBinding,name=DefaultDS</depends>
       <depends optional-attribute-name="TransactionManager">jboss:service=TransactionManager</depends>

       <!-- The name of the post office -->
       <attribute name="PostOfficeName">JMS post office</attribute>

       <!-- The datasource used by the post office to access it's binding information -->
       <attribute name="DataSource">java:/DefaultDS</attribute>

       <attribute name="CreateTablesOnStartup">true</attribute>

       <!-- This config was not meant to be used in production. For a clustered setup you need a shared database -->
       <attribute name="Clustered">true</attribute>

       <!-- All the remaining properties only have to be specified if the post office is clustered.
            You can safely comment them out if your post office is non clustered -->
       <!-- The JGroups group name that the post office will use -->
       <attribute name="GroupName">${jboss.messaging.groupname:MessagingPostOffice}</attribute>

       <!-- Max time to wait for state to arrive when the post office joins the cluster -->
       <attribute name="StateTimeout">5000</attribute>

       <!-- Max time to wait for a synchronous call to node members using the MessageDispatcher -->
       <attribute name="CastTimeout">50000</attribute>

       <!-- Set this to true if you want failover of connections to occur when a node is shut down -->
       <attribute name="FailoverOnNodeLeave">false</attribute>

       <depends optional-attribute-name="ChannelFactoryName">jboss.jgroups:service=ChannelFactory</depends>
       <attribute name="ControlChannelName">jbm-control</attribute>
       <attribute name="DataChannelName">jbm-data</attribute>
       <attribute name="ChannelPartitionName">${jboss.partition.name:DefaultPartition}-JMS</attribute>
    </mbean>

 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name = "mbean")
@XmlAccessorType(XmlAccessType.NONE)
public final class PostOfficeServiceBean extends DatasourceBasedBean<PostOfficeServiceBean> implements IConfigFragment, Origin.Wise {

    public PostOfficeServiceBean() {
    }

    @XmlPath("depends[@optional-attribute-name='ServerPeer']/text()") private String ServerPeer; // jboss.messaging:service=ServerPeer
    @XmlPath("attribute[@name='PostOfficeName']/text()") private String PostOfficeName; // JMS post office 
    @XmlPath("attribute[@name='Clustered']/text()") private String Clustered; // true 
    @XmlPath("attribute[@name='GroupName']/text()") private String GroupName; // ${jboss.messaging.groupname:MessagingPostOffice} 
    @XmlPath("attribute[@name='StateTimeout']/text()") private String StateTimeout; // 5000 
    @XmlPath("attribute[@name='CastTimeout']/text()") private String CastTimeout; // 50000 
    @XmlPath("attribute[@name='FailoverOnNodeLeave']/text()") private String FailoverOnNodeLeave; // false 
    @XmlPath("depends[@optional-attribute-name='ChannelFactoryName']/text()") private String ChannelFactoryName; // jboss.jgroups:service=ChannelFactory
    @XmlPath("attribute[@name='ControlChannelName']/text()") private String ControlChannelName; // jbm-control 
    @XmlPath("attribute[@name='DataChannelName']/text()") private String DataChannelName; // jbm-data 
    @XmlPath("attribute[@name='ChannelPartitionName']/text()") private String ChannelPartitionName; // ${jboss.partition.name:DefaultPartition}-JMS 


    public String getServerPeer() { return ServerPeer; }
    public void setServerPeer( String ServerPeer ) { this.ServerPeer = ServerPeer; }
    public String getPostOfficeName() { return PostOfficeName; }
    public void setPostOfficeName( String PostOfficeName ) { this.PostOfficeName = PostOfficeName; }
    public String getClustered() { return Clustered; }
    public void setClustered( String Clustered ) { this.Clustered = Clustered; }
    public String getGroupName() { return GroupName; }
    public void setGroupName( String GroupName ) { this.GroupName = GroupName; }
    public String getStateTimeout() { return StateTimeout; }
    public void setStateTimeout( String StateTimeout ) { this.StateTimeout = StateTimeout; }
    public String getCastTimeout() { return CastTimeout; }
    public void setCastTimeout( String CastTimeout ) { this.CastTimeout = CastTimeout; }
    public String getFailoverOnNodeLeave() { return FailoverOnNodeLeave; }
    public void setFailoverOnNodeLeave( String FailoverOnNodeLeave ) { this.FailoverOnNodeLeave = FailoverOnNodeLeave; }
    public String getChannelFactoryName() { return ChannelFactoryName; }
    public void setChannelFactoryName( String ChannelFactoryName ) { this.ChannelFactoryName = ChannelFactoryName; }
    public String getControlChannelName() { return ControlChannelName; }
    public void setControlChannelName( String ControlChannelName ) { this.ControlChannelName = ControlChannelName; }
    public String getDataChannelName() { return DataChannelName; }
    public void setDataChannelName( String DataChannelName ) { this.DataChannelName = DataChannelName; }
    public String getChannelPartitionName() { return ChannelPartitionName; }
    public void setChannelPartitionName( String ChannelPartitionName ) { this.ChannelPartitionName = ChannelPartitionName; }
  
}// class
