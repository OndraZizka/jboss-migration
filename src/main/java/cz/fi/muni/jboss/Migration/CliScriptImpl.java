package cz.fi.muni.jboss.Migration;

import cz.fi.muni.jboss.Migration.ConnectionFactories.ConfigProperty;
import cz.fi.muni.jboss.Migration.ConnectionFactories.ConnectionDefinition;
import cz.fi.muni.jboss.Migration.ConnectionFactories.ResourceAdapter;
import cz.fi.muni.jboss.Migration.DataSources.DatasourceAS7;
import cz.fi.muni.jboss.Migration.DataSources.XaDatasourceAS7;
import cz.fi.muni.jboss.Migration.DataSources.XaDatasourceProperty;
import cz.fi.muni.jboss.Migration.Logging.Logger;
import cz.fi.muni.jboss.Migration.Logging.LoggingAS7;
import cz.fi.muni.jboss.Migration.Security.LoginModuleAS7;
import cz.fi.muni.jboss.Migration.Security.ModuleOptionAS7;
import cz.fi.muni.jboss.Migration.Security.SecurityDomain;
import cz.fi.muni.jboss.Migration.Server.ConnectorAS7;
import cz.fi.muni.jboss.Migration.Server.SocketBinding;
import cz.fi.muni.jboss.Migration.Server.VirtualServer;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 10/7/12
 * Time: 2:29 PM
 */
public class CliScriptImpl implements CliScript {

    private String tmpMethod(String script, String name, String setter){
        if(setter != null){
            if (!setter.isEmpty()) {
                script = script.concat(name+"="+setter);
            }
        }
        return script;
    }


    @Override
    public String createDatasourceScript(DatasourceAS7 datasourceAS7) throws CliScriptException{
        if(datasourceAS7.getPoolName() == null){
              throw new CliScriptException("Error: pool-name cannot be null", new NullPointerException());
        }
        if(datasourceAS7.getJndiName() == null){
            throw new CliScriptException("Error: jndi-name cannot be null", new NullPointerException());
        }
        if(datasourceAS7.getConnectionUrl() == null){
            throw new CliScriptException("Error: connection-url cannot be null", new NullPointerException());
        }
        if(datasourceAS7.getDriver() == null){
            throw new CliScriptException("Error: driver-name cannot be null", new NullPointerException());
        }
        String script= "/subsystem=datasources/data-source=";
        script = script.concat(datasourceAS7.getPoolName()+":add(");
        script = tmpMethod(script, "jndi-name", datasourceAS7.getJndiName());
        //TODO:problem with parameter enabled. Not supported in CLI as parameter
        script = tmpMethod(script, ", enabled", datasourceAS7.getEnabled());
        script = tmpMethod(script, ", use-java-context", datasourceAS7.getUseJavaContext());
        script = tmpMethod(script, ", driver-name", datasourceAS7.getDriver());
        script = tmpMethod(script, ", connection-url", datasourceAS7.getConnectionUrl());
        script = tmpMethod(script, ", url-delimeter", datasourceAS7.getUrlDelimeter());
        script = tmpMethod(script, ", url-selector-strategy-class-name", datasourceAS7.getUrlSelector());
        script = tmpMethod(script, ", transaction-isolation", datasourceAS7.getTransactionIsolation());
        script = tmpMethod(script, ", new-connection-sql", datasourceAS7.getNewConnectionSql());
        script = tmpMethod(script, ", prefill", datasourceAS7.getPrefill());
        script = tmpMethod(script, ", min-pool-size", datasourceAS7.getMinPoolSize());
        script = tmpMethod(script, ", max-pool-size", datasourceAS7.getMaxPoolSize());
        script = tmpMethod(script, ", password", datasourceAS7.getPassword());
        script = tmpMethod(script, ", user-name", datasourceAS7.getUserName());
        script = tmpMethod(script, ", security-domain", datasourceAS7.getSecurityDomain());
        script = tmpMethod(script, ", check-valid-connection-sql", datasourceAS7.getCheckValidConnectionSql());
        script = tmpMethod(script, ", validate-on-match", datasourceAS7.getValidateOnMatch());
        script = tmpMethod(script, ", background-validation", datasourceAS7.getBackgroundValidation());
        script = tmpMethod(script, ", background-validation-minutes", datasourceAS7.getBackgroundValidationMinutes());
        script = tmpMethod(script, ", use-fast-fail", datasourceAS7.getUseFastFail());
        script = tmpMethod(script, ", exception-sorter-class-name", datasourceAS7.getExceptionSorter());
        script = tmpMethod(script, ", valid-connection-checker-class-name", datasourceAS7.getValidateOnMatch());
        script = tmpMethod(script, ", stale-connection-checker-class-name", datasourceAS7.getStaleConnectionChecker());
        script = tmpMethod(script, ", blocking-timeout-millis", datasourceAS7.getBlockingTimeoutMillis());
        script = tmpMethod(script, ", idle-timeout-minutes", datasourceAS7.getIdleTimeoutMinutes());
        script = tmpMethod(script, ", set-tx-query-timeout", datasourceAS7.getSetTxQueryTimeout());
        script = tmpMethod(script, ", query-timeout", datasourceAS7.getQueryTimeout());
        script = tmpMethod(script, ", allocation-retry", datasourceAS7.getAllocationRetry());
        script = tmpMethod(script, ", allocation-retry-wait-millis", datasourceAS7.getAllocationRetryWaitMillis());
        script = tmpMethod(script, ", use-try-lock", datasourceAS7.getUseTryLock());
        script = tmpMethod(script, ", prepared-statement-cache-size", datasourceAS7.getPreparedStatementCacheSize());
        script = tmpMethod(script, ", track-statements", datasourceAS7.getTrackStatements());
        script = tmpMethod(script, ", share-prepared-statements", datasourceAS7.getSharePreparedStatements());
        script = script.concat(")");
        return script;


    }

