package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.connectionFactories.*;
import cz.muni.fi.jboss.migration.dataSources.*;
import cz.muni.fi.jboss.migration.logging.*;
import cz.muni.fi.jboss.migration.security.*;
import cz.muni.fi.jboss.migration.server.*;
import org.apache.commons.lang.StringUtils;


import java.util.*;

/**
 * @author Roman Jakubco
 * Date: 8/28/12
 * Time: 5:59 PM
 */
public class MigrationImpl implements Migration {


    private Integer randomSocket = 1;
    private Integer randomConnector =1 ;
    private SocketBindingGroup socketBindingGroup;
    private Set<String> drivers = new HashSet();
    private Set<String> xaDatasourceClasses = new HashSet();
    private Set<CopyMemory> copyMemories = new HashSet();
    Set<SocketBinding> socketTemp = new HashSet();
    private boolean copy;

    MigrationImpl(){
        this.copy = false;
    }

    MigrationImpl(boolean copy){
        this.copy = copy;
    }

    private void createDefaultSockets(){
        /*
        <socket-binding name="ajp" port="8009"/>
     <socket-binding name="http" port="8080"/>
     <socket-binding name="https" port="8443"/>
     <socket-binding name="remoting" port="4447"/>
     <socket-binding name="txn-recovery-environment" port="4712"/>
     <socket-binding name="txn-status-manager" port="4713"/>
         */

        SocketBinding sb1 = new SocketBinding();
        sb1.setSocketName("ajp");
        sb1.setSocketPort("8009");
        socketTemp.add(sb1);

        SocketBinding sb2 = new SocketBinding();
        sb2.setSocketName("http");
        sb2.setSocketPort("8080");
        socketTemp.add(sb2);

        SocketBinding sb3 = new SocketBinding();
        sb3.setSocketName("https");
        sb3.setSocketPort("8443");
        socketTemp.add(sb3);

        SocketBinding sb4 = new SocketBinding();
        sb4.setSocketName("remoting");
        sb4.setSocketPort("4712");
        socketTemp.add(sb4);
    }

    @Override
    public SocketBindingGroup getSocketBindingGroup() {
        return socketBindingGroup;
    }
    @Override
    public Set<CopyMemory> getCopyMemories() {
        return copyMemories;
    }

