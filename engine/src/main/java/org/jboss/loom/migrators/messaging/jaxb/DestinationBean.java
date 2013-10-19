package org.jboss.loom.migrators.messaging.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.jboss.loom.migrators.MBeanJaxbBase;

/**
 *
   <mbean code="org.jboss.jms.server.destination.QueueService" name="jboss.messaging.destination:service=Queue,name=DLQ" xmbean-dd="xmdesc/Queue-xmbean.xml">
      <depends optional-attribute-name="ServerPeer">jboss.messaging:service=ServerPeer</depends>
      <depends>jboss.messaging:service=PostOffice</depends>
   </mbean>
   
   <mbean code="org.jboss.jms.server.destination.QueueService" name="jboss.messaging.destination:service=Queue,name=ExpiryQueue" xmbean-dd="xmdesc/Queue-xmbean.xml">
      <depends optional-attribute-name="ServerPeer">jboss.messaging:service=ServerPeer</depends>
      <depends>jboss.messaging:service=PostOffice</depends>
   </mbean>   

 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name = "mbean")
@XmlAccessorType(XmlAccessType.NONE)
public class DestinationBean extends MBeanJaxbBase<DestinationBean> {

    @XmlPath("depends[@optional-attribute-name='ServerPeer'/text()") private String ServerPeer;
    @XmlPath("depends[not(@*)]/text()") private String PostOffice;

    public String getServerPeer() { return ServerPeer; }
    public String getPostOffice() { return PostOffice; }

}// class
