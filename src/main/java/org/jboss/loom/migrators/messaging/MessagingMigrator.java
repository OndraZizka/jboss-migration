package org.jboss.loom.migrators.messaging;

import org.jboss.loom.migrators.messaging.jaxb.ServerPeerBean;
import org.jboss.loom.migrators.messaging.jaxb.PersistenceServiceBean;
import java.io.File;
import java.util.List;
import org.jboss.loom.actions.AbstractStatefulAction;
import org.jboss.loom.actions.ManualAction;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ctx.MigratorData;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.migrators.messaging.jaxb.ConnectionFactoryBean;
import org.jboss.loom.migrators.messaging.jaxb.DestinationBean;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.utils.Utils;
import org.jboss.loom.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
    $JBOSS_HOME/server/$PROFILE/deploy/messaging/messaging-service.xml
    $JBOSS_HOME/server/$PROFILE/deploy/messaging/remoting-bisocket-service.xml
    $JBOSS_HOME/server/$PROFILE/deploy/messaging/<your database type>-persistence-service.xml
    $JBOSS_HOME/server/$PROFILE/deploy/messaging/connection-factories-service.xml
    $JBOSS_HOME/server/$PROFILE/deploy/messaging/destinations-service.xml

 * 
 * @Jira: MIGR-44
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class MessagingMigrator extends AbstractMigrator implements IMigrator {
    private static final Logger log = LoggerFactory.getLogger(MessagingMigrator.class);

    @Override protected String getConfigPropertyModuleName() { return "messaging"; }


    public MessagingMigrator( GlobalConfiguration globalConfig ) {
        super( globalConfig );
    }

    @Override
    public void loadSourceServerConfig( MigrationContext ctx ) throws MigrationException {
        
        File mesgDir = new File(this.getGlobalConfig().getAS5Config().getDeployDir(), "messaging");
        if( ! mesgDir.exists() ){
            log.warn("No JBoss Messaging config directory in EAP was found. Skipping.");
            return;
        }
            
        
        // ServerPeer
        File confFile = Utils.createPath( mesgDir, "messaging-service.xml");
        ServerPeerBean serverPeerBean = XmlUtils.readXmlConfigFile( false, confFile, "/server/mbean[@code='org.jboss.jms.server.ServerPeer']", ServerPeerBean.class, "Messaging ServerPeer config");
        
        // PersistenceService
        List<PersistenceServiceBean> persServBeans = XmlUtils.readXmlConfigFiles( mesgDir, "*-persistence-service.xml", 
                "/server/mbean[@code='org.jboss.messaging.core.jmx.JDBCPersistenceManagerService']", PersistenceServiceBean.class, "Messaging PersistenceService config");
        
        // ConnectionFactory-es
        List<ConnectionFactoryBean> connFactBeans = XmlUtils.readXmlConfigFileMulti( false, new File( mesgDir, "connection-factories-service.xml"), 
                "/server/mbean[@code='org.jboss.jms.server.connectionfactory.ConnectionFactory']", ConnectionFactoryBean.class, "Messaging connection factories config");
        
        // Destinations
        List<DestinationBean> destBeans = XmlUtils.readXmlConfigFileMulti( false, new File( mesgDir, "destinations-service.xml"), 
                "/server/mbean[@code='org.jboss.jms.server.destination.QueueService']", DestinationBean.class, "Messaging destinations config");
        
        // Store to context
        ctx.getMigrationData().put( this.getClass(), new Data( serverPeerBean, persServBeans, connFactBeans, destBeans ));
    }

    
    /**
     * Actions.
     */
    @Override
    public void createActions( MigrationContext ctx ) throws MigrationException {
        Data data = (Data) ctx.getMigrationData().get(this.getClass());
        if( null == data )
            return;
        
        // ManualAction.
        AbstractStatefulAction warnAction = new ManualAction()
                .addWarning("JBoss Messaging config migration is not yet supported.");
        ctx.getActions().add( warnAction );
        
        warnAction.addWarning("    Skipping ServerPeer MBean config migration.");
        
        if( null != data.destBeans )
        for( DestinationBean dest : data.destBeans) {
            warnAction.addWarning("    Skipping destination config migration: " + dest.getMbeanName() );
        }
        
        if( null != data.connFactBeans )
        for( ConnectionFactoryBean cf : data.connFactBeans ) {
            warnAction.addWarning("    Skipping connection factory config migration: " + cf.getMbeanName() );
        }
        
        if( null != data.persServBeans )
        for( PersistenceServiceBean ps : data.persServBeans ) {
            warnAction.addWarning("    Skipping PersistenceService config migration: " + ps.getMbeanName() );
        }
    }
    
    
    
    /**
     * Custom MigrationData.
     */
    private static class Data extends MigratorData {
        
        private ServerPeerBean serverPeerBean;
        private List<PersistenceServiceBean> persServBeans;
        private List<ConnectionFactoryBean> connFactBeans;
        private List<DestinationBean> destBeans;

        public Data( ServerPeerBean serverPeerBean, List<PersistenceServiceBean> persServBeans, List<ConnectionFactoryBean> connFactBeans, List<DestinationBean> destBeans ) {
            this.serverPeerBean = serverPeerBean;
            this.persServBeans = persServBeans;
            this.connFactBeans = connFactBeans;
            this.destBeans = destBeans;
        }
        
    }
    
}// class
