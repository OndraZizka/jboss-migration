package cz.fi.muni.jboss.Migration;

import cz.fi.muni.jboss.Migration.ConnectionFactories.ConnectionFactories;
import cz.fi.muni.jboss.Migration.ConnectionFactories.ResourceAdaptersSub;
import cz.fi.muni.jboss.Migration.DataSources.*;
import cz.fi.muni.jboss.Migration.Logging.LoggingAS5;
import cz.fi.muni.jboss.Migration.Logging.LoggingAS7;
import cz.fi.muni.jboss.Migration.Security.SecurityAS5;
import cz.fi.muni.jboss.Migration.Security.SecurityAS7;
import cz.fi.muni.jboss.Migration.Server.ServerAS5;
import cz.fi.muni.jboss.Migration.Server.ServerSub;
import cz.fi.muni.jboss.Migration.Server.SocketBindingGroup;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 8/28/12
 * Time: 2:46 PM
 */
public interface Migration {

    /**
     *
     * @return
     */
    public SocketBindingGroup getSocketBindingGroup();
    /**
     *
     * @param datasources
     * @return
     */
    public Collection<DatasourceAS7> datasourceMigration(Collection<DatasourceAS5> datasources);

    /**
     *
     * @param datasources
     * @return
     */
    public Collection<XaDatasourceAS7> xaDatasourceMigration(Collection<XaDatasourceAS5> datasources);

    /**
     *
     * @param connectionFactories
     * @return
     */
    public ResourceAdaptersSub connectionFactoriesMigration(ConnectionFactories connectionFactories);

    /**
     *
     * @param dataSources
     * @return
     */
    public DatasourcesSub datasourceSubMigration(Collection<DataSources> dataSources);

    /**
     *
     * @param serverAS5
     * @return
     */
    public ServerSub serverMigration(ServerAS5 serverAS5);

    /**
     *
     * @param loggingAS5
     * @return
     */
    public LoggingAS7 loggingMigration(LoggingAS5 loggingAS5);

    /**
     *
     * @param securityAS5
     * @return
     */
    public SecurityAS7 securityMigration(SecurityAS5 securityAS5);
}
