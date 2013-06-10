package org.jboss.loom.migrators.remoting;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
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
  JBM:   $JBOSS_HOME/server/$PROFILE/deploy/messaging/remoting-bisocket-service.xml 
  EJB2:  $JBOSS_HOME/server/$PROFILE/deploy/remoting-jboss-beans.xml
  EJB3:  $JBOSS_HOME/server/$PROFILE/deploy/ejb3-connectors-jboss-beans.xml
  <mbean code="org.jboss.remoting.transport.Connector" .../>
 
 * @Jira: MIGR-45
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@ConfigPartDescriptor(
    name = "JBoss Remoting configuration",
    docLink = "https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/5/html-single/Administration_And_Configuration_Guide/index.html#remoting"
)
public class RemotingMigrator extends AbstractMigrator implements IMigrator {

    @Override protected String getConfigPropertyModuleName() { return "remoting"; }


    public RemotingMigrator( GlobalConfiguration globalConfig ) {
        super( globalConfig );
    }

    @Override
    public void loadSourceServerConfig( MigrationContext ctx ) throws MigrationException {
        
        RemotingConfigBean megs = null;
        RemotingConfigPojoBean ejb2 = null;
        RemotingConfigPojoBean ejb3 = null;
        
        // Messaging - MBean.
        File confFile = Utils.createPath( this.getGlobalConfig().getAS5Config().getDeployDir(), "messaging/remoting-bisocket-service.xml");
        if( confFile.exists() )
            megs = XmlUtils.readXmlConfigFile( confFile, "/server/mbean[@code='org.jboss.remoting.transport.Connector']", RemotingConfigBean.class, "Messaging remoting");

        // EJB2 - POJO.  xmlns="urn:jboss:bean-deployer:2.0"
        confFile = Utils.createPath( this.getGlobalConfig().getAS5Config().getDeployDir(), "remoting-jboss-beans.xml");
        if( confFile.exists() )
            ejb2 = XmlUtils.readXmlConfigFile( confFile, "/deployment/bean[@class='org.jboss.remoting.ServerConfiguration']", RemotingConfigPojoBean.class, "EJB2 remoting");

        // EJB3 - POJO.
        confFile = Utils.createPath( this.getGlobalConfig().getAS5Config().getDeployDir(), "ejb3-connectors-jboss-beans.xml");
        if( confFile.exists() )
            ejb3 = XmlUtils.readXmlConfigFile( confFile, "/deployment/bean[@class='org.jboss.remoting.ServerConfiguration']", RemotingConfigPojoBean.class, "EJB3 remoting");

        // Store to context
        ctx.getMigrationData().put( this.getClass(), new Data(megs, ejb2, ejb3) );
        
    }// loadSourceServerConfig()
    
        
    /**
     * Actions.
     */
    @Override
    public void createActions( MigrationContext ctx ) throws MigrationException {
        Data data = (Data) ctx.getMigrationData().get(this.getClass());
        if( data.isEmpty() )
            return;
        
        // ManualAction.
        AbstractStatefulAction warnAction = new ManualAction().addWarning("Remoting config migration is not yet supported.");
        ctx.getActions().add( warnAction );

        // TODO
        for( IConfigFragment fra : data.getConfigFragments() ) {
            if( ! (fra instanceof RemotingConfigBean) )  continue;
            RemotingConfigBean bean = (RemotingConfigBean) fra;
            warnAction.addWarning("  Skipping remoting config " + bean.getMbeanName() + " from "  + bean.getOrigin().getFile() );
        }
    }
    
    
    
    public static class Data extends MigratorData {
        RemotingConfigBean mesgConf;
        RemotingConfigPojoBean ejb2Conf;
        RemotingConfigPojoBean ejb3Conf;

        public Data( RemotingConfigBean mesgConf, RemotingConfigPojoBean ejb2Conf, RemotingConfigPojoBean ejb3Conf ) {
            this.mesgConf = mesgConf;
            this.ejb2Conf = ejb2Conf;
            this.ejb3Conf = ejb3Conf;
        }
        
        public boolean isEmpty(){
            return mesgConf == null && ejb2Conf == null && ejb3Conf == null;
        };


        @Override
        public List<RemotingConfigBean> getConfigFragments() {
            if( isEmpty() ) 
                return Collections.EMPTY_LIST;
            return Arrays.asList( this.mesgConf, this.ejb2Conf, this.ejb3Conf);
        }
        
    }
        
}// class
