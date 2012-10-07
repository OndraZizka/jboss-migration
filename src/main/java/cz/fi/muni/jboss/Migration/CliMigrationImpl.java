package cz.fi.muni.jboss.Migration;

import cz.fi.muni.jboss.Migration.ConnectionFactories.ConfigProperty;
import cz.fi.muni.jboss.Migration.ConnectionFactories.ConnectionDefinition;
import cz.fi.muni.jboss.Migration.ConnectionFactories.ResourceAdapter;
import cz.fi.muni.jboss.Migration.DataSources.*;
import cz.fi.muni.jboss.Migration.Logging.Logger;
import cz.fi.muni.jboss.Migration.Logging.LoggingAS7;
import cz.fi.muni.jboss.Migration.Security.SecurityDomain;
import cz.fi.muni.jboss.Migration.Server.ConnectorAS7;
import cz.fi.muni.jboss.Migration.Server.SocketBinding;
import cz.fi.muni.jboss.Migration.Server.VirtualServer;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;

import java.io.Closeable;
import java.net.InetAddress;
/**
 * It seems that this idea is pointless because some changes require restart of the server
 * So better way is to make file containing scripts for CLI which will run as batch when server is offline
 */

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 10/3/12
 * Time: 1:52 PM
 */
public class CliMigrationImpl implements CliMigration {
    @Override
    public void createDatasource(DatasourceAS7 datasourceAS7) {
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem","datasources");
        request.get(ClientConstants.OP_ADDR).add("data-source",datasourceAS7.getPoolName());

        //TODO:basic code for setting datasource, but there is problem with NullPointerException
//        request.get("jndi-name").set(datasourceAS7.getJndiName());
//        request.get("pool-name").set(datasourceAS7.getPoolName());
//        request.get("enabled").set(datasourceAS7.getEnabled());
//        request.get("use-java-context").set(datasourceAS7.getUseJavaContext());
//        request.get("url-delimeter").set(datasourceAS7.getUrlDelimeter());
//        request.get("url-selector-strategy-class-name").set(datasourceAS7.getUrlSelector());
//        request.get("connection-url").set(datasourceAS7.getConnectionUrl());
//        request.get("new-connection-sql").set(datasourceAS7.getNewConnectionSql());
//        request.get("driver-name").set(datasourceAS7.getDriver()) ;
//
//        request.get("prefill").set(datasourceAS7.getPrefill());
//        request.get("min-pool-size").set(datasourceAS7.getMinPoolSize());
//        request.get("max-pool-size").set(datasourceAS7.getMaxPoolSize());
//
//        request.get("password").set(datasourceAS7.getPassword());
//        request.get("user-name").set(datasourceAS7.getUserName());
//        request.get("security-domain").set(datasourceAS7.getSecurityDomain());
//
//        request.get("check-valid-connection-sql").set(datasourceAS7.getCheckValidConnectionSql());
//        request.get("validate-on-match").set(datasourceAS7.getValidateOnMatch());
//        request.get("background-validation").set(datasourceAS7.getBackgroundValidation());
//        request.get("background-validation-minutes").set(datasourceAS7.getBackgroundValidationMinutes());
//        request.get("use-fast-fail").set(datasourceAS7.getUseFastFail());
//        //in xml there exception-sorter element but here is with class-name
//        request.get("exception-sorter-class-name").set(datasourceAS7.getExceptionSorter());
//        //same problem
//        request.get("valid-connection-checker-class-name").set(datasourceAS7.getValidConnectionChecker());
//        request.get("stale-connection-checker-class-name").set(datasourceAS7.getStaleConnectionChecker());
//
//        request.get("blocking-timeout-millis").set(datasourceAS7.getBlockingTimeoutMillis());
//        request.get("idle-timeout-minutes").set(datasourceAS7.getIdleTimeoutMinutes());
//        request.get("set-tx-query-timeout").set(datasourceAS7.getSetTxQueryTimeout());
//        request.get("query-timeout").set(datasourceAS7.getQueryTimeout());
//        request.get("allocation-retry").set(datasourceAS7.getAllocationRetry());
//        request.get("allocation-retry-wait-millis").set(datasourceAS7.getAllocationRetryWaitMillis());
//        request.get("use-try-lock").set(datasourceAS7.getUseTryLock());
//        request.get("prepared-statement-cache-size").set(datasourceAS7.getPreparedStatementCacheSize());
//        request.get("track-statements").set(datasourceAS7.getTrackStatements());
//        request.get("share-prepared-statements").set(datasourceAS7.getSharePreparedStatements());

       //this is attempt for solving problem with NullPointer and many ifs
         updatingRequest(request, "jndi-name", datasourceAS7.getJndiName());
         updatingRequest(request, "pool-name", datasourceAS7.getPoolName());
         updatingRequest(request, "enabled", datasourceAS7.getEnabled());
         updatingRequest(request, "use-java-context", datasourceAS7.getUseJavaContext());

        //default for set to h2 for debugging
//         updatingRequest(request, "driver-name", datasourceAS7.getDriver());
        updatingRequest(request, "driver-name", "h2");
        updatingRequest(request, "connection-url", datasourceAS7.getConnectionUrl());
        updatingRequest(request,"url-delimeter",datasourceAS7.getUrlDelimeter());
        updatingRequest(request,"url-selector-strategy-class-name",datasourceAS7.getUrlSelector());
        updatingRequest(request,"transaction-isolation",datasourceAS7.getTransactionIsolation());
        updatingRequest(request,"new-connection-sql",datasourceAS7.getNewConnectionSql());
        updatingRequest(request,"prefill",datasourceAS7.getPrefill());
        updatingRequest(request,"min-pool-size",datasourceAS7.getMinPoolSize());
        updatingRequest(request,"max-pool-size",datasourceAS7.getMaxPoolSize());
        updatingRequest(request,"password",datasourceAS7.getPassword());
        updatingRequest(request,"user-name",datasourceAS7.getUserName());
        updatingRequest(request,"security-domain",datasourceAS7.getSecurityDomain());
        updatingRequest(request,"check-valid-connection-sql",datasourceAS7.getCheckValidConnectionSql());
        updatingRequest(request,"validate-on-match",datasourceAS7.getValidateOnMatch());
        updatingRequest(request,"background-validation",datasourceAS7.getBackgroundValidation());
        updatingRequest(request,"background-validation-minutes",datasourceAS7.getBackgroundValidationMinutes());
        updatingRequest(request,"use-fast-fail",datasourceAS7.getUseFastFail());
        updatingRequest(request,"exception-sorter-class-name",datasourceAS7.getExceptionSorter());
        updatingRequest(request,"valid-connection-checker-class-name",datasourceAS7.getValidConnectionChecker());
        updatingRequest(request,"stale-connection-checker-class-name",datasourceAS7.getStaleConnectionChecker());
        updatingRequest(request,"blocking-timeout-millis",datasourceAS7.getBlockingTimeoutMillis());
        updatingRequest(request,"idle-timeout-minutes",datasourceAS7.getIdleTimeoutMinutes());
        updatingRequest(request,"set-tx-query-timeout",datasourceAS7.getSetTxQueryTimeout());
        updatingRequest(request,"query-timeout",datasourceAS7.getQueryTimeout());
        updatingRequest(request,"allocation-retry",datasourceAS7.getAllocationRetry());
        updatingRequest(request,"allocation-retry-wait-millis",datasourceAS7.getAllocationRetryWaitMillis());
        updatingRequest(request,"use-try-lock",datasourceAS7.getUseTryLock());
        updatingRequest(request,"prepared-statement-cache-size",datasourceAS7.getPreparedStatementCacheSize());
        updatingRequest(request,"track-statements",datasourceAS7.getTrackStatements());
        updatingRequest(request,"share-prepared-statements",datasourceAS7.getSharePreparedStatements());

        try {
            executeRequest(request);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        if(datasourceAS7.getConnectionProperties() != null){
            for(ConnectionProperty connectionProperty : datasourceAS7.getConnectionProperties()){
                ModelNode connectionProperties = new ModelNode();
                connectionProperties.get(ClientConstants.OP).set(ClientConstants.ADD);
                connectionProperties.get(ClientConstants.OP_ADDR).add("subsystem","datasources");
                connectionProperties.get(ClientConstants.OP_ADDR).add("data-source",datasourceAS7.getPoolName());
                if(connectionProperty.getConnectionPropertyName() != null){
                    connectionProperties.get(ClientConstants.OP_ADDR).add("connection-properties",connectionProperty.getConnectionPropertyName());
                    connectionProperties.get("value").set(connectionProperty.getConnectionProperty());
                    try {
                        executeRequest(connectionProperties);
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                }
            }
        }
    }

    private ModelNode updatingRequest(ModelNode request, String getter, String setter){
        if(setter != null){
            if(!setter.isEmpty()){
                request.get(getter).set(setter);
            }
        }
        return request;
    }


    @Override
    public void createXaDatasource(XaDatasourceAS7 xaDatasourceAS7) {
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem","datasources");
        request.get(ClientConstants.OP_ADDR).add("xa-data-source",xaDatasourceAS7.getPoolName());
        updatingRequest(request, "jndi-name", xaDatasourceAS7.getJndiName());
        updatingRequest(request, "enabled", xaDatasourceAS7.getEnabled());

        updatingRequest(request, "use-java-context", xaDatasourceAS7.getUseJavaContext());

        //default for set to h2 for debugging
//         updatingRequest(request, "driver-name", datasourceAS7.getDriver());
        updatingRequest(request, "driver-name", "h2");
//        updatingRequest(request, "connection-url", xaDatasourceAS7.getConnectionUrl());
        updatingRequest(request,"url-delimeter",xaDatasourceAS7.getUrlDelimeter());
        updatingRequest(request,"url-selector-strategy-class-name",xaDatasourceAS7.getUrlSelector());
        updatingRequest(request,"transaction-isolation",xaDatasourceAS7.getTransactionIsolation());
        updatingRequest(request,"new-connection-sql",xaDatasourceAS7.getNewConnectionSql());
        updatingRequest(request,"prefill",xaDatasourceAS7.getPrefill());
        updatingRequest(request,"min-pool-size",xaDatasourceAS7.getMinPoolSize());
        updatingRequest(request,"max-pool-size",xaDatasourceAS7.getMaxPoolSize());
        updatingRequest(request,"is-same-rm-override", xaDatasourceAS7.getSameRmOverride());
        updatingRequest(request,"interleaving",xaDatasourceAS7.getInterleaving());
        updatingRequest(request,"no-tx-separate-pools",xaDatasourceAS7.getNoTxSeparatePools());

        updatingRequest(request,"password",xaDatasourceAS7.getPassword());
        updatingRequest(request,"user-name",xaDatasourceAS7.getUserName());
        updatingRequest(request,"security-domain",xaDatasourceAS7.getSecurityDomain());
        updatingRequest(request,"check-valid-connection-sql",xaDatasourceAS7.getCheckValidConnectionSql());
        updatingRequest(request,"validate-on-match",xaDatasourceAS7.getValidateOnMatch());
        updatingRequest(request,"background-validation",xaDatasourceAS7.getBackgroundValidation());
        updatingRequest(request,"background-validation-minutes",xaDatasourceAS7.getBackgroundValidationMinutes());
        updatingRequest(request,"use-fast-fail",xaDatasourceAS7.getUseFastFail());
        updatingRequest(request,"exception-sorter-class-name",xaDatasourceAS7.getExceptionSorter());
        updatingRequest(request,"valid-connection-checker-class-name",xaDatasourceAS7.getValidConnectionChecker());
        updatingRequest(request,"stale-connection-checker-class-name",xaDatasourceAS7.getStaleConnectionChecker());
        updatingRequest(request,"blocking-timeout-millis",xaDatasourceAS7.getBlockingTimeoutMillis());
        updatingRequest(request,"idle-timeout-minutes",xaDatasourceAS7.getIdleTimeoutMinutes());
        updatingRequest(request,"set-tx-query-timeout",xaDatasourceAS7.getSetTxQueryTimeout());
        updatingRequest(request,"query-timeout",xaDatasourceAS7.getQueryTimeout());
        updatingRequest(request,"allocation-retry",xaDatasourceAS7.getAllocationRetry());
        updatingRequest(request,"allocation-retry-wait-millis",xaDatasourceAS7.getAllocationRetryWaitMillis());
        updatingRequest(request,"use-try-lock",xaDatasourceAS7.getUseTryLock());
        updatingRequest(request,"xa-resource-timeout",xaDatasourceAS7.getXaResourceTimeout());
        updatingRequest(request,"prepared-statement-cache-size",xaDatasourceAS7.getPreparedStatementCacheSize());
        updatingRequest(request,"track-statements",xaDatasourceAS7.getTrackStatements());
        updatingRequest(request,"share-prepared-statements",xaDatasourceAS7.getSharePreparedStatements());
        try {
            executeRequest(request);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        if(xaDatasourceAS7.getXaDatasourceProperties() != null){
            for(XaDatasourceProperty xaDatasourceProperty : xaDatasourceAS7.getXaDatasourceProperties()){
                ModelNode connectionProperties = new ModelNode();
                connectionProperties.get(ClientConstants.OP).set(ClientConstants.ADD);
                connectionProperties.get(ClientConstants.OP_ADDR).add("subsystem","datasources");
                connectionProperties.get(ClientConstants.OP_ADDR).add("xa-data-source",xaDatasourceAS7.getPoolName());
                if(xaDatasourceProperty.getXaDatasourcePropertyName() != null){
                    connectionProperties.get(ClientConstants.OP_ADDR).add
                            ("xa-datasource-properties", xaDatasourceProperty.getXaDatasourcePropertyName());
                    connectionProperties.get("value").set(xaDatasourceProperty.getXaDatasourceProperty());
                    try {
                        executeRequest(connectionProperties);
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                }
            }
        }
    }

    @Override
    public void createDriver(Driver driver) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void createResourceAdapters(ResourceAdapter resourceAdapter) {
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem","resource-adapters");
        request.get(ClientConstants.OP_ADDR).add("resource-adapter", resourceAdapter.getJndiName());
        updatingRequest(request, "archive", resourceAdapter.getArchive());
        updatingRequest(request, "transaction-support", resourceAdapter.getTransactionSupport());
        try {
            executeRequest(request);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        for(ConnectionDefinition connectionDefinition : resourceAdapter.getConnectionDefinitions()){
            ModelNode connection = new ModelNode();
            connection.get(ClientConstants.OP).set(ClientConstants.ADD);
            connection.get(ClientConstants.OP_ADDR).add("subsystem","resource-adapters");
            connection.get(ClientConstants.OP_ADDR).add("resource-adapter", resourceAdapter.getJndiName());
            connection.get(ClientConstants.OP_ADDR).add("connection-definitions",connectionDefinition.getPoolName());
            updatingRequest(connection, "jndi-name", connectionDefinition.getJndiName());
            updatingRequest(connection, "enabled", connectionDefinition.getEnabled());

            updatingRequest(connection, "use-java-context",connectionDefinition.getUseJavaContext());
            updatingRequest(connection, "class-name", connectionDefinition.getClassName() );
            updatingRequest(connection, "use-ccm", connectionDefinition.getUseCcm() );
            updatingRequest(connection, "prefill", connectionDefinition.getPrefill() );
            updatingRequest(connection, "use-strcit-min", connectionDefinition.getUseStrictMin() );
            updatingRequest(connection, "flush-strategy", connectionDefinition.getFlushStrategy() );
            updatingRequest(connection, "min-pool-size", connectionDefinition.getMinPoolSize() );
            updatingRequest(connection, "max-pool-size", connectionDefinition.getMaxPoolSize() );
            if(connectionDefinition.getSecurityDomain() != null){
                updatingRequest(connection, "security-domain", connectionDefinition.getSecurityDomain() );
            }
            if(connectionDefinition.getSecurityDomainAndApp() != null){
                updatingRequest(connection, "security-domain-and-application",
                        connectionDefinition.getSecurityDomainAndApp() );
            }
            if(connectionDefinition.getApplicationManagedSecurity() != null){
                updatingRequest(connection, "application-managed-security",
                        connectionDefinition.getApplicationManagedSecurity() );
            }


            updatingRequest(connection, "background-validation", connectionDefinition.getBackgroundValidation());
            updatingRequest(connection, "background-validation-millis", connectionDefinition.getBackgroundValidationMillis());
            updatingRequest(connection, "blocking-timeout-millis", connectionDefinition.getBlockingTimeoutMillis());
            updatingRequest(connection, "idle-timeout-minutes", connectionDefinition.getIdleTimeoutMinutes());
            updatingRequest(connection, "allocation-retry", connectionDefinition.getAllocationRetry() );
            updatingRequest(connection, "allocation-retry-wait-millis", connectionDefinition.getAllocationRetryWaitMillis() );
            updatingRequest(connection, "xa-resource-timeout", connectionDefinition.getXaResourceTimeout() );
            try {
                executeRequest(connection);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            for(ConfigProperty configProperty : connectionDefinition.getConfigProperties()){
                ModelNode config = new ModelNode();
                config.get(ClientConstants.OP).set(ClientConstants.ADD);
                config.get(ClientConstants.OP_ADDR).add("subsystem","resource-adapters");
                config.get(ClientConstants.OP_ADDR).add("resource-adapter", resourceAdapter.getJndiName());
                config.get(ClientConstants.OP_ADDR).add("connection-definitions",connectionDefinition.getPoolName());
                config.get(ClientConstants.OP_ADDR).add("config-properties",configProperty.getConfigPropertyName());
                updatingRequest(config,"value", configProperty.getConfigProperty());
                try {
                    executeRequest(config);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }

        }

    }

    @Override
    public void createHandlers(LoggingAS7 loggingAS7) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void createLogger(Logger logger) {
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem","logging");
        request.get(ClientConstants.OP_ADDR).add("logger",logger.getLoggerCategory());
        updatingRequest(request,"level",logger.getLoggerLevelName());
        updatingRequest(request, "use-parent-handlers",logger.getUseParentHandlers());

       // updatingRequest(request,"handlers", "\"test\",\"skuska\"");
        //request.get("handlers").add("[CONSOLE,FILE]");


        try {
            executeRequest(request);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
//        for(String handler : logger.getHandlers()){
//            ModelNode request2 = new ModelNode();
//            request2.get(ClientConstants.OP).set(ClientConstants.ADD);
//            request2.get(ClientConstants.OP_ADDR).add("subsystem","logging");
//            request2.get(ClientConstants.OP_ADDR).add("logger",logger.getLoggerCategory());
//            request2.get(ClientConstants.OP_ADDR).add("handlers",);
////            updatingRequest(request2, "name",handler);
//            try {
//                executeRequest(request2);
//            } catch (Exception e) {
//                System.out.println(e.toString());
//            }
//        }

    }

    @Override
    public void createSecurityDomain(SecurityDomain securityDomain) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void createConnector(ConnectorAS7 connectorAS7) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void createVirtualServer(VirtualServer virtualServer) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void createSocketBinding(SocketBinding socketBinding) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public static void executeRequest(ModelNode request) throws Exception{
        ModelControllerClient client = null;
        try {
            client = ModelControllerClient.Factory.create(InetAddress.getByName("127.0.0.1"), 9999);
            final ModelNode response = client.execute(new OperationBuilder(request).build());
            reportFailure(response);
        } finally {
            safeClose(client);
        }
    }


    public static void safeClose(final Closeable closeable) {
        if (closeable != null) try {
            closeable.close();
        } catch (Exception e) {
            // no-op
        }
    }
    private static void reportFailure(final ModelNode node) {
        if (!node.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {
            final String msg;
            if (node.hasDefined(ClientConstants.FAILURE_DESCRIPTION)) {
                if (node.hasDefined(ClientConstants.OP)) {
                    msg = String.format("Operation '%s' at address '%s' failed: %s", node.get(ClientConstants.OP), node.get(ClientConstants.OP_ADDR), node.get(ClientConstants.FAILURE_DESCRIPTION));
                } else {
                    msg = String.format("Operation failed: %s", node.get(ClientConstants.FAILURE_DESCRIPTION));
                }
            } else {
                msg = String.format("Operation failed: %s", node);
            }
            throw new RuntimeException(msg);
        }
    }
}
