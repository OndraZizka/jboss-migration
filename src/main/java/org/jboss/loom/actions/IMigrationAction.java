package org.jboss.loom.actions;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.tools.report.adapters.ToHashCodeAdapter;

/**
 * Actions of which the migration consists.
 * 
 * An action must implement lifecycle callbacks:
 * preValidate(), backup(), perform(), postValidate(), cleanBackup(), rollback().
 * 
 * It should contain information about where it why created - 
 * what config piece of the source server it carries.
 * 
 * Also it should be able to tell by which Migrator it was created 
 * and ideally, at what place in the code (for exceptions).
 * 
 * It may contain warnings if some validation failed 
 * but doesn't prevent successful run (also see {@link Configuration.IfExists}).
 * 
 * It keeps a reference to MigrationContext which is needed in perform().
 * 
 * @Jira MIGR-31 and MIGR-23.
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name="action")
@XmlAccessorType( XmlAccessType.NONE )
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
    @XmlElement(name = "originMsg")
    String getOriginMessage();
    
    /** Where was this action created. (Debug purposes) */
    StackTraceElement getOriginStackTrace();
    
    /** Which migrator created this action. */
    Class<? extends IMigrator> getFromMigrator();
    
    @XmlElementWrapper(name = "dependencies")
    @XmlElement(name = "dep")
    @XmlJavaTypeAdapter( ToHashCodeAdapter.class )
    List<IMigrationAction> getDependencies();
    IMigrationAction addDependency( IMigrationAction dep );
    /**
     * @returns -1 if doesn't depend, 0 if equals, 1 if direct dependency, or the distance of transitive dependency.
     */
    public int dependsOn( IMigrationAction other ) throws AbstractStatefulAction.CircularDependencyException;
    
    
    /**
     * @returns A description of this action in terms of what exactly would it do.
     */
    @XmlElement(name = "desc") // Doesn't work - JAXB needs a getter.
    String toDescription();

    
    @XmlElementWrapper(name="warnings")
    @XmlElement(name="warning")
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