    @Override
    public String createXaDatasourceScript(XaDatasourceAS7 xaDatasourceAS7) throws  CliScriptException{
        if(xaDatasourceAS7.getPoolName() == null){
            throw new CliScriptException("Error: pool-name cannot be null", new NullPointerException());
        }
        if(xaDatasourceAS7.getJndiName() == null){
            throw new CliScriptException("Error: jndi-name cannot be null", new NullPointerException());
        }
        if(xaDatasourceAS7.getDriver() == null){
            throw new CliScriptException("Error: driver-name cannot be null", new NullPointerException());
        }
        String script= "/subsystem=datasources/xa-data-source=";
        script = script.concat(xaDatasourceAS7.getPoolName()+":add(");
        script = tmpMethod(script, "jndi-name", xaDatasourceAS7.getJndiName());
        //TODO:problem with parameter enabled. Not supported in CLI as parameter
        script = tmpMethod(script, ", enabled", xaDatasourceAS7.getEnabled());
        script = tmpMethod(script, ", use-java-context", xaDatasourceAS7.getUseJavaContext());
        script = tmpMethod(script, ", driver-name", xaDatasourceAS7.getDriver());
        script = tmpMethod(script, ", url-delimeter", xaDatasourceAS7.getUrlDelimeter());
        script = tmpMethod(script, ", url-selector-strategy-class-name", xaDatasourceAS7.getUrlSelector());
        script = tmpMethod(script, ", transaction-isolation", xaDatasourceAS7.getTransactionIsolation());
        script = tmpMethod(script, ", new-connection-sql", xaDatasourceAS7.getNewConnectionSql());
        script = tmpMethod(script, ", prefill", xaDatasourceAS7.getPrefill());
        script = tmpMethod(script, ", min-pool-size", xaDatasourceAS7.getMinPoolSize());
        script = tmpMethod(script, ", max-pool-size", xaDatasourceAS7.getMaxPoolSize());
        script = tmpMethod(script, ", is-same-rm-override", xaDatasourceAS7.getSameRmOverride());
        script = tmpMethod(script, ", interleaving", xaDatasourceAS7.getInterleaving());
        script = tmpMethod(script, ", no-tx-separate-pools", xaDatasourceAS7.getNoTxSeparatePools());
        script = tmpMethod(script, ", password", xaDatasourceAS7.getPassword());
        script = tmpMethod(script, ", user-name", xaDatasourceAS7.getUserName());
        script = tmpMethod(script, ", security-domain", xaDatasourceAS7.getSecurityDomain());
        script = tmpMethod(script, ", check-valid-connection-sql", xaDatasourceAS7.getCheckValidConnectionSql());
        script = tmpMethod(script, ", validate-on-match", xaDatasourceAS7.getValidateOnMatch());
        script = tmpMethod(script, ", background-validation", xaDatasourceAS7.getBackgroundValidation());
        script = tmpMethod(script, ", background-validation-minutes", xaDatasourceAS7.getBackgroundValidationMinutes());
        script = tmpMethod(script, ", use-fast-fail", xaDatasourceAS7.getUseFastFail());
        script = tmpMethod(script, ", exception-sorter-class-name", xaDatasourceAS7.getExceptionSorter());
        script = tmpMethod(script, ", valid-connection-checker-class-name", xaDatasourceAS7.getValidateOnMatch());
        script = tmpMethod(script, ", stale-connection-checker-class-name", xaDatasourceAS7.getStaleConnectionChecker());
        script = tmpMethod(script, ", blocking-timeout-millis", xaDatasourceAS7.getBlockingTimeoutMillis());
        script = tmpMethod(script, ", idle-timeout-minutes", xaDatasourceAS7.getIdleTimeoutMinutes());
        script = tmpMethod(script, ", set-tx-query-timeout", xaDatasourceAS7.getSetTxQueryTimeout());
        script = tmpMethod(script, ", query-timeout", xaDatasourceAS7.getQueryTimeout());
        script = tmpMethod(script, ", allocation-retry", xaDatasourceAS7.getAllocationRetry());
        script = tmpMethod(script, ", allocation-retry-wait-millis", xaDatasourceAS7.getAllocationRetryWaitMillis());
        script = tmpMethod(script, ", use-try-lock", xaDatasourceAS7.getUseTryLock());
        script = tmpMethod(script, ", xa-resource-timeout", xaDatasourceAS7.getXaResourceTimeout());
        script = tmpMethod(script, ", prepared-statement-cache-size", xaDatasourceAS7.getPreparedStatementCacheSize());
        script = tmpMethod(script, ", track-statements", xaDatasourceAS7.getTrackStatements());
        script = tmpMethod(script, ", share-prepared-statements", xaDatasourceAS7.getSharePreparedStatements());
        script = script.concat(")\n");

       if(xaDatasourceAS7.getXaDatasourceProperties() != null){
           for(XaDatasourceProperty xaDatasourceProperty :xaDatasourceAS7.getXaDatasourceProperties()){
               script=script.concat("/subsystem=datasources/xa-data-source=" + xaDatasourceAS7.getPoolName() );
               script=script.concat("/xa-datasource-properties=" + xaDatasourceProperty.getXaDatasourcePropertyName());
               script=script.concat(":add(value=" + xaDatasourceProperty.getXaDatasourceProperty() + ")\n");

           }
       }
        return script;
    }

