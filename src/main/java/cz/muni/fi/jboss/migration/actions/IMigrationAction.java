package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.MigrationContext;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.spi.IMigrator;

import java.util.List;

/**
 * Ammunition for MIGR-31 and MIGR-23.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface IMigrationAction {

    /**
     * State management - basically, a helper state to check in case
     * the lifecycle methods were called in wrong order.
     * Should be only used by actions themselves.
     */
    enum State {
        INITIAL, BACKED_UP, DONE, FINISHED, ROLLED_BACK
    }

    State getState();


    // Data
    
    /**  Why was this action created. I.e. what AS 5 config piece is it's counterpart? */
    String getOriginMessage();
    
    /** Where was this action created. (Debug purposes) */
    StackTraceElement getOriginStackTrace();
    
    /** Which migrator created this action. */
    Class<? extends IMigrator> getFromMigrator();
    
    /**
     * @returns A description of this action in terms of what exactly would it do.
     */
    String toDescription();

    List<String> getWarnings();


    // Implementation stuff
    void setMigrationContext(MigrationContext ctx);

    MigrationContext getMigrationContext();


    // "Lifecycle"

    /**
     * Checks whether this action can be performed under current conditions.
     * E.g. action for file copying would check if the file exists and is readable,
     * and if the destination file does not exists or is allowed to be overwritten and is writable.
     */
    void preValidate() throws MigrationException;

    /**
     * Performs whatever is necessary to roll back the action after it was performed.
     * Data are stored in this action object.
     * Data stored by this method will be used in rollback().
     */
    void backup() throws MigrationException;

    /**
     * Actually performs the action - does the real change:
     * Copies the file, sends a CLI command.
     */
    void perform() throws MigrationException;

    /**
     * Checks if the actions went well, e.g check if the file was copied etc.
     * May be no-op for certain operations which went well if perform() didn't throw.
     */
    void postValidate() throws MigrationException;

    /**
     * Cleans anything created by the backup() method, e.g a backup file.
     */
    void cleanBackup();

    /**
     * Undoes whatever was done by perform(). Uses data stored in this action by backup().
     *
     * @throws MigrationException
     */
    void rollback() throws MigrationException;

}// class
