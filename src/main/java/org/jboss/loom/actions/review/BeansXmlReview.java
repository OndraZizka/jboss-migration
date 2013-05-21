package org.jboss.loom.actions.review;

import java.io.File;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jboss.loom.actions.CopyFileAction;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.ex.MigrationException;

/**
 *  TODO: Check each <bean class="...">. If the class is org.jboss.*, WARN.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class BeansXmlReview extends ActionReviewBase {    
    
    @Override
    public void review( IMigrationAction action ) throws MigrationException {
        // Only accept CopyActions with -beans.xml
        if( ! ( action instanceof CopyFileAction ) )  return;
        CopyFileAction ca = (CopyFileAction) action;
        String fName = ca.getSrc().getName();
        if( ! fName.endsWith("-beans.xml")) return;
        
        // Check each <bean class="...">. If the class is org.jboss.*, WARN.
        List<Bean> beans = extractBeans( ca.getSrc() );
        for( Bean bean : beans ) {
            if( bean.cls.startsWith("org.jboss.")){
                action.getWarnings().add("A bean in " + fName + " uses org.jboss.* class: " + bean.cls + "\n"
                        + "Using MBeans is only supported as a legacy technology.\n"
                        + "Server boot may fail with ClassNotFoundException.\n"
                        + "Consult server documentation about classloading and modules.");
            }
        }
    }
    
    private List<Bean> extractBeans( File beansFile ) throws MigrationException {
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(Deployment.class).createUnmarshaller();
            Deployment depl = (Deployment) unmarshaller.unmarshal(beansFile);
            return depl.beans;
        } catch( JAXBException ex ) {
            throw new MigrationException("Failed parsing " + beansFile.getAbsolutePath() + ":\n  " + ex, ex);
        }
    }
    
}// class


/**
    <deployment xmlns="urn:jboss:bean-deployer:2.0">
        <!-- JMX notifier to trigger a resync with JDK log levels when the log4j config changes-->
        <bean name="LogBridgeNotifier" class="org.jboss.logbridge.LogNotificationListener">
            <property name="logBridgeHandler"><inject bean="LogBridgeHandler"/></property>
            <property name="MBeanServer"><inject bean="JMXKernel" property="mbeanServer"/></property>
            <property name="loggingMBeanName">jboss.system:service=Logging,type=Log4jService</property>
            <depends>jboss.system:service=Logging,type=Log4jService</depends>
        </bean>
    </deployment>
 */
@XmlRootElement(name = "deployment", namespace = "urn:jboss:bean-deployer:2.0")
class Deployment {
    @XmlElement(name = "bean", namespace = "urn:jboss:bean-deployer:2.0")
    List<Bean> beans;
}

class Bean {
    @XmlAttribute(name = "class")  String cls;
    @XmlAttribute(name = "name")   String name;
}
