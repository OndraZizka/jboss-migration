package org.jboss.loom.migrators.mail;

import java.io.File;
import java.util.List;
import org.jboss.loom.actions.AbstractStatefulAction;
import org.jboss.loom.actions.ManualAction;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ctx.MigratorData;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;
import org.jboss.loom.utils.Utils;
import org.jboss.loom.utils.XmlUtils;

/**
 * 
 * $JBOSS_HOME/server/default/deploy/mail-service.xml:

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
 * @Jira: MIGR-9
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@ConfigPartDescriptor(
    name = "Mail Service configuration"
    //docTargetLink = "https://docs.jboss.org/author/display/AS72/Logging+Configuration"
)
public class MailMigrator extends AbstractMigrator implements IMigrator {

    @Override protected String getConfigPropertyModuleName() { return "mail"; }


    public MailMigrator( GlobalConfiguration globalConfig ) {
        super( globalConfig );
    }

    @Override
    public void loadSourceServerConfig( MigrationContext ctx ) throws MigrationException {
        
        File confFile = Utils.createPath( this.getGlobalConfig().getAS5Config().getDeployDir(), "mail-service.xml");
        List<MailServiceBean> beans = 
            XmlUtils.readXmlConfigFileMulti( confFile, "/server/mbean[@code='org.jboss.mail.MailService']", MailServiceBean.class, "Mail Service config");
        
        // Store to context
        ctx.getMigrationData().put( this.getClass(), new Data(beans) );
    }

    
    /**
     * Actions.
     */
    @Override
    public void createActions( MigrationContext ctx ) throws MigrationException {
        Data data = (Data) ctx.getMigrationData().get(this.getClass());
        if( data.getConfigFragments().isEmpty() )
            return;
        
        // ManualAction.
        AbstractStatefulAction warnAction = new ManualAction().addWarning("MailService beans migration is not yet supported.");
        for( IConfigFragment fra : data.getConfigFragments() ) {
            if( ! (fra instanceof MailServiceBean) )  continue;
            MailServiceBean ms = (MailServiceBean) fra;
            if( "smtp.nosuchhost.nosuchdomain.com".equals(ms.getSmtpHost()) ){
                warnAction.addWarning("  MailService is just an example, will be skipped: " + ms.getJndiName());
                continue; // Example config in EAP.
            }
            warnAction.addWarning("  MailService will be skipped - JNDI name: " + ms.getJndiName() + ", MBean name: " + ms.getMbeanName());
        }
        ctx.getActions().add( warnAction );
    }
    
    
    
    /**
     * Custom MigrationData.
     */
    public static class Data extends MigratorData {
        MailServiceBean defaultMailService = null;
        
        private Data( List<MailServiceBean> beans ) {
            super( beans );
        }
    }
    
}// class
