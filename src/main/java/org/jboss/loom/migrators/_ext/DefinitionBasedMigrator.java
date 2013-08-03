package org.jboss.loom.migrators._ext;

import org.jboss.loom.migrators._ext.process.MigratorDefinitionProcessor;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.migrators._ext.MigratorDefinition.XmlFileQueryDef;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.utils.Utils;
import org.jboss.loom.utils.XmlUtils;
import org.jboss.loom.utils.el.IExprLangEvaluator;
import org.jboss.loom.utils.el.JuelCustomResolverEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Migrator which obeys the rules externalized to .mig.xml file ("migrator definitions").
 *  This class serves as a base class for Javassist-created subclasses; purpose:
 * 
 *    * Differentiate migrators created from different .mig.xml files (<migrator name="...">).
 *    * Allow to further subclass by a Groovy script.
 * 
 *  See the docs at https://github.com/OndraZizka/jboss-migration/wiki/Migrator-Definition-Rules .
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class DefinitionBasedMigrator extends AbstractMigrator implements IMigrator {
    private static final Logger name = LoggerFactory.getLogger( DefinitionBasedMigrator.class );

    // Data
    
    private MigratorDefinition descriptor;
    
    private Map<String, Class<? extends IConfigFragment>> jaxbClasses = new HashMap();
    
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


    /**
     *  Callback to load source server config.
     */
    @Override
    public void loadSourceServerConfig( final MigrationContext ctx ) throws MigrationException {

        // XML queries
        if( this.descriptor.xmlQueries != null )
        for( MigratorDefinition.XmlFileQueryDef query : this.descriptor.xmlQueries ) {
            Utils.validate( query );
            
            Class<? extends IConfigFragment> jaxbCls = this.jaxbClasses.get( query.jaxbBeanAlias );
            if( null == jaxbCls ){
                try {
                    jaxbCls = (Class<? extends IConfigFragment>) this.getClass().getClassLoader().loadClass( query.jaxbBeanAlias );
                } catch( ClassNotFoundException ex ) {
                    throw new MigrationException("Can't load JAXB class '"+query.jaxbBeanAlias
                            + "'\n    used in " + this.descriptor + ":\n    " + ex.getMessage() );
                }
                if( null == jaxbCls )
                    throw new MigrationException("Can't find JAXB class '"+query.jaxbBeanAlias
                            + "'\n    used in " + this.descriptor);
            }
            
            // Create a Context-based IVariablesProvider.
            // TODO: Move somewhere else.
            final IExprLangEvaluator.IVariablesProvider varProvider = new IExprLangEvaluator.IVariablesProvider() {
                @Override public Object getVariable( String name ) {
                    switch( name ) {
                        case "srcServer": return ctx.getConf().getGlobal().getAS5Config();
                        case "destServer":
                        case "targServer": return ctx.getConf().getGlobal().getAS7Config();
                    }
                    return "";
                }
            };
            
            // TODO: Evaluate EL in these. 
            // Or - should we do it in ExternalMigratorsLoader?
            final JuelCustomResolverEvaluator evtor = new JuelCustomResolverEvaluator(varProvider);
            final String pathMask = query.pathMask; // evtor.evaluateEL(query.pathMask); 
            final String xpath = query.xpath;
            final String subjectLabel = query.subjectLabel;
            
            List<IConfigFragment> conf = XmlUtils.readXmlConfigFiles(
                    new File("."), pathMask, xpath, jaxbCls, subjectLabel);
            
            this.loads.put( query.id, new ConfigLoadResult( query, conf ) );
        }
        
        // Property queries
        
        // File list queries
    }


    public Map<String, ConfigLoadResult> getQueryResults() {
        return loads;
    }

    public ConfigLoadResult getQueryResultByName( String name ){
        return this.loads.get( name );
    }
    
    

    /**
     *  Callback to create actions based on the .mig.xml descriptor using MigratorDescriptorProcessor.
     */
    @Override
    public void createActions( MigrationContext ctx ) throws MigrationException {
        List<IMigrationAction> actions = new MigratorDefinitionProcessor(this).process( this.descriptor );
        for( IMigrationAction iMigrationAction : actions ) {
            ctx.getActions().add( iMigrationAction );
        }
    }

    
    // Get/set
    public void setJaxbClasses( Map<String, Class<? extends IConfigFragment>> jaxbClasses ) { this.jaxbClasses = jaxbClasses; }

    DefinitionBasedMigrator addJaxbClass( String name, Class<? extends IConfigFragment> cls ) {
        this.jaxbClasses.put( name, cls );
        return this;
    }

    public static Logger getName() { return name; }
    public MigratorDefinition getDescriptor() { return descriptor; }

    
    // Struct
    public static class ConfigLoadResult {
        public XmlFileQueryDef descriptor;
        public List<IConfigFragment> configFragments;

        public ConfigLoadResult( XmlFileQueryDef descriptor, List<IConfigFragment> conf ) {
            this.descriptor = descriptor;
            this.configFragments = conf;
        }
    }

}// class