    // TODO: Security-Domain must reference something what exists in subsystem security...
    @Override
    public Collection<DatasourceAS7> datasourceMigration(Collection<DatasourceAS5> datasources) {
        Set<DatasourceAS7> datasourceAS7Collection = new HashSet();

        for (DatasourceAS5 datasourceAS5 : datasources) {
            DatasourceAS7 datasourceAS7 = new DatasourceAS7();

            drivers.add(datasourceAS5.getDriverClass());

            // Standalone elements in AS7
            datasourceAS7.setJndiName("java:jboss/datasources/" + datasourceAS5.getJndiName());
            datasourceAS7.setPoolName(datasourceAS5.getJndiName());
            datasourceAS7.setEnabled("true");
            datasourceAS7.setUseJavaContext(datasourceAS5.getUseJavaContext());
            datasourceAS7.setUrlDelimeter(datasourceAS5.getUrlDelimeter());
            datasourceAS7.setUrlSelector(datasourceAS5.getUrlSelectStratClName());
            datasourceAS7.setConnectionUrl(datasourceAS5.getConnectionUrl());

            if(datasourceAS5.getConnectionProperties() != null){
                datasourceAS7.setConnectionProperties(datasourceAS5.getConnectionProperties());
            }

            datasourceAS7.setTransIsolation(datasourceAS5.getTransIsolation());
            datasourceAS7.setNewConnectionSql(datasourceAS5.getNewConnectionSql());

            // TODO: First implementation.
            datasourceAS7.setDriver(StringUtils.substringAfter(datasourceAS5.getDriverClass(), "."));

            // Elements in element <security> in AS7
            datasourceAS7.setUserName(datasourceAS5.getUserName());
            datasourceAS7.setPassword(datasourceAS5.getPassword());
            // TODO: Some problems with elements in AS5(security-domain/application-managed-security/security-domain-and-application)
            datasourceAS7.setSecurityDomain(datasourceAS5.getSecurityDomain());

            // Elements in element <pool> in AS7
            datasourceAS7.setMinPoolSize(datasourceAS5.getMinPoolSize());
            datasourceAS7.setMaxPoolSize(datasourceAS5.getMaxPoolSize());
            datasourceAS7.setPrefill(datasourceAS5.getPrefill());

            // Elements in element <timeout> in AS7
            datasourceAS7.setBlockingTimeoutMillis(datasourceAS5.getBlockingTimeMillis());
            datasourceAS7.setIdleTimeoutMin(datasourceAS5.getIdleTimeoutMinutes());
            datasourceAS7.setQueryTimeout(datasourceAS5.getQueryTimeout());
            datasourceAS7.setAllocationRetry(datasourceAS5.getAllocationRetry());
            datasourceAS7.setAllocRetryWaitMillis(datasourceAS5.getAllocRetryWaitMillis());
            datasourceAS7.setSetTxQueryTimeout(datasourceAS5.getSetTxQueryTime());
            datasourceAS7.setUseTryLock(datasourceAS5.getUseTryLock());

            // Elements in element <validation> in AS7
            datasourceAS7.setCheckValidConSql(datasourceAS5.getCheckValidConSql());
            datasourceAS7.setValidateOnMatch(datasourceAS5.getValidateOnMatch());
            datasourceAS7.setBackgroundValid(datasourceAS5.getBackgroundValid());
            datasourceAS7.setExceptionSorter(datasourceAS5.getExcepSorterClName());
            datasourceAS7.setValidConChecker(datasourceAS5.getValidConCheckerClName());
            datasourceAS7.setStaleConChecker(datasourceAS5.getStaleConCheckerClName());
            // Millis represents Milliseconds?
            if (datasourceAS5.getBackgroundValidMillis() != null) {
                Integer tmp = Integer.valueOf(datasourceAS5.getBackgroundValidMillis())/ 60000;
                datasourceAS7.setBackgroundValidMin(tmp.toString());

            }

            // Elements in element <statement> in AS7
            datasourceAS7.setTrackStatements(datasourceAS5.getTrackStatements());
            datasourceAS7.setSharePreStatements(datasourceAS5.getSharePreStatements());
            datasourceAS7.setQueryTimeout(datasourceAS5.getQueryTimeout());

            // Strange element use-fast-fail
            //datasourceAS7.setUseFastFail(datasourceAS5.gF);

            datasourceAS7Collection.add(datasourceAS7);
        }

        return datasourceAS7Collection;
    }

