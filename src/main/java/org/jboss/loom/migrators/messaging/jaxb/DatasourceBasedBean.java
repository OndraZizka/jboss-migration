package org.jboss.loom.migrators.messaging.jaxb;

import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.jboss.loom.migrators.MBeanJaxbBase;
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.spi.IConfigFragment;

/**
 *
    $JBOSS_HOME/server/$PROFILE/deploy/messaging/*-persistence-service.xml

    <mbean code="org.jboss.messaging.core.jmx.JDBCPersistenceManagerService" name="jboss.messaging:service=PersistenceManager" xmbean-dd="xmdesc/JDBCPersistenceManager-xmbean.xml">

       <depends>jboss.jca:service=DataSourceBinding,name=DefaultDS</depends>
       <depends optional-attribute-name="TransactionManager">jboss:service=TransactionManager</depends>
       <attribute name="DataSource">java:/DefaultDS</attribute>
       <attribute name="CreateTablesOnStartup">true</attribute>
       <attribute name="UsingBatchUpdates">false</attribute>
       <!-- The maximum number of parameters to include in a prepared statement -->
       <attribute name="MaxParams">500</attribute>
    </mbean>


    <mbean code="org.jboss.messaging.core.jmx.MessagingPostOfficeService" name="jboss.messaging:service=PostOffice" xmbean-dd="xmdesc/MessagingPostOffice-xmbean.xml">
       <depends>jboss.jca:service=DataSourceBinding,name=DefaultDS</depends>
       <depends optional-attribute-name="TransactionManager">jboss:service=TransactionManager</depends>
       <attribute name="DataSource">java:/DefaultDS</attribute>
       <attribute name="CreateTablesOnStartup">true</attribute>
    </mbean>

    <mbean code="org.jboss.jms.server.plugin.JDBCJMSUserManagerService"
       <depends>jboss.jca:service=DataSourceBinding,name=DefaultDS</depends>
       <depends optional-attribute-name="TransactionManager">jboss:service=TransactionManager</depends>
       <attribute name="DataSource">java:/DefaultDS</attribute>
       <attribute name="CreateTablesOnStartup">true</attribute>
    </mbean>
            
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
//@XmlRootElement(name = "mbean")
//@XmlAccessorType(XmlAccessType.NONE)
public class DatasourceBasedBean<T extends MBeanJaxbBase> extends MBeanJaxbBase<T> implements IConfigFragment, Origin.Wise {

    public DatasourceBasedBean() {
    }
    
    @XmlPath("depends[contains(text(), 'DataSourceBinding')]/text()")
    private String datasourceMBean;
    
    @XmlPath("attribute[@name='DataSource']/text()")
    private String datasourceJndi;
    
    @XmlPath("attribute[@name='CreateTablesOnStartup']/text()")
    private boolean CreateTablesOnStartup;
    
    @XmlPath("depends[@optional-attribute-name='TransactionManager')]/text()")
    private String transactionManager;


    public String getDatasourceMBean() { return datasourceMBean; }
    public String getDatasourceJndi() { return datasourceJndi; }
    public boolean isCreateTablesOnStartup() { return CreateTablesOnStartup; }
    public String getTransactionManager() { return transactionManager; }
    
}// class
