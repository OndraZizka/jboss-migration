package org.jboss.loom.migrators.remoting;

import java.io.File;
import org.jboss.loom.actions.AbstractStatefulAction;
import org.jboss.loom.actions.ManualAction;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ctx.MigrationData;
import org.jboss.loom.ex.LoadMigrationException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.IMigrator;
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
public class RemotingMigrator extends AbstractMigrator implements IMigrator {

    @Override protected String getConfigPropertyModuleName() { return "remoting"; }


    public RemotingMigrator( GlobalConfiguration globalConfig ) {
        super( globalConfig );
    }

    @Override
    public void loadAS5Data( MigrationContext ctx ) throws LoadMigrationException {
        
        RemotingConfigBean megs;
        RemotingConfigPojoBean ejb2;
        RemotingConfigPojoBean ejb3;
        
        {
            // Messaging - MBean.
            File confFile = Utils.createPath( this.getGlobalConfig().getAS5Config().getDeployDir(), "messaging/remoting-bisocket-service.xml");
            try {
                megs = XmlUtils.unmarshallBean( confFile, "/server/mbean[@code='org.jboss.remoting.transport.Connector']", RemotingConfigBean.class);
            } catch( Exception ex ) {
                throw new LoadMigrationException("Failed loading Messaging remoting config from "+confFile.getPath()+": " + ex.getMessage(), ex);
            }
        }
        {
            // EJB2 - POJO.
            File confFile = Utils.createPath( this.getGlobalConfig().getAS5Config().getDeployDir(), "remoting-jboss-beans.xml");
            try {
                ejb2 = XmlUtils.unmarshallBean( confFile, "bean[@class='org.jboss.remoting.ServerConfiguration']", RemotingConfigPojoBean.class);
            } catch( Exception ex ) {
                throw new LoadMigrationException("Failed loading EJB2 remoting config from "+confFile.getPath()+": " + ex.getMessage(), ex);
            }
        }
        {
            // EJB3 - POJO.
            File confFile = Utils.createPath( this.getGlobalConfig().getAS5Config().getDeployDir(), "ejb3-connectors-jboss-beans.xml");
            try {
                ejb3 = XmlUtils.unmarshallBean( confFile, "bean[@class='org.jboss.remoting.ServerConfiguration']", RemotingConfigPojoBean.class);
            } catch( Exception ex ) {
                throw new LoadMigrationException("Failed loading EJB3 remoting config from "+confFile.getPath()+": " + ex.getMessage(), ex);
            }
        }
        // Store to context
        ctx.getMigrationData().put( this.getClass(), new Data(megs, ejb2, ejb3) );
        
    }// loadAS5Data()

    
    /**
     * Actions.
     */
    @Override
    public void createActions( MigrationContext ctx ) throws MigrationException {
        MigrationData data = ctx.getMigrationData().get(this.getClass());
        if( data.getConfigFragments().isEmpty() )
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
    
    
    
    public static class Data extends MigrationData {
        RemotingConfigBean mesgConf;
        RemotingConfigPojoBean ejb2Conf;
        RemotingConfigPojoBean ejb3Conf;

        public Data( RemotingConfigBean mesgConf, RemotingConfigPojoBean ejb2Conf, RemotingConfigPojoBean ejb3Conf ) {
            this.mesgConf = mesgConf;
            this.ejb2Conf = ejb2Conf;
            this.ejb3Conf = ejb3Conf;
        }
    }
        
}// class
