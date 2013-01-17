package cz.muni.fi.jboss.Migration;

import cz.muni.fi.jboss.Migration.ConnectionFactories.ConnectionFactories;
import cz.muni.fi.jboss.Migration.ConnectionFactories.ConnectionFactoryAS5;
import cz.muni.fi.jboss.Migration.ConnectionFactories.ResourceAdapter;
import cz.muni.fi.jboss.Migration.ConnectionFactories.ResourceAdaptersSub;
import cz.muni.fi.jboss.Migration.DataSources.*;
import cz.muni.fi.jboss.Migration.Logging.LoggingAS5;
import cz.muni.fi.jboss.Migration.Logging.LoggingAS7;
import cz.muni.fi.jboss.Migration.Security.SecurityAS5;
import cz.muni.fi.jboss.Migration.Security.SecurityAS7;
import cz.muni.fi.jboss.Migration.Server.ServerAS5;
import cz.muni.fi.jboss.Migration.Server.ServerSub;
import cz.muni.fi.jboss.Migration.Server.SocketBindingGroup;

import java.util.Collection;

/**
 *
 *
 * @author: Roman Jakubco
 * Date: 8/28/12
 * Time: 2:46 PM
 */
public interface Migration {

    /**
     * Method for getting all created socket-bindings
     *
     * @return  SocketBidningGroup, which contains all Socket-Bindings
     */
    public SocketBindingGroup getSocketBindingGroup();



    public Collection<CopyMemory> getCopyMemories();

    /**
     * Method for migrating configuration of datasource from AS5 to AS7
     *
     * @param datasources collection of datasources from AS5
     * @return collection of migrated datasources
     */
    public Collection<DatasourceAS7> datasourceMigration(Collection<DatasourceAS5> datasources);

    /**
     * Method for migrating configuration of xa-datasource from AS5 to AS7
     *
     * @param datasources  collection of xa-datasources from AS5
     * @return collection of migrated xa-datasources
     */
    public Collection<XaDatasourceAS7> xaDatasourceMigration(Collection<XaDatasourceAS5> datasources);

    /**
     * Method for migrating configuration of connection factory from AS5 to resource-adapter in AS7
     *
     * @param connectionFactoryAS5 object containing parsed connectionFactories from AS5
     * @return ResourceAdapter, which represents migrated connection-factory
     */
    public ResourceAdapter connectionFactoryMigration(ConnectionFactoryAS5 connectionFactoryAS5);

    /**'
     * Method for migrating all connection factories from AS5 to resource-adapter subsystem
     *
     * @param connectionFactories  object containing parsed Connection Factories from AS5
     * @return
     */
    public ResourceAdaptersSub resourceAdaptersMigration(Collection<ConnectionFactories> connectionFactories);

    /**
     * Method for migrating all datasource files from AS5, which contain datasource and xa-datasources
     *
     * @param dataSources collection of all xa-datasources and datasources
     * @return  DatasourcesSub, which represents all migrated datasources and xa-datasources
     */
    public DatasourcesSub datasourceSubMigration(Collection<DataSources> dataSources);

    /**
     * Method for migrating configuration of Tomcat from AS5 to AS7 configuration
     *
     * @param serverAS5 object representing configuration file of Tomcat from AS5
     * @return  ServerSub, which represents migrated Tomcat configuration
     */
    public ServerSub serverMigration(ServerAS5 serverAS5);

    /**
     * Method for migrating configuration of logging from AS5 to AS7
     *
     * @param loggingAS5  configuration of logging from AS5
     * @return  LoggingAS7, which represents migrated logging
     */
    public LoggingAS7 loggingMigration(LoggingAS5 loggingAS5);

    /**
     * Method for migrating security configuration from AS5 to AS7
     *
     * @param securityAS5  configuration of security from AS5
     * @return  SecurityAs7, which represents migrated security
     */
    public SecurityAS7 securityMigration(SecurityAS5 securityAS5);
}
