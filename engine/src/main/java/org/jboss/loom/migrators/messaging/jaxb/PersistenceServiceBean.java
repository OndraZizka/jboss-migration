package org.jboss.loom.migrators.messaging.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.spi.IConfigFragment;

/**
 *
    $JBOSS_HOME/server/$PROFILE/deploy/messaging/*-persistence-service.xml

    <mbean code="org.jboss.messaging.core.jmx.JDBCPersistenceManagerService" name="jboss.messaging:service=PersistenceManager" xmbean-dd="xmdesc/JDBCPersistenceManager-xmbean.xml">

       <depends>jboss.jca:service=DataSourceBinding,name=DefaultDS</depends>
       <depends optional-attribute-name="TransactionManager">jboss:service=TransactionManager</depends>

       <!-- The datasource to use for the persistence manager -->
       <attribute name="DataSource">java:/DefaultDS</attribute>

       <!-- If true will attempt to create tables and indexes on every start-up -->
       <attribute name="CreateTablesOnStartup">true</attribute>

       <!-- If true then will use JDBC batch updates -->
       <attribute name="UsingBatchUpdates">false</attribute>

       <!-- The maximum number of parameters to include in a prepared statement -->
       <attribute name="MaxParams">500</attribute>
    </mbean>
            
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name = "mbean")
@XmlAccessorType(XmlAccessType.NONE)
public final class PersistenceServiceBean extends DatasourceBasedBean<PersistenceServiceBean> implements IConfigFragment, Origin.Wise {

    public PersistenceServiceBean() {
    }

  
}// class
