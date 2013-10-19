package org.jboss.loom.migrators.mail;

import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.jboss.loom.migrators.MBeanJaxbBase;
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;

/**
    <?xml version="1.0" encoding="UTF-8"?>  
    <server>  
        <mbean code="org.jboss.mail.MailService" name="jboss:service=Mail">  
            <attribute name="JNDIName">java:/Mail</attribute>  
            <attribute name="User">user</attribute>  
            <attribute name="Password">password</attribute>  
            <attribute name="Configuration">  
                <configuration>  
                    <property name="mail.store.protocol" value="pop3"/>  
                    <property name="mail.transport.protocol" value="smtps"/>  
                    <property name="mail.smtps.starttls.enable" value="true"/>  
                    <property name="mail.smtps.auth" value="true"/>    
                    <property name="mail.user" value="user"/>  
                    <property name="mail.pop3.host" value="pop3.gmail.com"/>  
                    <property name="mail.smtps.host" value="smtp.gmail.com"/>  
                    <property name="mail.smtps.port" value="465"/>  
                    <property name="mail.from" value="user@gmail.com"/>  
                    <property name="mail.debug" value="true"/>  
                </configuration>  
            </attribute>  
            <depends>jboss:service=Naming</depends>  
        </mbean>  
    </server>
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@ConfigPartDescriptor(
    name = "Mail Service ${jndiName}"
)
@XmlRootElement(name = "mbean")
@XmlAccessorType(XmlAccessType.NONE)
public final class MailServiceBean extends MBeanJaxbBase<MailServiceBean> implements IConfigFragment, Origin.Wise {


    public MailServiceBean() {
    }
    
    @XmlPath("attribute[@name='JNDIName']/text()")
    @XmlElement(name = "attribute")
    private String jndiName;
    
    @XmlPath("attribute[@name='User']")
    private String userAttr;
    @XmlPath("attribute[@name='Password']")
    private String pass;

    @XmlPath("attribute[@name='Configuration']/configuration/property[@name='mail.store.protocol']/@value")
    private String storeProtocol;       //pop3
    @XmlPath("attribute[@name='Configuration']/configuration/property[@name='mail.pop3.host']/@value")
    private String pop3Host;            // pop3.gmail.com
    @XmlPath("attribute[@name='Configuration']/configuration/property[@name='mail.user']/@value")
    private String user;               // user

    @XmlPath("attribute[@name='Configuration']/configuration/property[@name='mail.transport.protocol']/@value")
    private String transportProtocol;  // smtp
    @XmlPath("attribute[@name='Configuration']/configuration/property[@name='mail.smtp.host']/@value")
    private String smtpHost;           // smtp.gmail.com
    @XmlPath("attribute[@name='Configuration']/configuration/property[@name='mail.smtp.port']/@value")
    private String smtpPort;           // 465
    @XmlPath("attribute[@name='Configuration']/configuration/property[@name='mail.smtp.auth']/@value")
    private String smtpAuth;           // true | false
    @XmlPath("attribute[@name='Configuration']/configuration/property[@name='mail.smtp.password']/@value")
    private String smtpPassword;       // pass
    @XmlPath("attribute[@name='Configuration']/configuration/property[@name='mail.smtp.user']/@value")
    private String smtpUser;           // user
    @XmlPath("attribute[@name='Configuration']/configuration/property[@name='mail.smtp.starttls.enable']/@value")
    private String smtpStarttlsEnable; // true | false
    @XmlPath("attribute[@name='Configuration']/configuration/property[@name='mail.smtp.ssl.enable']/@value")
    private String smtpSslEnable;      // true | false
    @XmlPath("attribute[@name='Configuration']/configuration/property[@name='mail.from']/@value")
    private String from;                // user@gmail.com
    
    @XmlPath("attribute[@name='Configuration']/configuration/property[@name='mail.debug']/@value")
    private String debug;               // true
    
    
    /**
     * Wrapper for the List.
     */
    @XmlRootElement(name = "server")
    public static final class Wrap {
        
        @XmlElement(name = "mbean")
        List<MailServiceBean> mailBeans = Collections.EMPTY_LIST;
        
    }

    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getJndiName() { return jndiName; }
    public void setJndiName( String jndiName ) { this.jndiName = jndiName; }
    public String getUser() { return user; }
    public void setUser( String user ) { this.user = user; }
    public String getPass() { return pass; }
    public void setPass( String pass ) { this.pass = pass; }
    public String getStoreProtocol() { return storeProtocol; }
    public void setStoreProtocol( String storeProtocol ) { this.storeProtocol = storeProtocol; }
    public String getPop3Host() { return pop3Host; }
    public void setPop3Host( String pop3Host ) { this.pop3Host = pop3Host; }
    public String getTransportProtocol() { return transportProtocol; }
    public void setTransportProtocol( String transportProtocol ) { this.transportProtocol = transportProtocol; }
    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost( String smtpsHost ) { this.smtpHost = smtpsHost; }
    public String getSmtpPort() { return smtpPort; }
    public void setSmtpPort( String smtpsPort ) { this.smtpPort = smtpsPort; }
    public String getSmtpAuth() { return smtpAuth; }
    public void setSmtpAuth( String smtpsAuth ) { this.smtpAuth = smtpsAuth; }
    public String getSmtpUser() { return smtpUser; }
    public void setSmtpUser( String smtpsUser ) { this.smtpUser = smtpsUser; }
    public String getSmtpStarttlsEnable() { return smtpStarttlsEnable; }
    public void setSmtpStarttlsEnable( String smtpsStarttlsEnable ) { this.smtpStarttlsEnable = smtpsStarttlsEnable; }
    public String getFrom() { return from; }
    public void setFrom( String from ) { this.from = from; }
    public String getDebug() { return debug; }
    public void setDebug( String debug ) { this.debug = debug; }
    //</editor-fold>
    
}// class
