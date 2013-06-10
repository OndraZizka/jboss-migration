package org.jboss.loom.migrators.jaxr;

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
 * $JBOSS_HOME/server/all/deploy/juddi-service.sar/META-INF/jboss-service.xml

<server>
    <!-- The juddi service configration -->
    <mbean code="org.jboss.jaxr.juddi.JUDDIService" name="jboss:service=juddi">
        <!-- Whether we want to run the db initialization scripts -->
        <!-- Should all tables be created on Start-->
        <attribute name="CreateOnStart">false</attribute>
        <!-- Should all tables be dropped on Stop-->
        <attribute name="DropOnStop">false</attribute>
        <!-- Should all tables be dropped on Start-->
        <attribute name="DropOnStart">false</attribute>
        <!-- Datasource to Database-->
        <attribute name="DataSourceUrl">java:/DefaultDS</attribute>
        <!-- Alias to the registry-->
        <attribute name="RegistryOperator">RegistryOperator</attribute>
        <!-- Should I bind a Context to which JaxrConnectionFactory bound-->
        <attribute name="ShouldBindJaxr">true</attribute>
        <!-- Context to which JaxrConnectionFactory to bind to.
             If you have remote clients, please bind it to the global
             namespace(default behavior). To just cater to clients running
             on the same VM as JBoss, change to java:/JAXR -->
        <attribute name="BindJaxr">JAXR</attribute>
        <attribute name="DropDB">false</attribute> 
        <depends>jboss.jca:service=DataSourceBinding,name=DefaultDS</depends>
    </mbean>
</server>

 * 
 * @Jira: MIGR-42
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@ConfigPartDescriptor(
    name = "JAXR (jUDDI) configuration"
)
public class JaxrMigrator extends AbstractMigrator implements IMigrator {

    @Override protected String getConfigPropertyModuleName() { return "jaxr"; }


    public JaxrMigrator( GlobalConfiguration globalConfig ) {
        super( globalConfig );
    }

    @Override
    public void loadSourceServerConfig( MigrationContext ctx ) throws MigrationException {
        // Only in "all" profile (out of those distributed).
        File confFile = Utils.createPath( this.getGlobalConfig().getAS5Config().getDeployDir(), "juddi-service.sar/META-INF/jboss-service.xml");
        
        // Optional.
        if( ! confFile.exists() )  return;
        
        List<JaxrConfigBean> beans = XmlUtils.readXmlConfigFileMulti( confFile, 
                "/server/mbean[@code='org.jboss.jaxr.juddi.JUDDIService']", JaxrConfigBean.class, "JAXR config");
        
        // DDL - data from SQL scripts
        // TBC: We could parse this using HSQL db.
        //File ddlSchema = Utils.createPath( this.getGlobalConfig().getAS5Config().getDeployDir(), "juddi-service.sar/META-INF/ddl/juddi_create_db.ddl");
        //File ddlData   = Utils.createPath( this.getGlobalConfig().getAS5Config().getDeployDir(), "juddi-service.sar/META-INF/ddl/juddi_data.ddl");
        
        // Store to context
        ctx.getMigrationData().put( this.getClass(), new MigratorData(beans) );
    }

    
    /**
     * Actions.
     */
    @Override
    public void createActions( MigrationContext ctx ) throws MigrationException {
        MigratorData data = ctx.getMigrationData().get(this.getClass());
        if( data == null || data.getConfigFragments().isEmpty() )
            return;
        
        // ManualAction.
        AbstractStatefulAction warnAction = new ManualAction().addWarning("JAXR (jUDDI) config migration is not yet supported.");
        ctx.getActions().add( warnAction );

        // TODO
        for( IConfigFragment fra : data.getConfigFragments() ) {
            if( ! (fra instanceof JaxrConfigBean) )  continue;
            JaxrConfigBean bean = (JaxrConfigBean) fra;
            // ...
        }
    }
        
}// class
