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
    public String createLoggerScript(Logger logger) throws  CliScriptException;
    public String createHandlersScript(LoggingAS7 loggingAS7) throws CliScriptException;
    public String createSecurityDomainScript(SecurityDomain securityDomain) throws CliScriptException;
    public String createConnectorScript(ConnectorAS7  connectorAS7) throws CliScriptException;
    public String createVirtualServerScript(VirtualServer virtualServer) throws CliScriptException;
    public String createSocketBinding(SocketBinding socketBinding) throws  CliScriptException;



}