    @Override
    public String createResourceAdapterScript(ResourceAdapter resourceAdapter) throws CliScriptException{
        if(resourceAdapter.getJndiName() == null){
             throw new CliScriptException("Error: name of resource-adapter cannot be null", new NullPointerException());
        }
        if((resourceAdapter.getArchive() == null) || (resourceAdapter.getArchive().isEmpty())){
             throw new CliScriptException("Error: archive in resource-adapter cannot be null", new NullPointerException());
        }
        String script = "/subsystems=resource-adapters/resource-adapter=";
        script = script.concat(resourceAdapter.getJndiName() + ":add(");
        script = script.concat("archive=" + resourceAdapter.getArchive());
        script = tmpMethod(script, ", transaction-support", resourceAdapter.getTransactionSupport());
        script = script.concat(")\n");
        if(resourceAdapter.getConnectionDefinitions() != null){
            for(ConnectionDefinition connectionDefinition : resourceAdapter.getConnectionDefinitions()){
                if(connectionDefinition.getClassName() == null){
                     throw new CliScriptException("Error: class-name in connection definition cannot be null ",
                             new NullPointerException());
                }
                script =  script.concat("/subsystems=resource-adapters/resource-adapter=" + resourceAdapter.getJndiName());
                script = script.concat("/connection-definitions=" + connectionDefinition.getPoolName() + ":add(");
                script = tmpMethod(script, " jndi-name", connectionDefinition.getJndiName());
                script = tmpMethod(script, ", enabled", connectionDefinition.getEnabled());
                script = tmpMethod(script, ", use-java-context", connectionDefinition.getUseJavaContext());
                script = tmpMethod(script, ", class-name", connectionDefinition.getClassName());
                script = tmpMethod(script, ", use-ccm", connectionDefinition.getUseCcm());
                script = tmpMethod(script, ", prefill", connectionDefinition.getPrefill());
                script = tmpMethod(script, ", use-strict-min", connectionDefinition.getUseStrictMin());
                script = tmpMethod(script, ", flush-strategy", connectionDefinition.getFlushStrategy());
                script = tmpMethod(script, ", min-pool-size", connectionDefinition.getMinPoolSize());
                script = tmpMethod(script, ", max-pool-size", connectionDefinition.getMaxPoolSize());
                if(connectionDefinition.getSecurityDomain() != null){
                    script = tmpMethod(script, ", security-domain", connectionDefinition.getSecurityDomain());
                }
                if(connectionDefinition.getSecurityDomainAndApp() != null){
                    script = tmpMethod(script, ", security-domain-and-application",
                            connectionDefinition.getSecurityDomainAndApp());
                }
                if(connectionDefinition.getApplicationManagedSecurity() != null){
                    script = tmpMethod(script, ", application-managed-security",
                            connectionDefinition.getApplicationManagedSecurity());
                }
                script = tmpMethod(script, ", background-validation", connectionDefinition.getBackgroundValidation());
                script = tmpMethod(script, ", background-validation-millis", connectionDefinition.getBackgroundValidationMillis());
                script = tmpMethod(script, ", blocking-timeout-millis", connectionDefinition.getBackgroundValidationMillis());
                script = tmpMethod(script, ", idle-timeout-minutes", connectionDefinition.getIdleTimeoutMinutes());
                script = tmpMethod(script, ", allocation-retry", connectionDefinition.getAllocationRetry());
                script = tmpMethod(script, ", allocation-retry-wait-millis", connectionDefinition.getAllocationRetryWaitMillis());
                script = tmpMethod(script, ", xa-resource-timeout", connectionDefinition.getXaResourceTimeout());
                script = script.concat(")\n");
                if(connectionDefinition.getConfigProperties() != null){
                    for(ConfigProperty configProperty : connectionDefinition.getConfigProperties()){
                        script = script.concat("/subsystems=resource-adapters/resource-adapter=" + resourceAdapter.getJndiName());
                        script = script.concat("/connection-definitions=" + connectionDefinition.getPoolName());
                        script = script.concat("/config-properties=" + configProperty.getConfigPropertyName() + ":add(");
                        script = script.concat("value=" + configProperty.getConfigProperty() + ")\n");
                    }
                }
            }
        }
       return script;
    }