    @Override
    public Collection<XaDatasourceAS7> xaDatasourceMigration(Collection<XaDatasourceAS5> datasources) {
        Set<XaDatasourceAS7> xaDatasourceAS7Collection = new HashSet();

        for (XaDatasourceAS5 xaDataAS5 : datasources) {
            XaDatasourceAS7 xaDataAS7 = new XaDatasourceAS7();

            xaDataAS7.setJndiName("java:jboss/datasources/" + xaDataAS5.getJndiName());
            xaDataAS7.setPoolName(xaDataAS5.getJndiName());
            xaDataAS7.setUseJavaContext(xaDataAS5.getUseJavaContext());
            xaDataAS7.setEnabled("true");


             // xa-datasource-class should be declared in drivers no in datasource.
            // xa-datasource then reference xa-datasource-class with element name
            //xaDatasourceAS7.setXaDatasourceClass(xaDatasourceAS5.getXaDatasourceClass());
            xaDatasourceClasses.add(xaDataAS5.getXaDatasourceClass());

            xaDataAS7.setDriver(StringUtils.substringAfter(xaDataAS5.getXaDatasourceClass(), "."));

            xaDataAS7.setXaDatasourceProps(xaDataAS5.getXaDatasourceProps());
            xaDataAS7.setUrlDelimeter(xaDataAS5.getUrlDelimeter());
            xaDataAS7.setUrlSelector(xaDataAS5.getUrlSelectorStratClName());
            xaDataAS7.setTransIsolation(xaDataAS5.getTransIsolation());
            xaDataAS7.setNewConnectionSql(xaDataAS5.getNewConnectionSql());

            // Elements in element <xa-pool> in AS7
            xaDataAS7.setMinPoolSize(xaDataAS5.getMinPoolSize());
            xaDataAS7.setMaxPoolSize(xaDataAS5.getMaxPoolSize());
            xaDataAS7.setPrefill(xaDataAS5.getPrefill());
            xaDataAS7.setSameRmOverride(xaDataAS5.getSameRM());
            xaDataAS7.setInterleaving(xaDataAS5.getInterleaving());
            xaDataAS7.setNoTxSeparatePools(xaDataAS5.getNoTxSeparatePools());

            // Elements in element <security> in AS7
            xaDataAS7.setUserName(xaDataAS5.getUserName());
            xaDataAS7.setPassword(xaDataAS5.getPassword());
            // TODO: Same problem as in datasourceMigration()
            xaDataAS7.setSecurityDomain(xaDataAS5.getSecurityDomain());

            // Elements in element <validation> in AS7
            xaDataAS7.setCheckValidConSql(xaDataAS5.getCheckValidConSql());
            xaDataAS7.setValidateOnMatch(xaDataAS5.getValidateOnMatch());
            xaDataAS7.setBackgroundValid(xaDataAS5.getBackgroundValid());
            xaDataAS7.setExceptionSorter(xaDataAS5.getExSorterClassName());
            xaDataAS7.setValidConChecker(xaDataAS5.getValidConCheckerClName());
            xaDataAS7.setStaleConChecker(xaDataAS5.getStaleConCheckerClName());
            // Millis represents Milliseconds?:p
            if (xaDataAS5.getBackgroundValidMillis() != null) {
                Integer tmp= Integer.valueOf(xaDataAS5.getBackgroundValidMillis()) / 60000;
                xaDataAS7.setBackgroundValidMin(tmp.toString());
            }

            // Elements in element <timeout> in AS7
            xaDataAS7.setBlockingTimeoutMillis(xaDataAS5.getBlockingTimeoutMillis());
            xaDataAS7.setIdleTimeoutMinutes(xaDataAS5.getIdleTimeoutMinutes());
            xaDataAS7.setQueryTimeout(xaDataAS5.getQueryTimeout());
            xaDataAS7.setAllocationRetry(xaDataAS5.getAllocationRetry());
            xaDataAS7.setAllocRetryWaitMillis(xaDataAS5.getAllocRetryWaitMillis());
            xaDataAS7.setSetTxQueryTimeout(xaDataAS5.getSetTxQueryTimeout());
            xaDataAS7.setUseTryLock(xaDataAS5.getUseTryLock());
            xaDataAS7.setXaResourceTimeout(xaDataAS5.getXaResourceTimeout());

            // Elements in element <statement> in AS7
            xaDataAS7.setPreStatementCacheSize(xaDataAS5.getPreStatementCacheSize());
            xaDataAS7.setTrackStatements(xaDataAS5.getTrackStatements());
            xaDataAS7.setSharePreStatements(xaDataAS5.getSharePreStatements());


            // Strange element use-fast-fail
            //datasourceAS7.setUseFastFail(datasourceAS5.gF);


            xaDatasourceAS7Collection.add(xaDataAS7);
        }

        return xaDatasourceAS7Collection;
    }




