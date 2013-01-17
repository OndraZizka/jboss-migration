package cz.muni.fi.jboss.Migration;

import cz.muni.fi.jboss.Migration.ConnectionFactories.ResourceAdapter;
import cz.muni.fi.jboss.Migration.DataSources.DatasourceAS7;
import cz.muni.fi.jboss.Migration.DataSources.Driver;
import cz.muni.fi.jboss.Migration.DataSources.XaDatasourceAS7;
import cz.muni.fi.jboss.Migration.Logging.*;
import cz.muni.fi.jboss.Migration.Security.SecurityDomain;
import cz.muni.fi.jboss.Migration.Server.ConnectorAS7;
import cz.muni.fi.jboss.Migration.Server.SocketBinding;
import cz.muni.fi.jboss.Migration.Server.VirtualServer;

/**
 * Interface for creating CLI scripts
 *
 * @author  Roman Jakubco
 * Date: 10/7/12
 * Time: 2:28 PM
 */

public interface CliScript {

    /**
     * Method for generating CLI script for creating given AS7 datasource
     *
     * @param datasourceAS7 configuration of datasource from AS7
     * @return  String containing CLI commands for creating datasource
     * @throws CliScriptException  if one of the required attributes is null or empty
     */
    public String createDatasourceScript(DatasourceAS7 datasourceAS7) throws CliScriptException;

    /**
     *  Method for generating CLI script for creating given AS7 xa-datasource
     *
     * @param xaDatasourceAS7  configuration of xa-datasource from AS7
     * @return  String containing CLI commands for creating xa-datasource
     * @throws CliScriptException  if one of the required attributes is null of empty
     */
    public String createXaDatasourceScript(XaDatasourceAS7 xaDatasourceAS7) throws CliScriptException;

    /**
     * Method for generating CLI script for creating driver
     *
     * @param driver configuration of driver
     * @return  String containing CLI commands for creating driver
     * @throws CliScriptException if one of required attributes is null or empty
     */
    public String createDriverScript(Driver driver) throws CliScriptException;


    /**
     * Methof for generating CLI script for creating given resource-adapter
     *
     * @param resourceAdapter configuration of resource-adapter
     * @return  String conataining CLI commands for creating xa-datasource
     * @throws CliScriptException  if one of required attributes is null or empty
     */
    public String createResAdapterScript(ResourceAdapter resourceAdapter) throws CliScriptException;

    /**
     * Method for generating CLI script for creating given logger
     *
     * @param logger configuration of logger
     * @return String containing CLI commands for creating logger
     * @throws CliScriptException if one of required attributes is null or empty
     */
    public String createLoggerScript(Logger logger) throws  CliScriptException;

    /**
     * Method for generating CLI script for creating all handlers in logging subsystem
     *
     * @param loggingAS7  configuration of logging subsystem
     * @return  String containing CLI commands for creating handlers
     * @throws CliScriptException  if one of required attributes is null or empty
     */
    public String createHandlersScript(LoggingAS7 loggingAS7) throws CliScriptException;

    /**
     * Method for generating CLI script for creating given security-domain
     *
     * @param securityDomain configuration of security-domain
     * @return  String containing CLI commands for creating security-domain
     * @throws CliScriptException if one of required attributes is null or empty
     */
    public String createSecurityDomainScript(SecurityDomain securityDomain) throws CliScriptException;

    /**
     * Method for generating CLI script for creating given connector
     *
     * @param connectorAS7 configuration of connector
     * @return  String containing CLI commands for creating connector
     * @throws CliScriptException if one of required attributes is null or empty
     */
    public String createConnectorScript(ConnectorAS7  connectorAS7) throws CliScriptException;

    /**
     * Method for generating CLI script for creating given virtual-server
     *
     * @param virtualServer configuration of virtual-server
     * @return  String containing CLI commands for generating virtual-server
     * @throws CliScriptException  if one of required attributes is null or empty
     */
    public String createVirtualServerScript(VirtualServer virtualServer) throws CliScriptException;

    /**
     * Method for generating CLI script for creating given socket-binding
     *
     * @param socketBinding configuration of socket-binding
     * @return  String conatining CLi commands for creating socket-binding
     * @throws CliScriptException if one of required attributes is null or empty
     */
    public String createSocketBinding(SocketBinding socketBinding) throws  CliScriptException;

    /**
     * Method for generating CLI script for creating periodic-rotating-file-handler
     *
     * @param periodic configuration of periodic-rotating-file-handler
     * @return String containing CLI commands for creating periodic-rotating-file-handler
     * @throws CliScriptException
     */
    public String createPerHandlerScript(PerRotFileHandler periodic) throws CliScriptException;

    /**
     * Method for generating CLI script for creating size-rotating-file-handler
     *
     * @param size configuration of size-rotating-file-handler
     * @return String containing CLI commands for creating size-rotating-handler
     * @throws CliScriptException if one of required attributes is null or empty
     */
    public String createSizeHandlerScript(SizeRotatingFileHandler size) throws CliScriptException;

    /**
     *  Method for generating CLI script for creating size-rotating-file-handler
     *
     * @param asyncHandler   configuration of async-handler
     * @return  String containing CLI commands for creating async-handler
     * @throws CliScriptException if one of required attributes is null or empty
     */
    public String createAsyncHandlerScript(AsyncHandler asyncHandler) throws CliScriptException;

    /**
     *  Method for generating CLI script for creating size-rotating-file-handler
     *
     * @param customHandler configuration of custom-handler
     * @return  String containing CLI commands for creating custom-handler
     * @throws CliScriptException  if one of required attributes is null or empty
     */
    public String createCustomHandlerScript(CustomHandler customHandler) throws CliScriptException;

    /**
     *  Method for generating CLI script for creating console-handler
     *
     * @param consoleHandler  configuration of console-handler
     * @return String containing CLI commands for creating console-handler
     * @throws CliScriptException  if one of required attributes is null or empty
     */
    public String createConsoleHandlerScript(ConsoleHandler consoleHandler) throws CliScriptException;


}
