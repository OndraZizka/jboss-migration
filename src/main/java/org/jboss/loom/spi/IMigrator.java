package org.jboss.loom.spi;

import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.LoadMigrationException;
import org.jboss.loom.ex.MigrationException;

/**
 * A Migrator is responsible for
 * <ul>
 * <li> reading the necessary data from AS, according to given configuration
 * <li> transforming these data into it's own metamodel objects
 * <li> providing the corresponding representation of data (CLI commands) for the target server (AS 7)
 * <li> process the provided given custom config property
 * </ul>
 *
 * @author Roman Jakubco
 */

public interface IMigrator {

    public GlobalConfiguration getGlobalConfig();

    public void setGlobalConfig(GlobalConfiguration conf);


    /**
     * Loads all files from AS5 and converts them to objects for migration 
     * which are then stored in MigrationContext.
     *
     * @param ctx context of migration with necessary object and information
     * @throws LoadMigrationException if loading of AS5 configuration fails (missing files / cannot read / wrong content)
     */
    public void loadSourceServerConfig(MigrationContext ctx) throws MigrationException;

    /**
     * Creates the actions, based on what data is in the context (e.g. previously obtained from the source server).
     */
    public void createActions(MigrationContext ctx) throws MigrationException;


    /**
     * Examines a configuration property, typically acquired as console app params.
     *
     * @param moduleOption It's value May be null, e.g. if the property didn't have '=value' part.
     * @returns 0 if the property wasn't recognized, non-zero otherwise.
     */
    public int examineConfigProperty(Configuration.ModuleSpecificProperty moduleOption);


}// class
