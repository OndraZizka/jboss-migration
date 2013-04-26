package org.jboss.loom.ex;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class RollbackMigrationException extends MigrationException {

    private Throwable rollbackCause;


    /**
     * For wrapping both rollback cause and exception during rollback.
     * Maybe we could get rid of the other constructors.
     */
    public RollbackMigrationException(Throwable rollbackCause, Throwable originalCause) {
        super(rollbackCause);
    }

    public RollbackMigrationException(String message) {
        super(message);
    }

    public RollbackMigrationException(String message, Throwable cause) {
        super(message, cause);
    }


    public RollbackMigrationException(Throwable cause) {
        super(cause);
    }


    public Throwable getRollbackCause() {
        return rollbackCause;
    }

    public void setRollbackCause(Throwable rollbackCause) {
        this.rollbackCause = rollbackCause;
    }

}// class
