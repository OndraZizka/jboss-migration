package org.jboss.loom.migrators.transactions;

import org.apache.commons.collections.map.MultiValueMap;
import org.jboss.loom.MigrationContext;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.LoadMigrationException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class TransactionsMigrator extends AbstractMigrator {


    @Override  protected String getConfigPropertyModuleName() { return "transactions"; }


    public TransactionsMigrator( GlobalConfiguration globalConfig, MultiValueMap config ) {
        super( globalConfig, config );
    }
    
    
    


    @Override
    public void loadAS5Data( MigrationContext ctx ) throws LoadMigrationException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void createActions( MigrationContext ctx ) throws MigrationException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
}// class
