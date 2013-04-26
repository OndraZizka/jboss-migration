package org.jboss.loom.spi;

import org.jboss.loom.MigrationContext;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.ActionException;
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
     * Method for loading all files from AS5 and converting them to objects for migration which are then stored in Mig-
     * rationContext
     *
     * @param ctx context of migration with necessary object and information
     * @throws LoadMigrationException if loading of AS5 configuration fails (missing files / cannot read / wrong content)
     */
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException;

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
