package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.MigrationContext;
import cz.muni.fi.jboss.migration.ex.MigrationException;

/**
 * Ammunition for MIGR-31 and MIGR-23.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface IMigrationAction {
    
    enum State { INITIAL, BACKED_UP, DONE, FINISHED, ROLLED_BACK };

    State getState();
    
    void setMigrationContext( MigrationContext ctx );
    
    void preValidate() throws MigrationException;
    
    void backup() throws MigrationException;
    
    void perform() throws MigrationException;
    
    void postValidate() throws MigrationException;
    
    void cleanBackup();
    
    void rollback() throws MigrationException;
    
}
