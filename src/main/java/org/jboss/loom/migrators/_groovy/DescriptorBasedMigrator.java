package org.jboss.loom.migrators._groovy;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jboss.dmr.ModelNode;
import org.jboss.loom.actions.CliCommandAction;
import org.jboss.loom.actions.CopyFileAction;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.actions.ManualAction;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.migrators._groovy.MigratorDescriptorBean.Action;
import org.jboss.loom.migrators._groovy.MigratorDescriptorBean.XmlFileQuery;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class DescriptorBasedMigrator extends AbstractMigrator implements IMigrator {
    private static final Logger log = LoggerFactory.getLogger( DescriptorBasedMigrator.class );

    // Data
    
    private MigratorDescriptorBean descriptor;
    
    private List<Class> jaxbClasses = new LinkedList();
    
    private File baseDir;
    
    private Map<String, ConfigLoadResult> loads = new HashMap();
    
    
    
    // Constructors
    
    public static DescriptorBasedMigrator from( MigratorDescriptorBean desc, GroovyMigratorsLoader loader, GlobalConfiguration globalConfig ) {
        DescriptorBasedMigrator mig = new DescriptorBasedMigrator(globalConfig);
        mig.descriptor = desc;
        return mig;
    }

    public DescriptorBasedMigrator( GlobalConfiguration globalConfig ) {
        super( globalConfig );
    }
    
    public DescriptorBasedMigrator( MigratorDescriptorBean descriptor, GlobalConfiguration globalConfig ) {
        super( globalConfig );
        this.descriptor = descriptor;
    }
    
    
    
    
    // Abstract methods overrides.

    @Override protected String getConfigPropertyModuleName() {
        return this.descriptor.name;
    }


    @Override
    public void loadSourceServerConfig( MigrationContext ctx ) throws MigrationException {
        
        for( MigratorDescriptorBean.XmlFileQuery query : this.descriptor.xmlQueries ) {
            List<IConfigFragment> conf = XmlUtils.readXmlConfigFiles(
                    new File("."), query.pathMask, query.xpath, query.jaxbBean, query.subjectLabel);
            this.loads.put( query.id, new ConfigLoadResult( query, conf ) );
        }
    }


    @Override
    public void createActions( MigrationContext ctx ) throws MigrationException {
        for( Action actionDescr : this.descriptor.actions ) {
            IMigrationAction action;
            switch( actionDescr.type ){
                case "manual":
                    action = new ManualAction();
                    break;
                case "copy": 
                    String src = actionDescr.attribs.get("src");
                    String dest = actionDescr.attribs.get("dest");
                    CopyFileAction.IfExists ifExists = CopyFileAction.IfExists.valueOf("ifExists");
                    action = new CopyFileAction( getClass(), new File(src), new File(dest), ifExists ); 
                    break;
                case "cli": 
                    String cliScript = actionDescr.attribs.get("cliScript");
                    ModelNode modelNode = ModelNode.fromString( cliScript );
                    action = new CliCommandAction( getClass(), cliScript, modelNode ); 
                    break;
                default: 
                    throw new MigrationException("Unsupported action type '" + actionDescr.type + "' in " + descriptor.location.getSystemId());
            }
        }
    }

    
    // Get/set
    public void setJaxbClasses( List<Class> jaxbClasses ) { this.jaxbClasses = jaxbClasses; }

    DescriptorBasedMigrator addJaxbClass( Class cls ) {
        this.jaxbClasses.add( cls );
        return this;
    }

    
    
    // Struct
    private static class ConfigLoadResult {
        public XmlFileQuery descriptor;
        public List<IConfigFragment> conf;


        public ConfigLoadResult( XmlFileQuery descriptor, List<IConfigFragment> conf ) {
            this.descriptor = descriptor;
            this.conf = conf;
        }
    }

}// class
