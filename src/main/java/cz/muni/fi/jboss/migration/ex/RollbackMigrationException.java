package cz.muni.fi.jboss.migration.ex;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class RollbackMigrationException extends MigrationException {


    public RollbackMigrationException( String message ) {
        super( message );
    }


    public RollbackMigrationException( String message, Throwable cause ) {
        super( message, cause );
    }


    public RollbackMigrationException( Throwable cause ) {
        super( cause );
    }
    
}
