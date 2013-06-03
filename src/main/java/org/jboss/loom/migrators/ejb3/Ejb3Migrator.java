package org.jboss.loom.migrators.ejb3;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.list.UnmodifiableList;
import org.jboss.loom.actions.AbstractStatefulAction;
import org.jboss.loom.actions.ManualAction;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ctx.MigrationData;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.migrators.remoting.RemotingConfigBean;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.utils.Utils;
import org.jboss.loom.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  $JBOSS_HOME/server/$PROFILE/conf/standardjboss.xml
 * 
 *  Docs: https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/5/html-single/Administration_And_Configuration_Guide/index.html#EJBs_on_JBoss
 * 
 *  @Jira MIGR-45
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Ejb3Migrator extends AbstractMigrator implements IMigrator {
    private static final Logger log = LoggerFactory.getLogger( Ejb3Migrator.class );
    
    @Override protected String getConfigPropertyModuleName() { return "ejb3"; }


    public Ejb3Migrator( GlobalConfiguration globalConfig ) {
        super( globalConfig );
    }


    @Override
    public void loadAS5Data( MigrationContext ctx ) throws MigrationException 
    {
        // InvokerProxyBindingBean
        File confFile = Utils.createPath( this.getGlobalConfig().getAS5Config().getConfDir(), "standardjboss.xml");
        List<InvokerProxyBindingBean> invoBeans = XmlUtils.readXmlConfigFileMulti( confFile,
                "/jboss/invoker-proxy-bindings/invoker-proxy-binding", InvokerProxyBindingBean.class, "EJB3 invoker proxy binding config");
        
        // ContainerConfigBean
        confFile = Utils.createPath( this.getGlobalConfig().getAS5Config().getConfDir(), "standardjboss.xml");
        List<ContainerConfigBean> contBeans = XmlUtils.readXmlConfigFileMulti( confFile,
                "/jboss/container-configurations/container-configuration", ContainerConfigBean.class, "EJB3 container config");

        // TODO: EJB jar's META-INF/
        
        // Store to context
        ctx.getMigrationData().put( this.getClass(), new Data(invoBeans, contBeans) );
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
    public static class Data extends MigrationData {
        List<InvokerProxyBindingBean> invokers = Collections.EMPTY_LIST;
        List<ContainerConfigBean> containters = Collections.EMPTY_LIST;


        public Data( List<InvokerProxyBindingBean> invokers, List<ContainerConfigBean> containters ) {
            this.invokers = invokers;
            this.containters = containters;
        }
        
        public boolean isEmpty(){
            return (invokers == null || invokers.isEmpty()) && (containters == null || invokers.isEmpty());
        };


        @Override
        public List<RemotingConfigBean> getConfigFragments() {
            if( isEmpty() ) 
                return Collections.EMPTY_LIST;
            List ret = new ArrayList( invokers.size() + containters.size() );
            return UnmodifiableList.decorate( ret );
        }
        
    }// class Data
    

}// class
