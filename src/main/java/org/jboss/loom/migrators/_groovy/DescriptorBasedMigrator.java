package org.jboss.loom.migrators._groovy;

import java.util.List;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.spi.IMigrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class DescriptorBasedMigrator extends AbstractMigrator implements IMigrator {
    private static final Logger log = LoggerFactory.getLogger( DescriptorBasedMigrator.class );


    static DescriptorBasedMigrator from( MigratorDescriptorBean desc,  GlobalConfiguration gc ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    
    private MigratorDescriptorBean descriptor;
    
    private List<Class> jaxbClasses;
    

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

    @Override
    protected String getConfigPropertyModuleName() {
        return this.descriptor.name;
    }


    @Override
    public void loadSourceServerConfig( MigrationContext ctx ) throws MigrationException {
    }


    @Override
    public void createActions( MigrationContext ctx ) throws MigrationException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    
    // Get/set
    public void setJaxbClasses( List<Class> jaxbClasses ) { this.jaxbClasses = jaxbClasses; }


}// class