    @Override
    public String createLoggerScript(Logger logger) {
        String script = "/subsystems=logging/logger=" + logger.getLoggerCategory() + ":add(";
        script = tmpMethod(script, "level", logger.getLoggerLevelName());
        script = tmpMethod(script,", use-parent-handlers", logger.getUseParentHandlers());
        if(logger.getHandlers() != null){
            String handlers = "";
            for(String handler : logger.getHandlers()){
                  handlers = handlers.concat(",\"" + handler + "\"");
            }
            if(!handlers.isEmpty()){
                handlers = handlers.replaceFirst("\\,","");
                script = script.concat(", handlers=[" + handlers +"])");
            } else{
                script = script.concat(")");
            }
        } else {
           script = script.concat(")");
        }
        return script;

    }

    @Override
    public String createHandlersScript(LoggingAS7 loggingAS7) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String createSecurityDomainScript(SecurityDomain securityDomain) {
        if(securityDomain.getSecurityDomainName() == null){

        }
        String script = "/subsystems=security/security-domain=";
        script = script.concat(securityDomain.getSecurityDomainName() + ":add(");
        script = tmpMethod(script,"cache-type", securityDomain.getCacheType() + ")\n");
        if(securityDomain.getLoginModules() != null){
            for(LoginModuleAS7 loginModuleAS7 : securityDomain.getLoginModules()){
               script = script.concat("/subsystems=security/security-domain=" + securityDomain.getSecurityDomainName());
                script = script.concat("/authentication=classic:add(login-modules=[{");
                script = tmpMethod(script, "\"code\"", "\"" + loginModuleAS7.getLoginModuleCode() + "\"");
                script = tmpMethod(script, ", \"flag\"", "\"" + loginModuleAS7.getLoginModuleFlag() + "\"" );

                if((loginModuleAS7.getModuleOptions() != null) || !loginModuleAS7.getModuleOptions().isEmpty()){
                    String modules= "";
                    for(ModuleOptionAS7 moduleOptionAS7 : loginModuleAS7.getModuleOptions()){
                        modules = modules.concat(", (\"" + moduleOptionAS7.getModuleOptionName() + "\"=");
                        modules = modules.concat("\"" + moduleOptionAS7.getModuleOptionValue() + "\")");
                    }
                    modules = modules.replaceFirst("\\,", "");
                    modules = modules.replaceFirst(" ", "");
                    if(!modules.isEmpty()){
                        script = script.concat(", \"module-option\"=[{" + modules + "}]");
                    }
                }

            }
        }
        script = script.concat("}])");
        return script;
    }

