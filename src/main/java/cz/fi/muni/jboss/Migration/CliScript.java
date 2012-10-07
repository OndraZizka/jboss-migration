package cz.fi.muni.jboss.Migration;

import cz.fi.muni.jboss.Migration.ConnectionFactories.ResourceAdapter;
import cz.fi.muni.jboss.Migration.DataSources.DatasourceAS7;
import cz.fi.muni.jboss.Migration.DataSources.XaDatasourceAS7;
import cz.fi.muni.jboss.Migration.Logging.Logger;
import cz.fi.muni.jboss.Migration.Logging.LoggingAS7;
import cz.fi.muni.jboss.Migration.Security.SecurityDomain;
import cz.fi.muni.jboss.Migration.Server.ConnectorAS7;
import cz.fi.muni.jboss.Migration.Server.SocketBinding;
import cz.fi.muni.jboss.Migration.Server.VirtualServer;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 10/7/12
 * Time: 2:28 PM
 */
public interface CliScript {
    public String createDatasourceScript(DatasourceAS7 datasourceAS7) throws CliScriptException;
    public String createXaDatasourceScript(XaDatasourceAS7 xaDatasourceAS7) throws CliScriptException;
    public String createResourceAdapterScript(ResourceAdapter resourceAdapter) throws CliScriptException;
    public String createLoggerScript(Logger logger);
    public String createHandlersScript(LoggingAS7 loggingAS7);
    public String createSecurityDomainScript(SecurityDomain securityDomain);
    public String createConnectorScript(ConnectorAS7  connectorAS7);
    public String createVirtualServerScript(VirtualServer virtualServer);
    public String createSocketBinding(SocketBinding socketBinding);



}