   // Resource-adapters .... maybe change name in the future?
    @Override
    public ResourceAdapter connectionFactoryMigration(ConnectionFactoryAS5 connFactoryAS5) {
        ResourceAdapter resAdapter = new ResourceAdapter();
        resAdapter.setJndiName(connFactoryAS5.getJndiName());

        CopyMemory copyMemory = new CopyMemory();
        copyMemory.setName(connFactoryAS5.getRarName());
        copyMemory.setType("resource");
        copyMemories.add(copyMemory);

        resAdapter.setArchive(connFactoryAS5.getRarName());

        // TODO: Not sure what exactly this element represents and what it is in AS5
        resAdapter.setTransactionSupport("XATransaction");

        ConnectionDefinition connDef = new ConnectionDefinition();
        connDef.setJndiName("java:jboss/" + connFactoryAS5.getJndiName());
        connDef.setPoolName(connFactoryAS5.getJndiName());
        connDef.setEnabled("true");
        connDef.setUseJavaCont("true");
        connDef.setEnabled("true");
        connDef.setClassName(connFactoryAS5.getConnectionDefinition());
        connDef.setPrefill(connFactoryAS5.getPrefill());

        for (ConfigProperty configProperty : connFactoryAS5.getConfigProperties()) {
            configProperty.setType(null);
        }
        connDef.setConfigProperties(connFactoryAS5.getConfigProperties());

        if (connFactoryAS5.getApplicationManagedSecurity() != null) {
            connDef.setAppManagedSec(connFactoryAS5.getApplicationManagedSecurity());
        }
        if (connFactoryAS5.getSecurityDomain() != null) {
            connDef.setSecurityDomain(connFactoryAS5.getSecurityDomain());
        }
        if (connFactoryAS5.getSecDomainAndApp() != null) {
            connDef.setSecDomainAndApp(connFactoryAS5.getSecDomainAndApp());
        }

        connDef.setMinPoolSize(connFactoryAS5.getMinPoolSize());
        connDef.setMaxPoolSize(connFactoryAS5.getMaxPoolSize());

        connDef.setBackgroundValidation(connFactoryAS5.getBackgroundValid());
        connDef.setBackgroundValiMillis(connFactoryAS5.getBackgroundValiMillis());

        connDef.setBlockingTimeoutMillis(connFactoryAS5.getBlockingTimeoutMillis());
        connDef.setIdleTimeoutMinutes(connFactoryAS5.getIdleTimeoutMin());
        connDef.setAllocationRetry(connFactoryAS5.getAllocationRetry());
        connDef.setAllocRetryWaitMillis(connFactoryAS5.getAllocRetryWaitMillis());
        connDef.setXaResourceTimeout(connFactoryAS5.getXaResourceTimeout());

        Set<ConnectionDefinition> connDefColl = new HashSet();
        connDefColl.add(connDef);
        resAdapter.setConnectionDefinitions(connDefColl);

        return resAdapter;
    }

    @Override
    public ResourceAdaptersSub resourceAdaptersMigration(Collection<ConnectionFactories> connFactories){
        if(connFactories.isEmpty()){
            return null;
        }

        ResourceAdaptersSub resAdaptersSub = new ResourceAdaptersSub();
        Set<ResourceAdapter> resourceAdapters = new HashSet();

        for(ConnectionFactories cf: connFactories){
           for(ConnectionFactoryAS5 connectionFactoryAS5 : cf.getConnectionFactories()){
                    resourceAdapters.add(connectionFactoryMigration(connectionFactoryAS5));
            }
        }

        resAdaptersSub.setResourceAdapters(resourceAdapters);

        return resAdaptersSub;
    }


    @Override
    public DatasourcesSub datasourceSubMigration(Collection<DataSources> dataSources) {
        DatasourcesSub datasourcesSub = new DatasourcesSub();
        Set<DatasourceAS7> ds7 = new HashSet();
        Set<XaDatasourceAS7> xaDS7 = new HashSet();
        Set<Driver> driverColl= new HashSet();
        Set<Driver> xaDataClassColl = new HashSet();

        for (DataSources ds : dataSources) {
            if(ds.getLocalDatasourceAS5s() != null){
                ds7.addAll(datasourceMigration(ds.getLocalDatasourceAS5s()));
            }
            if(ds.getXaDatasourceAS5s() != null){
                xaDS7.addAll(xaDatasourceMigration(ds.getXaDatasourceAS5s()));
            }
        }

        for(String driverClass : drivers){
            Driver driver = new Driver();
            driver.setDriverClass(driverClass);
            driver.setDriverName(StringUtils.substringAfter(driverClass, "."));

            // TODO: Problem with copy memory class and setting name for Driver so it can be find in server dir
            CopyMemory cp = new CopyMemory();
            cp.setName(StringUtils.substringAfter(driverClass, "."));
            cp.setType("driver");
            copyMemories.add(cp);
            driver.setDriverModule(cp.driverModuleGen());

            driverColl.add(driver);
        }

        for(String xaDsClass : xaDatasourceClasses){
            Driver driver = new Driver();
            driver.setXaDatasourceClass(xaDsClass);
            driver.setDriverName(StringUtils.substringAfter(xaDsClass, "."));

            // TODO: Problem with copy memory class and setting name for Driver so it can be find in server dir
            CopyMemory cp = new CopyMemory();
            cp.setName(StringUtils.substringAfter(xaDsClass, "."));
            cp.setType("driver");
            copyMemories.add(cp);
            driver.setDriverModule(cp.driverModuleGen());

            xaDataClassColl.add(driver);
        }

        datasourcesSub.setDatasource(ds7);
        datasourcesSub.setXaDatasource(xaDS7);
        datasourcesSub.setDrivers(driverColl);
        datasourcesSub.setXaDsClasses(xaDataClassColl);

        return datasourcesSub;
    }


