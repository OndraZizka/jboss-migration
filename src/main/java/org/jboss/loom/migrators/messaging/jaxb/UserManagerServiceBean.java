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


    <!-- Messaging JMS User Manager MBean config
         ======================================= -->
    <mbean code="org.jboss.jms.server.plugin.JDBCJMSUserManagerService" name="jboss.messaging:service=JMSUserManager" xmbean-dd="xmdesc/JMSUserManager-xmbean.xml">

       <depends>jboss.jca:service=DataSourceBinding,name=DefaultDS</depends>
       <depends optional-attribute-name="TransactionManager">jboss:service=TransactionManager</depends>

       <attribute name="DataSource">java:/DefaultDS</attribute>
       <attribute name="CreateTablesOnStartup">true</attribute>
       <attribute name="SqlProperties"><![CDATA[
             POPULATE.TABLES.14 = INSERT INTO JBM_ROLE (ROLE_ID, USER_ID) VALUES ('noacc','nobody')
             ...
       ]]></attribute>
    </mbean>

 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name = "mbean")
@XmlAccessorType(XmlAccessType.NONE)
public final class UserManagerServiceBean extends DatasourceBasedBean<UserManagerServiceBean> implements IConfigFragment, Origin.Wise {

    public UserManagerServiceBean() {
    }
    
    @XmlPath("attribute[@name='SqlProperties']/text()")
    private String SqlProperties;


    public String getSqlProperties() { return SqlProperties; }
  
}// class
