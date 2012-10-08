package cz.fi.muni.jboss.Migration;

import cz.fi.muni.jboss.Migration.ConnectionFactories.ResourceAdapter;
import cz.fi.muni.jboss.Migration.DataSources.DatasourceAS7;
import cz.fi.muni.jboss.Migration.DataSources.DatasourcesSub;
import cz.fi.muni.jboss.Migration.DataSources.XaDatasourceAS7;
import cz.fi.muni.jboss.Migration.Logging.*;
import cz.fi.muni.jboss.Migration.Security.SecurityDomain;
import cz.fi.muni.jboss.Migration.Server.ConnectorAS7;
import cz.fi.muni.jboss.Migration.Server.SocketBinding;
import cz.fi.muni.jboss.Migration.Server.VirtualServer;
import org.jboss.logmanager.handlers.PeriodicSizeRotatingFileHandler;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 10/7/12
 * Time: 2:28 PM
 */
public interface CliScript {
    /**
     *
     * @param datasourceAS7
     * @return
     * @throws CliScriptException
     */
    public String createDatasourceScript(DatasourceAS7 datasourceAS7) throws CliScriptException;

    /**
     *
     * @param xaDatasourceAS7
     * @return
     * @throws CliScriptException
     */
    public String createXaDatasourceScript(XaDatasourceAS7 xaDatasourceAS7) throws CliScriptException;

    public String createDriverScript(DatasourcesSub datasourcesSub) throws CliScriptException;


    /**
     *
     * @param resourceAdapter
     * @return
     * @throws CliScriptException
     */
    public String createResourceAdapterScript(ResourceAdapter resourceAdapter) throws CliScriptException;

    /**
     *
     * @param logger
     * @return
     * @throws CliScriptException
     */
    public String createLoggerScript(Logger logger) throws  CliScriptException;

    /**
     *
     * @param loggingAS7
     * @return
     * @throws CliScriptException
     */
    public String createHandlersScript(LoggingAS7 loggingAS7) throws CliScriptException;

    /**
     *
     * @param securityDomain
     * @return
     * @throws CliScriptException
     */
    public String createSecurityDomainScript(SecurityDomain securityDomain) throws CliScriptException;

    /**
     *
     * @param connectorAS7
     * @return
     * @throws CliScriptException
     */
    public String createConnectorScript(ConnectorAS7  connectorAS7) throws CliScriptException;

    /**
     *
     * @param virtualServer
     * @return
     * @throws CliScriptException
     */
    public String createVirtualServerScript(VirtualServer virtualServer) throws CliScriptException;

    /**
     *
     * @param socketBinding
     * @return
     * @throws CliScriptException
     */
    public String createSocketBinding(SocketBinding socketBinding) throws  CliScriptException;

    /**
     *
     * @param periodic
     * @return
     * @throws CliScriptException
     */
    public String createPeriodicHandlerScript(PeriodicRotatingFileHandler periodic) throws CliScriptException;

    /**
     *
     * @param size
     * @return
     * @throws CliScriptException
     */
    public String createSizeHandlerScript(SizeRotatingFileHandler size) throws CliScriptException;

    /**
     *
     * @param asyncHandler
     * @return
     * @throws CliScriptException
     */
    public String createAsyncHandlerScript(AsyncHandler asyncHandler) throws CliScriptException;

    /**
     *
     * @param customHandler
     * @return
     * @throws CliScriptException
     */
    public String createCustomHandlerScript(CustomHandler customHandler) throws CliScriptException;

    /**
     *
     * @param consoleHandler
     * @return
     * @throws CliScriptException
     */
    public String createConsoleHandlerScript(ConsoleHandler consoleHandler) throws CliScriptException;


}
