package org.jboss.loom.migrators.remoting;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.jboss.loom.migrators.MBeanJaxbBase;
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.spi.IConfigFragment;

/**
 * $JBOSS_HOME/server/$profile/deploy/messaging/remoting-bisocket-service.xml
 * $JBOSS_HOME/server/$profile/deploy/remoting-jboss-beans.xml
   
    <mbean code="org.jboss.remoting.transport.Connector"
            name="jboss.messaging:service=Connector,transport=bisocket"
            display-name="Bisocket Transport Connector">
      <attribute name="Configuration">
        <config>
          <invoker transport="bisocket">         
            <attribute name="marshaller" isParam="true">org.jboss.jms.wireformat.JMSWireFormat</attribute>
            <attribute name="unmarshaller" isParam="true">org.jboss.jms.wireformat.JMSWireFormat</attribute>            
            <attribute name="serverBindAddress">${jboss.bind.address}</attribute>
            <attribute name="serverBindPort">4457</attribute>
            <attribute name="callbackTimeout">10000</attribute>
                 ...     
          </invoker>
              ...
        </config>
      </attribute>
    </mbean>
* 
 * @Jira: MIGR-45
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name = "mbean")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "mbean")
public final class RemotingConfigBean extends MBeanJaxbBase<RemotingConfigBean> implements IConfigFragment, Origin.Wise {
    
    @XmlPath("attribute[@name='Configuration']/config/invoker[@transport='bisocket']/attribute[@name='marshaller']/text()")
    private String marshaller;        // org.jboss.jms.wireformat.JMSWireFormat
    
    @XmlPath("attribute[@name='Configuration']/config/invoker[@transport='bisocket']/attribute[@name='unmarshaller']/text()")
    private String unmarshaller;      // org.jboss.jms.wireformat.JMSWireFormat
    
    @XmlPath("attribute[@name='Configuration']/config/invoker[@transport='bisocket']/attribute[@name='serverBindAddress']/text()")
    private String serverBindAddress; // ${jboss.bind.address}
    
    @XmlPath("attribute[@name='Configuration']/config/invoker[@transport='bisocket']/attribute[@name='serverBindPort']/text()")
    private String serverBindPort;    // 4457

    private String protocol; // socket, sslsocket, bisocket, sslbisocket, http, https, rmi, sslrmi, servlet, sslservlet
    
    @XmlPath("attribute[@name='Configuration']/config/invoker[@transport='bisocket']/attribute[@name='callbackTimeout']/text()")
    private String callbackTimeout;   // 10000

    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getMarshaller() { return marshaller; }
    public void setMarshaller( String marshaller ) { this.marshaller = marshaller; }
    public String getUnmarshaller() { return unmarshaller; }
    public void setUnmarshaller( String unmarshaller ) { this.unmarshaller = unmarshaller; }
    
    public String getServerBindAddress() { return serverBindAddress; }
    public void setServerBindAddress( String serverBindAddress ) { this.serverBindAddress = serverBindAddress; }
    public String getServerBindPort() { return serverBindPort; }
    public void setServerBindPort( String serverBindPort ) { this.serverBindPort = serverBindPort; }
    
    public String getProtocol() { return protocol; }
    public void setProtocol( String protocol ) { this.protocol = protocol; }

    
    public String getCallbackTimeout() { return callbackTimeout; }
    public void setCallbackTimeout( String callbackTimeout ) { this.callbackTimeout = callbackTimeout; }
    //</editor-fold>

}// class
