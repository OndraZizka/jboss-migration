package cz.muni.fi.jboss.migration.migrators;

/**
 * @author Roman Jakubco
 * Date: 1/20/13
 * Time: 3:40 PM
 */
public interface IMigrator {

    public void migration(MigrationContext ctx);

}
