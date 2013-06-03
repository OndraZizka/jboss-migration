package org.jboss.loom.migrators.ejb3;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 *  Docs: https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/5/html-single/Administration_And_Configuration_Guide/index.html#EJBs_on_JBoss-EJB_Timer_Configuration
 * 
 *  In later EAP's, the definition of this MBean used <depends>.

    <!-- An EJB Timer Service that is Tx aware -->
    <mbean code="org.jboss.ejb.txtimer.EJBTimerServiceImpl" name="jboss.ejb:service=EJBTimerService">
        <attribute name="TimerIdGeneratorClassName">org.jboss.ejb.txtimer.UUIDTimerIdGenerator</attribute>
        <attribute name="TimedObjectInvokerClassName">org.jboss.ejb.txtimer.TimedObjectInvokerImpl</attribute>
        <depends optional-attribute-name="RetryPolicy">jboss.ejb:service=EJBTimerService,retryPolicy=fixedDelay</depends>
        <depends optional-attribute-name="PersistencePolicy">jboss.ejb:service=EJBTimerService,persistencePolicy=database</depends>
        <depends optional-attribute-name="TransactionManagerFactory" proxy-type="org.jboss.tm.TransactionManagerFactory">
            jboss:service=TransactionManager
        </depends>    
    </mbean>

 *  @Jira: MIGR-116
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Ejb2TimerNewConfigBean extends Ejb2TimerConfigBean {

    @XmlPath("attribute[@name='RetryPolicy']")
    @Override public String getRetryPolicy() { return super.getRetryPolicy(); }
    
    @XmlPath("depends[@optional-attribute-name='PersistencePolicy']/text()")
    @Override public String getPersistencePolicy() { return super.getRetryPolicy(); }
    

}// class