    @Override
    public String createConnectorScript(ConnectorAS7 connectorAS7) throws CliScriptException{
        if(connectorAS7.getScheme() == null){
           throw new CliScriptException("Error: scheme cannot be null", new NullPointerException()) ;
        }
        if(connectorAS7.getSocketBinding() == null){
            throw new CliScriptException("Error: socket-binding cannot be null", new NullPointerException()) ;
        }
        if(connectorAS7.getConnectorName() == null){
            throw new CliScriptException("Error: connector name be null", new NullPointerException()) ;
        }
        String script = "/subsystems=web/connector=";
        script = script.concat(connectorAS7.getConnectorName() + ":add(");
        script = tmpMethod(script, "socket-binding", connectorAS7.getSocketBinding());
        script = tmpMethod(script, ",enable-lookups", connectorAS7.getEnableLookups());
        script = tmpMethod(script, ", max-post-size", connectorAS7.getMaxPostSize());
        script = tmpMethod(script, ", max-save-post-size", connectorAS7.getMaxSavePostSize());
        script = tmpMethod(script, ", max-connections", connectorAS7.getMaxConnections());
        script = tmpMethod(script, ", protocol", connectorAS7.getProtocol());
        script = tmpMethod(script, ", proxy-name", connectorAS7.getProxyName());
        script = tmpMethod(script, ", proxy-port", connectorAS7.getProxyPort());
        script = tmpMethod(script, ", redirect-port", connectorAS7.getRedirectPort());
        script = tmpMethod(script, ", scheme", connectorAS7.getScheme());
        script = tmpMethod(script, ", secure", connectorAS7.getSecure());
        script = tmpMethod(script, ", enabled", connectorAS7.getEnabled());
        script = script.concat(")");
        if(connectorAS7.getScheme().equals("https"))  {
            script = script.concat("\n/subsystems=web/connector=" + connectorAS7.getConnectorName()
                      + "/ssl=configuration:add(");
            script = tmpMethod(script,"name", connectorAS7.getSslName());
            script = tmpMethod(script,", verify-client", connectorAS7.getVerifyClient());
            script = tmpMethod(script,", verify-depth", connectorAS7.getVerifyDepth());
            script = tmpMethod(script,", certificate-key-file", connectorAS7.getCertificateKeyFile());
            script = tmpMethod(script,", password", connectorAS7.getPassword());
            script = tmpMethod(script,", protocol", connectorAS7.getProtocol());
            script = tmpMethod(script,", ciphers", connectorAS7.getCiphers());
            script = tmpMethod(script,", key-alias", connectorAS7.getKeyAlias());
            script = tmpMethod(script,", ca-certificate-file", connectorAS7.getCaCertificateFile());
            script = tmpMethod(script,", session-cache-size", connectorAS7.getSessionCacheSize());
            script = tmpMethod(script,", session-timeout", connectorAS7.getSessionTimeout());
            script = script.concat(")");
        }
      return script;


    }

    @Override
    public String createVirtualServerScript(VirtualServer virtualServer) {
        String script = "/subsystems=web/virtual-server=";
        script = script.concat(virtualServer.getVirtualServerName() + ":add(");
        script = tmpMethod(script, "enable-welcome-root",virtualServer.getEnableWelcomeRoot());
        script = tmpMethod(script, "default-web-module", virtualServer.getDefaultWebModule());
        if(virtualServer.getAliasName() != null){
            String aliases = "";
            for(String alias : virtualServer.getAliasName()){
                aliases = aliases.concat(", \"" + alias+"\"");
            }
            aliases = aliases.replaceFirst("\\, ", "");

            if(!aliases.isEmpty()){
                script = script.concat(", alias=[" + aliases + "]");
            }
        }
        script = script.concat(")");
        return script;

    }

    @Override
    public String createSocketBinding(SocketBinding socketBinding) throws CliScriptException{
        if(socketBinding.getSocketPort() == null){
            throw new CliScriptException("Error: port of socket binding cannot be null", new NullPointerException());
        }
        if(socketBinding.getSocketName() == null){
            throw new CliScriptException("Error: name of socket binding cannot be null", new NullPointerException());
        }
        String script = "/socket-binding-group=standard-sockets/socket-binding=";
        script = script.concat(socketBinding.getSocketName() + ":add(");
        script = script.concat("port=" + socketBinding.getSocketPort());
        script = tmpMethod(script, ", interface", socketBinding.getSocketInterface());
        script = script.concat(")");
        return script;

    }


}
