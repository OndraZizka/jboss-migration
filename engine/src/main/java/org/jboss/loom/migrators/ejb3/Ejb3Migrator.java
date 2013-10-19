package org.jboss.loom.migrators.ejb3;


import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.collections.list.UnmodifiableList;
import org.jboss.loom.actions.AbstractStatefulAction;
import org.jboss.loom.actions.ManualAction;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ctx.MigratorData;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.migrators.remoting.RemotingConfigBean;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;
import org.jboss.loom.utils.Utils;
import org.jboss.loom.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  $JBOSS_HOME/server/$PROFILE/conf/standardjboss.xml
 *  $JBOSS_HOME/server/$PROFILE/deploy/ejb-deployer.xml
 * 
 *  Docs: https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/5/html-single/Administration_And_Configuration_Guide/index.html#EJBs_on_JBoss
 * 
 *  @Jira MIGR-45
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@ConfigPartDescriptor(
    name = "EJB 3 configuration",
    docLink = "https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/5/html-single/Administration_And_Configuration_Guide/index.html#EJBs_on_JBoss"
)
public class Ejb3Migrator extends AbstractMigrator implements IMigrator {
    private static final Logger log = LoggerFactory.getLogger( Ejb3Migrator.class );
    
    @Override protected String getConfigPropertyModuleName() { return "ejb3"; }


    public Ejb3Migrator( GlobalConfiguration globalConfig ) {
        super( globalConfig );
    }


    @Override
    public void loadSourceServerConfig( MigrationContext ctx ) throws MigrationException 
    {
        // EJB 2 Timer - EAP 5.0.0
        Ejb2TimerConfigBean timerBean;
        File confFile = Utils.createPath( this.getGlobalConfig().getAS5Config().getDeployDir(), "ejb-deployer.xml");
        if( confFile.exists() ){
            timerBean = XmlUtils.readXmlConfigFile( confFile,
                "/server/mbean[@code='org.jboss.ejb.txtimer.EJBTimerServiceImpl']", Ejb2TimerConfigBean.class, "EJB2 Timer config");
        }
        // EJB 2 Timer - EAP 5.2.0
        else {
            confFile = Utils.createPath( this.getGlobalConfig().getAS5Config().getDeployDir(), "ejb2-timer-service.xml");
            timerBean = XmlUtils.readXmlConfigFile( confFile,
                "/server/mbean[@code='org.jboss.ejb.txtimer.EJBTimerServiceImpl']", Ejb2TimerNewConfigBean.class, "EJB2 Timer config");
        }
        
        // InvokerProxyBindingBean
        confFile = Utils.createPath( this.getGlobalConfig().getAS5Config().getConfDir(), "standardjboss.xml");
        List<InvokerProxyBindingBean> invoBeans = XmlUtils.readXmlConfigFileMulti( confFile,
                "/jboss/invoker-proxy-bindings/invoker-proxy-binding", InvokerProxyBindingBean.class, "EJB3 invoker proxy binding config");
        
        // ContainerConfigBean
        confFile = Utils.createPath( this.getGlobalConfig().getAS5Config().getConfDir(), "standardjboss.xml");
        List<ContainerConfigBean> contBeans = XmlUtils.readXmlConfigFileMulti( confFile,
                "/jboss/container-configurations/container-configuration", ContainerConfigBean.class, "EJB3 container config");

        // TODO: EJB jar's META-INF/
        
        // Store to context
        ctx.getMigrationData().put( this.getClass(), new Data( timerBean, invoBeans, contBeans) );
    }


    @Override
    public void createActions( MigrationContext ctx ) throws MigrationException {
        Data data = (Data) ctx.getMigrationData().get(this.getClass());
        if( data.isEmpty() )
            return;
        
        // ManualAction.
        AbstractStatefulAction warnAction = new ManualAction().addWarning("EJB 3 config migration is not yet supported.");
        ctx.getActions().add( warnAction );

        // TODO
        for( ContainerConfigBean bean : data.containters ) {
            warnAction.addWarning("  Skipping container config " + bean.containerName + " from "  + bean.getOrigin().getFile() );
        }
        for( InvokerProxyBindingBean bean : data.invokers ) {
            warnAction.addWarning("  Skipping invoker proxy config " + bean.getName() + " from "  + bean.getOrigin().getFile() );
        }
    }
    
    
    /**
     *  Conf data class for this migrator.
     */
    public static class Data extends MigratorData {
        
        Ejb2TimerConfigBean ejbTimer = null;
        List<InvokerProxyBindingBean> invokers = Collections.EMPTY_LIST;
        List<ContainerConfigBean> containters = Collections.EMPTY_LIST;


        public Data( Ejb2TimerConfigBean ejbTimer, List<InvokerProxyBindingBean> invokers, List<ContainerConfigBean> containters ) {
            this.ejbTimer = ejbTimer;
            this.invokers = invokers;
            this.containters = containters;
        }
        
        public boolean isEmpty(){
            return ( ejbTimer == null && invokers == null || invokers.isEmpty()) && (containters == null || invokers.isEmpty());
        };


        @Override
        public List<RemotingConfigBean> getConfigFragments() {
            if( isEmpty() ) 
                return Collections.EMPTY_LIST;
            
            List ret = new LinkedList();
            ret.add( this.ejbTimer );
            ret.addAll( this.invokers );
            ret.addAll( this.containters );
            return UnmodifiableList.decorate( ret );
        }
        
    }// class Data
    

}// class