    // TODO: First implementation of socket binding..
    public String createSocketBinding(String port, String name) {
        if(socketTemp.isEmpty()){
            createDefaultSockets();
        }
        SocketBinding socketBinding = new SocketBinding();
        Set<SocketBinding> socketBindings = new HashSet();

        for (SocketBinding sb : socketTemp) {
            if (sb.getSocketPort().equals(port)) {
                return sb.getSocketName();
            }
        }

        if (socketBindingGroup.getSocketBindings() == null) {
            socketBinding.setSocketName(name);
            socketBinding.setSocketPort(port);
            socketBindings.add(socketBinding);
            socketBindingGroup.setSocketBindings(socketBindings);
            return name;
        }

        for (SocketBinding sb : socketBindingGroup.getSocketBindings()) {
            if (sb.getSocketPort().equals(port)) {
                return sb.getSocketName();
            }
        }

        socketBinding.setSocketPort(port);

        for (SocketBinding sb : socketBindingGroup.getSocketBindings()) {
            if (sb.getSocketName().equals(name)) {
                name = name.concat(randomSocket.toString());
                randomSocket++;
            }
        }

        socketBinding.setSocketName(name);
        socketBindingGroup.getSocketBindings().add(socketBinding);

        return name;
    }



    @Override
    public ServerSub serverMigration(ServerAS5 serverAS5) {
        socketBindingGroup = new SocketBindingGroup();
        ServerSub serverSub = new ServerSub();
        Set<ConnectorAS7> subConnectors = new HashSet();
        Set<VirtualServer> virtualServers = new HashSet();

        for (Service service : serverAS5.getServices()) {
            for (ConnectorAS5 connector : service.getConnectorAS5s()) {
                ConnectorAS7 connAS7 = new ConnectorAS7();
                connAS7.setEnabled("true");
                connAS7.setEnableLookups(connector.getEnableLookups());
                connAS7.setMaxPostSize(connector.getMaxPostSize());
                connAS7.setMaxSavePostSize(connector.getMaxSavePostSize());
                connAS7.setProtocol(connector.getProtocol());
                connAS7.setProxyName(connector.getProxyName());
                connAS7.setProxyPort(connector.getProxyPort());
                connAS7.setRedirectPort(connector.getRedirectPort());

                // TODO: Getting error in AS7 when deploying ajp connector with empty scheme or without attribute.
                // TODO: Only solution is http?
                connAS7.setScheme("http");


                connAS7.setConnectorName("connector" + randomConnector);
                randomConnector++;

                // sSocket-binding.. first try
                if (connector.getProtocol().equals("HTTP/1.1")) {

                    if (connector.getSslEnabled() == null) {
                        connAS7.setSocketBinding(createSocketBinding(connector.getPort(), "http"));
                    } else {
                        if (connector.getSslEnabled().equals("true")) {
                            connAS7.setSocketBinding(createSocketBinding(connector.getPort(), "https"));
                        } else {
                            connAS7.setSocketBinding(createSocketBinding(connector.getPort(), "http"));
                        }
                    }
                } else {
                    connAS7.setSocketBinding(createSocketBinding(connector.getPort(), "ajp"));
                }


                if(connector.getSslEnabled() == null){

                } else{
                    if (connector.getSslEnabled().equals("true")) {
                        connAS7.setScheme("https");
                        connAS7.setSecure(connector.getSecure());

                        connAS7.setSslName("ssl");
                        connAS7.setVerifyClient(connector.getClientAuth());
                        // TODO: Problem with place of the file
                        connAS7.setCertifKeyFile(connector.getKeystoreFile());


                        // TODO: No sure which protocols can be in AS5
                        if (connector.getSslProtocol().equals("TLS")) {
                            connAS7.setSslProtocol("TLSv1");
                        }
                        connAS7.setSslProtocol(connector.getSslProtocol());

                        connAS7.setCiphers(connector.getCiphers());
                        connAS7.setKeyAlias(connAS7.getKeyAlias());


                        // TODO: Problem with passwords. Password in AS7 stores keystorePass and truststorePass(there are same)
                        connAS7.setPassword(connector.getKeystorePass());
                    }
                }

                subConnectors.add(connAS7);
            }

            VirtualServer virtualServer = new VirtualServer();
            virtualServer.setVirtualServerName(service.getEngineName());
            virtualServer.setEnableWelcomeRoot("true");
            virtualServer.setAliasName(service.getHostNames());

            virtualServers.add(virtualServer);
        }

        // TODO: Set first virtual server as default or it will be done by user?
        serverSub.setConnectors(subConnectors);
        serverSub.setVirtualServers(virtualServers);

        return serverSub;

    }

