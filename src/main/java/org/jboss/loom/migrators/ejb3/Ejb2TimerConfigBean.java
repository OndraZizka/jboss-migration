package org.jboss.loom.migrators.ejb3;

import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.jboss.loom.migrators.MBeanJaxbBase;

/**
 *  Docs: https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/5/html-single/Administration_And_Configuration_Guide/index.html#EJBs_on_JBoss-EJB_Timer_Configuration
 * 
    <mbean code="org.jboss.ejb.txtimer.EJBTimerServiceImpl" name="jboss.ejb:service=EJBTimerService">
        <attribute name="RetryPolicy">jboss.ejb:service=EJBTimerService,retryPolicy=fixedDelay</attribute>
        <attribute name="PersistencePolicy">jboss.ejb:service=EJBTimerService,persistencePolicy=database</attribute>
        <attribute name="TimerIdGeneratorClassName">org.jboss.ejb.txtimer.BigIntegerTimerIdGenerator</attribute>
        <attribute name="TimedObjectInvokerClassName">org.jboss.ejb.txtimer.TimedObjectInvokerImpl</attribute>
    </mbean>
 * 
 *  @Jira: MIGR-116
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name = "mbean")
public class Ejb2TimerConfigBean extends MBeanJaxbBase<Ejb2TimerConfigBean> {
    
    @XmlPath("attribute[@name='TimerIdGeneratorClassName']/text()")   private String timerIdGeneratorClassName; // org.jboss.ejb.txtimer.BigIntegerTimerIdGenerator
    @XmlPath("attribute[@name='TimedObjectInvokerClassName']/text()") private String timedObjectInvokerClassName; // org.jboss.ejb.txtimer.TimedObjectInvokerImpl
    
    @XmlPath("attribute[@name='RetryPolicy']/text()")
    public String getRetryPolicy() { return retryPolicy; }
    public void setRetryPolicy( String RetryPolicy ) { this.retryPolicy = RetryPolicy; }
    private String retryPolicy; // jboss.ejb:service=EJBTimerService,retryPolicy=fixedDelay
    
    @XmlPath("attribute[@name='PersistencePolicy']/text()")           
    public String getPersistencePolicy() { return persistencePolicy; }
    public void setPersistencePolicy( String PersistencePolicy ) { this.persistencePolicy = PersistencePolicy; }
    private String persistencePolicy; // jboss.ejb:service=EJBTimerService,persistencePolicy=database
    
    public String getTimerIdGeneratorClassName() { return timerIdGeneratorClassName; }
    public void setTimerIdGeneratorClassName( String TimerIdGeneratorClassName ) { this.timerIdGeneratorClassName = TimerIdGeneratorClassName; }
    public String getTimedObjectInvokerClassName() { return timedObjectInvokerClassName; }
    public void setTimedObjectInvokerClassName( String TimedObjectInvokerClassName ) { this.timedObjectInvokerClassName = TimedObjectInvokerClassName; }
    

}// class
