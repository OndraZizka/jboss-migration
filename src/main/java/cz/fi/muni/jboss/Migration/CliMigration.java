package cz.fi.muni.jboss.Migration;

import cz.fi.muni.jboss.Migration.ConnectionFactories.ConnectionFactoryAS7;
import cz.fi.muni.jboss.Migration.DataSources.DatasourceAS7;
import cz.fi.muni.jboss.Migration.DataSources.Driver;
import cz.fi.muni.jboss.Migration.DataSources.XaDatasourceAS7;
import cz.fi.muni.jboss.Migration.Logging.Logger;
import cz.fi.muni.jboss.Migration.Logging.LoggingAS7;
import cz.fi.muni.jboss.Migration.Security.SecurityAS7;
import cz.fi.muni.jboss.Migration.Security.SecurityDomain;
import cz.fi.muni.jboss.Migration.Server.ConnectorAS7;
import cz.fi.muni.jboss.Migration.Server.SocketBinding;
import cz.fi.muni.jboss.Migration.Server.VirtualServer;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 10/3/12
 * Time: 2:59 PM
 */
public interface CliMigration {
    public void createDatasource(DatasourceAS7 datasourceAS7);
    public void createXaDatasource(XaDatasourceAS7 xaDatasourceAS7);
    public void createDriver(Driver driver);
    public void createResourceAdapters(ConnectionFactoryAS7 connectionFactoryAS7);
    public void createHandlers(LoggingAS7 loggingAS7);
    public void createLogger(Logger logger);
    public void createSecurityDomain(SecurityDomain securityDomain);
    public void createConnector(ConnectorAS7 connectorAS7);
    public void createVirtualServer(VirtualServer virtualServer);
    public void createSocketBinding(SocketBinding socketBinding);

}