    @Override
    public LoggingAS7 loggingMigration(LoggingAS5 loggingAS5) {
        LoggingAS7 loggingAS7 = new LoggingAS7();
        Set<AsyncHandler> asyncHandlers = new HashSet<>();
        Set<SizeRotatingFileHandler> sizeRotFileHandlers = new HashSet();
        Set<FileHandler> fileHandlers = new HashSet();
        Set<PerRotFileHandler> perRotFileHandlers = new HashSet();
        Set<ConsoleHandler> consoleHandlers = new HashSet();
        Set<CustomHandler> customHandlers = new HashSet();

        for (Appender appender : loggingAS5.getAppenders()) {
            String type = appender.getAppenderClass();

            switch (StringUtils.substringAfterLast(type, ".")) {
                case "DailyRollingFileAppender": {
                    PerRotFileHandler periodic = new PerRotFileHandler();
                    periodic.setName(appender.getAppenderName());

                    for (Parameter parameter : appender.getParameters()) {
                        if (parameter.getParamName().equalsIgnoreCase("Append")) {
                            periodic.setAppend(parameter.getParamValue());
                            continue;
                        }

                        if (parameter.getParamName().equals("File")) {
                            String value = parameter.getParamValue();


                            periodic.setFileRelativeTo("jboss.server.log.dir");
                            periodic.setPath(StringUtils.substringAfterLast(value, "/"));

                            CopyMemory copyMemory = new CopyMemory();
                            copyMemory.setName(StringUtils.substringAfterLast(value, "/"));
                            copyMemory.setType("log");
                            copyMemories.add(copyMemory);
                        }

                        if (parameter.getParamName().equalsIgnoreCase("DatePattern")) {
                            // TODO: Basic for now. Don't know what to do with apostrophes
                            periodic.setSuffix(parameter.getParamValue());
                            continue;
                        }

                        if (parameter.getParamName().equalsIgnoreCase("Threshold")) {
                            periodic.setLevel(parameter.getParamValue());
                            continue;
                        }
                    }
                    periodic.setFormatter(appender.getLayoutParamValue());
                    perRotFileHandlers.add(periodic);
                }
                break;
                case "RollingFileAppender": {
                    SizeRotatingFileHandler size = new SizeRotatingFileHandler();
                    size.setName(appender.getAppenderName());

                    for (Parameter parameter : appender.getParameters()) {
                        if (parameter.getParamName().equalsIgnoreCase("Append")) {
                            size.setAppend(parameter.getParamValue());
                            continue;
                        }

                        if (parameter.getParamName().equals("File")) {
                            String value = parameter.getParamValue();

                            //TODO: Problem with bad parse? same thing in DailyRotating
                            size.setRelativeTo("jboss.server.log.dir");
                            size.setPath(StringUtils.substringAfterLast(value, "/"));

                            CopyMemory copyMemory = new CopyMemory();
                            copyMemory.setName(StringUtils.substringAfterLast(value, "/"));
                            copyMemory.setType("log");
                            copyMemories.add(copyMemory);
                        }

                        if (parameter.getParamName().equalsIgnoreCase("MaxFileSize")) {
                            size.setRotateSize(parameter.getParamValue());
                            continue;
                        }

                        if (parameter.getParamName().equalsIgnoreCase("MaxBackupIndex")) {
                            size.setMaxBackupIndex(parameter.getParamValue());
                            continue;
                        }

                        if (parameter.getParamName().equalsIgnoreCase("Threshold")) {
                            size.setLevel(parameter.getParamValue());
                            continue;
                        }
                    }

                    size.setFormatter(appender.getLayoutParamValue());
                    sizeRotFileHandlers.add(size);
                }
                break;
                case "ConsoleAppender": {
                    ConsoleHandler consoleHandler = new ConsoleHandler();
                    consoleHandler.setName(appender.getAppenderName());

                    for (Parameter parameter : appender.getParameters()) {
                        if (parameter.getParamName().equalsIgnoreCase("Target")) {
                            consoleHandler.setTarget(parameter.getParamValue());
                            continue;
                        }

                        if (parameter.getParamName().equalsIgnoreCase("Threshold")) {
                            consoleHandler.setLevel(parameter.getParamValue());
                            continue;
                        }
                    }

                    consoleHandler.setFormatter(appender.getLayoutParamValue());
                    consoleHandlers.add(consoleHandler);
                }
                break;
                case "AsyncAppender":{
                    AsyncHandler asyncHandler = new AsyncHandler();
                    asyncHandler.setName(appender.getAppenderName());
                    for (Parameter parameter : appender.getParameters()) {
                        if (parameter.getParamName().equalsIgnoreCase("BufferSize")) {
                            asyncHandler.setQueueLength(parameter.getParamValue());
                            continue;
                        }

                        if (parameter.getParamName().equalsIgnoreCase("Blocking")) {
                            asyncHandler.setOverflowAction(parameter.getParamValue());
                            continue;
                        }
                    }

                    Set<String> appendersRef = new HashSet();

                    for (String ref : appender.getAppenderRefs()) {
                        appendersRef.add(ref);
                    }

                    asyncHandler.setSubhandlers(appendersRef);
                    asyncHandler.setFormatter(appender.getLayoutParamValue());
                    asyncHandlers.add(asyncHandler);
                }
                break;
                // TODO: There is not such thing as FileAppender in AS5. Only sizeRotating or dailyRotating
                // TODO: So i think that FileAppender in AS7 is then useless?
                // THINK !!

                //case "FileAppender" :

                // Basic implementation of Custom Handler
                //TODO: Problem with module
                default: {
                    CustomHandler customHandler = new CustomHandler();
                    customHandler.setName(appender.getAppenderName());
                    customHandler.setClassValue(appender.getAppenderClass());
                    Set<Property> properties = new HashSet();

                    for (Parameter parameter : appender.getParameters()) {
                        if (parameter.getParamName().equalsIgnoreCase("Threshold")) {
                            customHandler.setLevel(parameter.getParamValue());
                            continue;
                        }

                        Property property = new Property();
                        property.setName(parameter.getParamName());
                        property.setValue(parameter.getParamValue());
                        properties.add(property);

                    }

                    customHandler.setProperties(properties);
                    customHandler.setFormatter(appender.getLayoutParamValue());
                    customHandlers.add(customHandler);
                }
                break;
            }
        }

        loggingAS7.setAsyncHandlers(asyncHandlers);
        loggingAS7.setConsoleHandlers(consoleHandlers);
        loggingAS7.setCustomHandlers(customHandlers);
        loggingAS7.setPerRotFileHandlers(perRotFileHandlers);
        loggingAS7.setSizeRotFileHandlers(sizeRotFileHandlers);

        Set<Logger> loggers = new HashSet();

        for (Category category : loggingAS5.getCategories()) {
            Logger logger = new Logger();
            logger.setLoggerCategory(category.getCategoryName());
            logger.setLoggerLevelName(category.getCategoryValue());
            logger.setHandlers(category.getAppenderRef());
            loggers.add(logger);
        }

        loggingAS7.setLoggers(loggers);
        /*
        TODO: Problem with level, because there is relative path in AS:<priority value="${jboss.server.log.threshold}"/>
           for now only default INFO
         */
        loggingAS7.setRootLoggerLevel("INFO");
        loggingAS7.setRootLoggerHandlers(loggingAS5.getRootAppenderRefs());

        return loggingAS7;
    }

