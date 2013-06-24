package org.jboss.loom.migrators._ext;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.migrators._ext.MigratorDefinition.XmlFileQueryDef;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class DefinitionBasedMigrator extends AbstractMigrator implements IMigrator {
    private static final Logger name = LoggerFactory.getLogger( DefinitionBasedMigrator.class );

    // Data
    
    private MigratorDefinition descriptor;
    
    private List<Class> jaxbClasses = new LinkedList();
    
    private File baseDir;
    
    private Map<String, ConfigLoadResult> loads = new HashMap();
    
    
    
    // Constructors
    
    public static DefinitionBasedMigrator from( MigratorDefinition desc, GlobalConfiguration globalConfig ) {
        DefinitionBasedMigrator mig = new DefinitionBasedMigrator(globalConfig);
        mig.descriptor = desc;
        return mig;
    }

    public DefinitionBasedMigrator( GlobalConfiguration globalConfig ) {
        super( globalConfig );
    }
    
    public DefinitionBasedMigrator( MigratorDefinition descriptor, GlobalConfiguration globalConfig ) {
        super( globalConfig );
        this.descriptor = descriptor;
    }
    
    
    
    
    // Abstract methods overrides.

    @Override protected String getConfigPropertyModuleName() {
        return this.descriptor.name;
    }


    @Override
    public void loadSourceServerConfig( MigrationContext ctx ) throws MigrationException {
        
        for( MigratorDefinition.XmlFileQueryDef query : this.descriptor.xmlQueries ) {
            List<IConfigFragment> conf = XmlUtils.readXmlConfigFiles(
                    new File("."), query.pathMask, query.xpath, query.jaxbBean, query.subjectLabel);
            this.loads.put( query.id, new ConfigLoadResult( query, conf ) );
        }
    }
    
    ConfigLoadResult getQueryResultByName( String name ){
        return this.loads.get( name );
    }
    
    

    /**
     *  Creates actions based on the .mig.xml descriptor using MigratorDescriptorProcessor.
     */
    @Override
    public void createActions( MigrationContext ctx ) throws MigrationException {
        List<IMigrationAction> actions = new MigratorDefinitionProcessor(this).processChildren( this.descriptor );
        for( IMigrationAction iMigrationAction : actions ) {
            ctx.getActions().add( iMigrationAction );
        }
    }

    
    // Get/set
    public void setJaxbClasses( List<Class> jaxbClasses ) { this.jaxbClasses = jaxbClasses; }

    DefinitionBasedMigrator addJaxbClass( Class cls ) {
        this.jaxbClasses.add( cls );
        return this;
    }

    
    
    // Struct
    static class ConfigLoadResult {
        public XmlFileQueryDef descriptor;
        public List<IConfigFragment> configFragments;

        public ConfigLoadResult( XmlFileQueryDef descriptor, List<IConfigFragment> conf ) {
            this.descriptor = descriptor;
            this.configFragments = conf;
        }
    }

}// class