    @Override
    public SecurityAS7 securityMigration(SecurityAS5 securityAS5) {
        SecurityAS7 securityAS7 = new SecurityAS7();
        Set<SecurityDomain> securityDomains = new HashSet();

        for (ApplicationPolicy applicationPolicy : securityAS5.getApplicationPolicies()) {
            Set<LoginModuleAS7> loginModules = new HashSet<>();
            SecurityDomain securityDomain = new SecurityDomain();
            securityDomain.setSecurityDomainName(applicationPolicy.getApplicationPolicyName());
            securityDomain.setCacheType("default");

            for (LoginModuleAS5 lmAS5 : applicationPolicy.getLoginModules()) {
                Set<ModuleOptionAS7> moduleOptions = new HashSet();
                LoginModuleAS7 lmAS7 = new LoginModuleAS7();
                lmAS7.setLoginModuleFlag(lmAS5.getLoginModuleFlag());

                switch (StringUtils.substringAfterLast(lmAS5.getLoginModule(), ".")) {
                    case "ClientLoginModule":
                        lmAS7.setLoginModuleCode("Client");
                        break;
                    //*
                    case "BaseCertLoginModule":
                        lmAS7.setLoginModuleCode("Certificate");
                        break;
                    case "CertRolesLoginModule":
                        lmAS7.setLoginModuleCode("CertificateRoles");
                        break;
                    //*
                    case "DatabaseServerLoginModule":
                        lmAS7.setLoginModuleCode("Database");
                        break;
                    case "DatabaseCertLoginModule":
                        lmAS7.setLoginModuleCode("DatabaseCertificate");
                        break;
                    case "IdentityLoginModule":
                        lmAS7.setLoginModuleCode("Identity");
                        break;
                    case "LdapLoginModule":
                        lmAS7.setLoginModuleCode("Ldap");
                        break;
                    case "LdapExtLoginModule":
                        lmAS7.setLoginModuleCode("LdapExtended");
                        break;
                    case "RoleMappingLoginModule":
                        lmAS7.setLoginModuleCode("RoleMapping");
                        break;
                    case "RunAsLoginModule":
                        lmAS7.setLoginModuleCode("RunAs");
                        break;
                    case "SimpleServerLoginModule":
                        lmAS7.setLoginModuleCode("Simple");
                        break;
                    case "ConfiguredIdentityLoginModule":
                        lmAS7.setLoginModuleCode("ConfiguredIdentity");
                        break;
                    case "SecureIdentityLoginModule":
                        lmAS7.setLoginModuleCode("SecureIdentity");
                        break;
                    case "PropertiesUsersLoginModule":
                        lmAS7.setLoginModuleCode("PropertiesUsers");
                        break;
                    case "SimpleUsersLoginModule":
                        lmAS7.setLoginModuleCode("SimpleUsers");
                        break;
                    case "LdapUsersLoginModule":
                        lmAS7.setLoginModuleCode("LdapUsers");
                        break;
                    case "Krb5loginModule":
                        lmAS7.setLoginModuleCode("Kerberos");
                        break;
                    case "SPNEGOLoginModule":
                        lmAS7.setLoginModuleCode("SPNEGOUsers");
                        break;
                    case "AdvancedLdapLoginModule":
                        lmAS7.setLoginModuleCode("AdvancedLdap");
                        break;
                    case "AdvancedADLoginModule":
                        lmAS7.setLoginModuleCode("AdvancedADldap");
                        break;
                    case "UsersRolesLoginModule":
                        lmAS7.setLoginModuleCode("UsersRoles");
                        break;
                    default:
                        lmAS7.setLoginModuleCode(lmAS5.getLoginModule());
                }

                if (lmAS5.getModuleOptions() != null) {
                    for (ModuleOptionAS5 moAS5 : lmAS5.getModuleOptions()) {
                        ModuleOptionAS7 moAS7 = new ModuleOptionAS7();
                        moAS7.setModuleOptionName(moAS5.getModuleName());

                        // TODO: Module-option using file can only use .properties?
                        if(moAS5.getModuleValue().contains("properties")){
                            String value;
                            if(moAS5.getModuleValue().contains("/")){
                                value = StringUtils.substringAfterLast(moAS5.getModuleValue(), "/");
                            } else{
                                value = moAS5.getModuleValue();
                            }
                            moAS7.setModuleOptionValue("${jboss.server.config.dir}/" + value);

                            CopyMemory cp = new CopyMemory();
                            cp.setName(value);
                            cp.setType("security");
                            copyMemories.add(cp);
                        }


                        moduleOptions.add(moAS7);
                    }
                }

                lmAS7.setModuleOptions(moduleOptions);
                loginModules.add(lmAS7);
            }

            securityDomain.setLoginModules(loginModules);
            securityDomains.add(securityDomain);
        }

        securityAS7.setSecurityDomains(securityDomains);

        return securityAS7;
    }


}
