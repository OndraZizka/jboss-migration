/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.dataSources;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.loom.utils.as7.CliAddScriptBuilder;
import org.jboss.loom.utils.as7.CliApiCommandBuilder;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ctx.MigratorData;
import org.jboss.loom.actions.CliCommandAction;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.actions.ModuleCreationAction;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.ActionException;
import org.jboss.loom.ex.CliScriptException;
import org.jboss.loom.ex.LoadMigrationException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.migrators.dataSources.jaxb.*;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;
import org.jboss.loom.utils.XmlUtils;

/**
 * Migrator of Datasource subsystem implementing IMigrator
 *
 * @author Roman Jakubco
 */
@ConfigPartDescriptor(
    name = "Datasources configuration",
    docLink = "https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/5/html-single/Administration_And_Configuration_Guide/index.html#datasource-config"
)
public class DatasourceMigrator extends AbstractMigrator {
    private static final Logger log = LoggerFactory.getLogger(DatasourceMigrator.class);
    
    @Override protected String getConfigPropertyModuleName() { return "datasource"; }

    
    private static final String JDBC_DRIVER_MODULE_PREFIX = "jdbcdrivers.";
    private static final String DATASOURCES_ROOT_ELEMENT_NAME = "datasources";

    private int namingSequence = 1;

    

    public DatasourceMigrator(GlobalConfiguration globalConfig) {
        super(globalConfig);

    }

    @Override
    public void loadSourceServerConfig(MigrationContext ctx) throws LoadMigrationException {
        try {

            // Get a list of -ds.xml files.
            File dsFiles = getGlobalConfig().getAS5Config().getDeployDir();
            if( ! dsFiles.canRead() )
                throw new LoadMigrationException("Can't read: " + dsFiles);
            
            SuffixFileFilter sf = new SuffixFileFilter("-ds.xml");
            Collection<File> dsXmls = FileUtils.listFiles(dsFiles, sf, FileFilterUtils.trueFileFilter());
            log.debug("  Found -ds.xml files #: " + dsXmls.size());
            if( dsXmls.isEmpty() )
                return;

            List<DatasourcesBean> dsColl = new LinkedList();
            Unmarshaller dataUnmarshaller = JAXBContext.newInstance(DatasourcesBean.class).createUnmarshaller();
            
            for( File dsXml : dsXmls ) {
                Document doc = XmlUtils.parseFileToXmlDoc( dsXml );
                Element element = doc.getDocumentElement();
                if( DATASOURCES_ROOT_ELEMENT_NAME.equals( element.getTagName() )){
                    DatasourcesBean dataSources = (DatasourcesBean) dataUnmarshaller.unmarshal(dsXml);
                    dsColl.add(dataSources);
                }
            }

            MigratorData mData = new MigratorData();

            for (DatasourcesBean ds : dsColl) {
                if (ds.getLocalDatasourceAS5s() != null) {
                    mData.getConfigFragments().addAll(ds.getLocalDatasourceAS5s());
                }

                if (ds.getXaDatasourceAS5s() != null) {
                    mData.getConfigFragments().addAll(ds.getXaDatasourceAS5s());
                }

                if(ds.getNoTxDatasourceAS5s() != null){
                    mData.getConfigFragments().addAll(ds.getNoTxDatasourceAS5s());
                }
            }

            ctx.getMigrationData().put(DatasourceMigrator.class, mData);

        } catch (JAXBException | SAXException | IOException ex) {
            throw new LoadMigrationException(ex);
        }
    }

    
    
    /**
     * The driver creation CliCommandAction must be added (performed) before datasource creation.
     * 
     * TODO: Rewrite to some sane form. I.e. code drivers "cache" and references handover properly.
     */
    @Override
    public void createActions(MigrationContext ctx) throws MigrationException {
        
        Map<String, DriverBean> classToDriverMap = new HashMap();
        
        List<IMigrationAction> tempActions = new LinkedList();
        for( IConfigFragment fragment : ctx.getMigrationData().get(DatasourceMigrator.class).getConfigFragments() ) {
            
            String dsType = null;
            try {
                if( fragment instanceof DatasourceAS5Bean ) {
                    dsType = "local-tx-datasource";
                    final DatasourceAS7Bean ds = (DatasourceAS7Bean) migrateDatasouceAS5( (AbstractDatasourceAS5Bean) fragment, classToDriverMap );
                    tempActions.add( createDatasourceCliAction(ds) );
                }
                else if( fragment instanceof XaDatasourceAS5Bean ) {
                    dsType = "xa-datasource";
                    final XaDatasourceAS7Bean ds = (XaDatasourceAS7Bean) migrateDatasouceAS5( (AbstractDatasourceAS5Bean) fragment, classToDriverMap );
                    tempActions.addAll(createXaDatasourceCliActions(ds));
                }
                else if( fragment instanceof NoTxDatasourceAS5Bean ){
                    dsType = "no-tx-datasource";
                    final DatasourceAS7Bean ds = (DatasourceAS7Bean) migrateDatasouceAS5( (AbstractDatasourceAS5Bean) fragment, classToDriverMap );
                    tempActions.add(createDatasourceCliAction(ds));
                }
                else 
                    throw new MigrationException("Config fragment unrecognized by " + this.getClass().getSimpleName() + ": " + fragment );
            }
            catch (CliScriptException ex) {
                throw new MigrationException("Migration of " + dsType + " failed: " + ex.getMessage(), ex);
            }
        }

        
        // Search for driver class in jars and create module. Similar to finding logging classes.
        HashMap<File, String> tempModules = new HashMap();
        for( DriverBean driver : classToDriverMap.values() ) {
            ctx.getActions().addAll( createDriverActions(driver, tempModules) );
        }

        // Add datasource CliCommandActions after drivers.
        ctx.getActions().addAll(tempActions);
    }

    /**
     * Creates CliCommandAction for given driver and if driver's module isn't already created then it creates
     * ModuleCreationAction.
     *
     * @param driver driver for migration
     * @param tempModules  Map containing already created Modules
     * @return  list containing CliCommandAction for adding driver and if Module module for this driver hasn't be already
     *          defined then the list also contains ModuleCreationAction for creating module.
     * @throws ActionException if jar archive containing class declared in driver cannot be found or if creation of
     *         CliCommandAction fails or if Document representing module.xml cannot be created.
     */
    private List<IMigrationAction> createDriverActions(DriverBean driver, HashMap<File, String> tempModules)
            throws MigrationException {
        
        // Find driver .jar
        File driverJar;
        try {
            driverJar = Utils.findJarFileWithClass(
                    StringUtils.defaultIfEmpty(driver.getDriverClass(), driver.getXaDatasourceClass() ),
                    getGlobalConfig().getAS5Config().getDir(),
                    getGlobalConfig().getAS5Config().getProfileName());
        }
        catch (IOException e) {
            throw new MigrationException("Finding jar containing driver class failed: " + e.getMessage(), e);
        }

        List<IMigrationAction> actions = new LinkedList();

        if( tempModules.containsKey(driverJar) ) {
            // ModuleCreationAction is already set. No need for another one => just create a CLI for the driver.
            try {
                driver.setDriverModule(tempModules.get(driverJar));
                actions.add( createDriverCliAction(driver) );
            }
            catch (CliScriptException ex) {
                throw new MigrationException("Migration of driver failed (CLI command): " + ex.getMessage(), ex);
            }
            return actions;
        }
        
        
        // Driver jar not processed yet => create ModuleCreationAction, new module and a CLI script.
        {
            final String moduleName = driver.getDriverName();
            driver.setDriverModule( moduleName );
            tempModules.put(driverJar, driver.getDriverModule());

            // CliAction
            try{
                CliCommandAction action = createDriverCliAction(driver);
                actions.add( action );
            }
            catch (CliScriptException ex) {
                throw new MigrationException("Migration of driver failed (CLI command): " + ex.getMessage(), ex);
            }

            String[] deps = new String[]{"javax.api", "javax.transaction.api", null, "javax.servlet.api"}; // null = next is optional.
            
            IMigrationAction moduleAction = new ModuleCreationAction( this.getClass(), moduleName, deps, driverJar, Configuration.IfExists.OVERWRITE);
            actions.add(moduleAction);
        }

        return actions;
    }

    /**
     * Migrates datasource (all types) from AS5 to its equivalent in AS7
     *
     * @param datasourceAS5 datasource to be migrated
     * @param drivers map containing created drivers to this point
     * @return created AS7 datasource
     */
    private AbstractDatasourceAS7Bean migrateDatasouceAS5(AbstractDatasourceAS5Bean datasourceAS5, Map<String, DriverBean> drivers){
        AbstractDatasourceAS7Bean datasourceAS7;
        DriverBean driver;

        if(datasourceAS5 instanceof XaDatasourceAS5Bean){
            datasourceAS7 = new XaDatasourceAS7Bean();
            driver = drivers.get( ((XaDatasourceAS5Bean) datasourceAS5).getXaDatasourceClass() );
            setXaDatasourceProps( (XaDatasourceAS7Bean) datasourceAS7, (XaDatasourceAS5Bean) datasourceAS5 );
        } else {
            datasourceAS7 = new DatasourceAS7Bean();
            driver = drivers.get( datasourceAS5.getDriverClass() );
            setDatasourceProps((DatasourceAS7Bean) datasourceAS7, datasourceAS5);
        }

        // Setting name for driver
        if( null != driver ){
            datasourceAS7.setDriver( driver.getDriverName());
        }
        else {
            driver = new DriverBean();
            driver.setDriverClass( datasourceAS5.getDriverClass() );

            String driverName = JDBC_DRIVER_MODULE_PREFIX + "createdDriver" + this.namingSequence ++;
            datasourceAS7.setDriver(driverName);
            driver.setDriverName(driverName);
            drivers.put( datasourceAS5.getDriverClass(), driver );
        }

        // Standalone elements in AS7
        datasourceAS7.setJndiName("java:jboss/datasources/" + datasourceAS5.getJndiName());
        datasourceAS7.setPoolName(datasourceAS5.getJndiName());
        datasourceAS7.setEnabled("true");
        datasourceAS7.setUseJavaContext(datasourceAS5.getUseJavaContext());
        datasourceAS7.setUrlDelimeter(datasourceAS5.getUrlDelimeter());
        datasourceAS7.setUrlSelector(datasourceAS5.getUrlSelectStratClName());

        datasourceAS7.setNewConnectionSql(datasourceAS5.getNewConnectionSql());

        // Elements in element <security> in AS7
        datasourceAS7.setUserName(datasourceAS5.getUserName());
        datasourceAS7.setPassword(datasourceAS5.getPassword());

        datasourceAS7.setSecurityDomain(datasourceAS5.getSecurityDomain());

        // Elements in element <timeout> in AS7
        datasourceAS7.setBlockingTimeoutMillis(datasourceAS5.getBlockingTimeMillis());
        datasourceAS7.setIdleTimeoutMin(datasourceAS5.getIdleTimeoutMin());
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
            Integer tmp = Integer.valueOf(datasourceAS5.getBackgroundValidMillis()) / 60000;
            datasourceAS7.setBackgroundValidMin(tmp.toString());

        }

        // Elements in element <statement> in AS7
        datasourceAS7.setTrackStatements(datasourceAS5.getTrackStatements());
        datasourceAS7.setSharePreStatements(datasourceAS5.getSharePreStatements());
        datasourceAS7.setQueryTimeout(datasourceAS5.getQueryTimeout());

        // Strange element use-fast-fail
        //datasourceAS7.setUseFastFail(datasourceAS5.gF);

        return datasourceAS7;
    }

    /**
     * Sets specific attributes for Xa-Datasource
     *
     * @param xaDatasourceAS7 xa-datasource from AS7 for setting attributes
     * @param xaDatasourceAS5 xa-datasource from AS5 for getting attributes
     */
    private static void setXaDatasourceProps(XaDatasourceAS7Bean xaDatasourceAS7, XaDatasourceAS5Bean xaDatasourceAS5){
        // Elements in element <xa-pool> in AS7
        xaDatasourceAS7.setMinPoolSize(xaDatasourceAS5.getMinPoolSize());
        xaDatasourceAS7.setMaxPoolSize(xaDatasourceAS5.getMaxPoolSize());
        xaDatasourceAS7.setPrefill(xaDatasourceAS5.getPrefill());
        xaDatasourceAS7.setSameRmOverride(xaDatasourceAS5.getSameRM());
        xaDatasourceAS7.setInterleaving(xaDatasourceAS5.getInterleaving());
        xaDatasourceAS7.setNoTxSeparatePools(xaDatasourceAS5.getNoTxSeparatePools());

        if(xaDatasourceAS5.getXaDatasourceProps() != null){
            xaDatasourceAS7.setXaDatasourceProps(xaDatasourceAS5.getXaDatasourceProps());
        }

        xaDatasourceAS7.setXaResourceTimeout(xaDatasourceAS5.getXaResourceTimeout());
        xaDatasourceAS7.setTransIsolation(xaDatasourceAS5.getTransIsolation());

    }

    /**
     * Sets specific attributes for Datasource
     *
     * @param datasourceAS7 datasource from AS7 for setting attributes
     * @param datasourceAS5 datasource from AS5 for getting attributes
     */
    private static void setDatasourceProps(DatasourceAS7Bean datasourceAS7, AbstractDatasourceAS5Bean datasourceAS5){
        if(datasourceAS5 instanceof NoTxDatasourceAS5Bean){
            datasourceAS7.setJta("false");

        } else{
            datasourceAS7.setJta("true");
        }

        if( datasourceAS5.getConnectionProperties() != null){
            datasourceAS7.setConnectionProperties( datasourceAS5.getConnectionProperties() );
        }

        datasourceAS7.setConnectionUrl( datasourceAS5.getConnectionUrl() );
        datasourceAS7.setMinPoolSize(datasourceAS5.getMinPoolSize());
        datasourceAS7.setMaxPoolSize(datasourceAS5.getMaxPoolSize());
        datasourceAS7.setPrefill(datasourceAS5.getPrefill());
    }

    /**
     * Creates CliCommandAction for adding a Datasource
     *
     * @param datasource Datasource for adding
     * @return  created CliCommandAction for adding the Datasource
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Datasource
     *                            are missing or are empty (pool-name, jndi-name, connection-url, driver-name)
     */
    private static CliCommandAction createDatasourceCliAction(DatasourceAS7Bean datasource)
            throws CliScriptException {
        String errMsg = " in datasource must be set.";
        Utils.throwIfBlank(datasource.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(datasource.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(datasource.getConnectionUrl(), errMsg, "Connection url");
        Utils.throwIfBlank(datasource.getDriver(), errMsg, "Driver name");

        return new CliCommandAction( DatasourceMigrator.class, 
                createDatasourceScriptNew(datasource), 
                createDatasourceModelNode(datasource) );
    }

    private static ModelNode createDatasourceModelNode( DatasourceAS7Bean dataSource ){
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        request.get(ClientConstants.OP_ADDR).add("data-source", dataSource.getPoolName());

        CliApiCommandBuilder builder = new CliApiCommandBuilder(request);
        builder.addPropertyIfSet("jndi-name", dataSource.getJndiName());

        // TODO: Try if property enabled works
        builder.addPropertyIfSet("enabled", "true");

        builder.addPropertyIfSet("driver-name", dataSource.getDriver());
        
        builder.addPropertyIfSet("jta", dataSource.getJta());
        builder.addPropertyIfSet("use-java-context", dataSource.getUseJavaContext());
        builder.addPropertyIfSet("connection-url", dataSource.getConnectionUrl());
        builder.addPropertyIfSet("url-delimeter", dataSource.getUrlDelimeter());
        builder.addPropertyIfSet("url-selector-strategy-class-name", dataSource.getUrlSelector());
        builder.addPropertyIfSet("transaction-isolation", dataSource.getTransIsolation());
        builder.addPropertyIfSet("new-connection-sql", dataSource.getNewConnectionSql());
        builder.addPropertyIfSet("prefill", dataSource.getPrefill());
        builder.addPropertyIfSet("min-pool-size", dataSource.getMinPoolSize());
        builder.addPropertyIfSet("max-pool-size", dataSource.getMaxPoolSize());
        builder.addPropertyIfSet("password", dataSource.getPassword());
        builder.addPropertyIfSet("user-name", dataSource.getUserName());
        builder.addPropertyIfSet("security-domain", dataSource.getSecurityDomain());
        builder.addPropertyIfSet("check-valid-connection-sql", dataSource.getCheckValidConSql());
        builder.addPropertyIfSet("validate-on-match", dataSource.getValidateOnMatch());
        builder.addPropertyIfSet("background-validation", dataSource.getBackgroundValid());
        builder.addPropertyIfSet("background-validation-minutes", dataSource.getBackgroundValidMin());
        builder.addPropertyIfSet("use-fast-fail", dataSource.getUseFastFail());
        builder.addPropertyIfSet("exception-sorter-class-name", dataSource.getExceptionSorter());
        builder.addPropertyIfSet("valid-connection-checker-class-name", dataSource.getValidateOnMatch());
        builder.addPropertyIfSet("stale-connection-checker-class-name", dataSource.getStaleConChecker());
        builder.addPropertyIfSet("blocking-timeout-millis", dataSource.getBlockingTimeoutMillis());
        builder.addPropertyIfSet("idle-timeout-minutes", dataSource.getIdleTimeoutMin());
        builder.addPropertyIfSet("set-tx-query-timeout", dataSource.getSetTxQueryTimeout());
        builder.addPropertyIfSet("query-timeout", dataSource.getQueryTimeout());
        builder.addPropertyIfSet("allocation-retry", dataSource.getAllocationRetry());
        builder.addPropertyIfSet("allocation-retry-wait-millis", dataSource.getAllocRetryWaitMillis());
        builder.addPropertyIfSet("use-try-lock", dataSource.getUseTryLock());
        builder.addPropertyIfSet("prepared-statement-cache-size", dataSource.getPreStatementCacheSize());
        builder.addPropertyIfSet("track-statements", dataSource.getTrackStatements());
        builder.addPropertyIfSet("share-prepared-statements", dataSource.getSharePreStatements());
        return builder.getCommand();
    }


    /**
     * Creates a list of CliCommandActions for adding a Xa-Datasource
     *
     * @param dataSource Xa-Datasource for adding
     * @return  list containing CliCommandActions for adding the Xa-Datasource
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Xa-Datasource
     *                            are missing or are empty (pool-name, jndi-name, driver-name)
     */
    private static List<CliCommandAction> createXaDatasourceCliActions(XaDatasourceAS7Bean dataSource)
            throws CliScriptException {
        String errMsg = " in xaDatasource must be set.";
        Utils.throwIfBlank(dataSource.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(dataSource.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(dataSource.getDriver(), errMsg, "Driver name");

        List<CliCommandAction> actions = new LinkedList();


        actions.add( new CliCommandAction( DatasourceMigrator.class, 
                createXaDatasourceScriptNew(dataSource),
                createXaDatasourceModelNode(dataSource)));

        // Properties
        if(dataSource.getXaDatasourceProps() != null){
            for(XaDatasourcePropertyBean property : dataSource.getXaDatasourceProps()){
                actions.add(createXaPropertyCliAction(dataSource, property));
            }
        }

        return actions;
    }

    
    private static ModelNode createXaDatasourceModelNode( XaDatasourceAS7Bean dataSource){
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        request.get(ClientConstants.OP_ADDR).add("xa-data-source", dataSource.getPoolName());

        CliApiCommandBuilder builder = new CliApiCommandBuilder(request);

        builder.addPropertyIfSet("jndi-name", dataSource.getJndiName());
        builder.addPropertyIfSet("use-java-context", dataSource.getUseJavaContext());
        builder.addPropertyIfSet("driver-name", dataSource.getDriver());
        builder.addPropertyIfSet("url-delimeter", dataSource.getUrlDelimeter());
        builder.addPropertyIfSet("url-selector-strategy-class-name", dataSource.getUrlSelector());
        builder.addPropertyIfSet("transaction-isolation", dataSource.getTransIsolation());
        builder.addPropertyIfSet("new-connection-sql", dataSource.getNewConnectionSql());
        builder.addPropertyIfSet("prefill", dataSource.getPrefill());
        builder.addPropertyIfSet("min-pool-size", dataSource.getMinPoolSize());
        builder.addPropertyIfSet("max-pool-size", dataSource.getMaxPoolSize());
        builder.addPropertyIfSet("is-same-rm-override", dataSource.getSameRmOverride());
        builder.addPropertyIfSet("interleaving", dataSource.getInterleaving());
        builder.addPropertyIfSet("no-tx-separate-pools", dataSource.getNoTxSeparatePools());
        builder.addPropertyIfSet("password", dataSource.getPassword());
        builder.addPropertyIfSet("user-name", dataSource.getUserName());
        builder.addPropertyIfSet("security-domain", dataSource.getSecurityDomain());
        builder.addPropertyIfSet("check-valid-connection-sql", dataSource.getCheckValidConSql());
        builder.addPropertyIfSet("validate-on-match", dataSource.getValidateOnMatch());
        builder.addPropertyIfSet("background-validation", dataSource.getBackgroundValid());
        builder.addPropertyIfSet("background-validation-minutes", dataSource.getBackgroundValidMin());
        builder.addPropertyIfSet("use-fast-fail", dataSource.getUseFastFail());
        builder.addPropertyIfSet("exception-sorter-class-name", dataSource.getExceptionSorter());
        builder.addPropertyIfSet("valid-connection-checker-class-name", dataSource.getValidateOnMatch());
        builder.addPropertyIfSet("stale-connection-checker-class-name", dataSource.getStaleConChecker());
        builder.addPropertyIfSet("blocking-timeout-millis", dataSource.getBlockingTimeoutMillis());
        builder.addPropertyIfSet("idle-timeout-minutes", dataSource.getIdleTimeoutMin());
        builder.addPropertyIfSet("set-tx-query-timeout", dataSource.getSetTxQueryTimeout());
        builder.addPropertyIfSet("query-timeout", dataSource.getQueryTimeout());
        builder.addPropertyIfSet("allocation-retry", dataSource.getAllocationRetry());
        builder.addPropertyIfSet("allocation-retry-wait-millis", dataSource.getAllocRetryWaitMillis());
        builder.addPropertyIfSet("use-try-lock", dataSource.getUseTryLock());
        builder.addPropertyIfSet("xa-resource-timeout", dataSource.getXaResourceTimeout());
        builder.addPropertyIfSet("prepared-statement-cache-size", dataSource.getPreStatementCacheSize());
        builder.addPropertyIfSet("track-statements", dataSource.getTrackStatements());
        builder.addPropertyIfSet("share-prepared-statements", dataSource.getSharePreStatements());
        
        return builder.getCommand();
    }
    

    /**
     * Creates CliCommandAction for adding a Xa-Datasource-Property of the specific Xa-Datasource
     *
     * @param datasource Xa-Datasource containing Xa-Datasource-Property
     * @param property Xa-Datasource-property
     * @return  created CliCommandAction for adding the Xa-Datasource-Property
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Xa-Datasource-Property
     *                            are missing or are empty (property-name)
     */
    private static CliCommandAction createXaPropertyCliAction(XaDatasourceAS7Bean datasource, XaDatasourcePropertyBean property)
            throws CliScriptException{
        String errMsg = "in xa-datasource property must be set";
        Utils.throwIfBlank(property.getXaDatasourcePropName(), errMsg, "Property name");

        ModelNode xaDsPropNode = createXaPropertyModelNode( datasource.getPoolName(), property);
        String    xaDsPropCli  = createXaPropertyScript(datasource, property);

        return new CliCommandAction( DatasourceMigrator.class, xaDsPropCli, xaDsPropNode);
    }
    
    private static ModelNode createXaPropertyModelNode( String dsName, XaDatasourcePropertyBean property ){
        ModelNode connProperty = new ModelNode();
        connProperty.get(ClientConstants.OP).set(ClientConstants.ADD);
        connProperty.get(ClientConstants.OP_ADDR).add("subsystem","datasources");
        connProperty.get(ClientConstants.OP_ADDR).add("xa-data-source", dsName);
        connProperty.get(ClientConstants.OP_ADDR).add("xa-datasource-properties", property.getXaDatasourcePropName());
        connProperty.get("value").set(property.getXaDatasourceProp());
        return connProperty;
    }

    /**
     * Creates CliCommandAction for adding a Driver
     *
     * @param driver object representing Driver
     * @return created CliCommandAction for adding the Driver
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Driver are missing or
     *                            are empty (module, driver-name)
     */
    private static CliCommandAction createDriverCliAction(DriverBean driver) throws CliScriptException {
        
        String errMsg = " in driver must be set.";
        Utils.throwIfBlank(driver.getDriverModule(), errMsg, "Module");
        Utils.throwIfBlank(driver.getDriverName(), errMsg, "Driver-name");

        return new CliCommandAction( DatasourceMigrator.class, createDriverScript(driver), createDriverModelNode(driver) );
    }
    
    private static ModelNode createDriverModelNode( DriverBean driver){
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        //JBAS010414: the attribute driver-name (jdbcdrivers.createdDriver1) 
        //            cannot be different from driver resource name (createdDriver1)
        request.get(ClientConstants.OP_ADDR).add("jdbc-driver", driver.getDriverModule()); // getDriverName()

        CliApiCommandBuilder builder = new CliApiCommandBuilder(request);

        builder.addPropertyIfSet("driver-name", driver.getDriverModule());
        builder.addPropertyIfSet("driver-module-name", driver.getDriverModule());
        builder.addPropertyIfSet("driver-class-name", driver.getDriverClass());
        builder.addPropertyIfSet("driver-xa-datasource-class-name", driver.getXaDatasourceClass());
        builder.addPropertyIfSet("driver-major-version", driver.getMajorVersion());
        builder.addPropertyIfSet("driver-minor-version", driver.getMinorVersion());
        
        return builder.getCommand();
    }    
    
    /**
     * Creates a CLI script for adding a Driver
     *
     * @param driver object of DriverBean
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes for creation of the CLI script are missing or are empty
     *                           (module, driver-name)
     * 
     * @deprecated  This should be buildable from the ModelNode.
     *              String cliCommand = AS7CliUtils.formatCommand( builder.getCommand() );
     */
    private static String createDriverScript(DriverBean driver) throws CliScriptException {
        String errMsg = " in driver must be set.";
        Utils.throwIfBlank(driver.getDriverModule(), errMsg, "Module");
        Utils.throwIfBlank(driver.getDriverName(), errMsg, "Driver-name");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=datasources/jdbc-driver=");

        resultScript.append(driver.getDriverName()).append(":add(");
        resultScript.append("driver-module-name=").append(driver.getDriverModule() + ", ");

        builder.addProperty("driver-class-name", driver.getDriverClass());
        builder.addProperty("driver-xa-datasource-class-name", driver.getXaDatasourceClass());
        builder.addProperty("driver-major-version", driver.getMajorVersion());
        builder.addProperty("driver-minor-version", driver.getMinorVersion());

        resultScript.append(builder.asString()).append(")");

        return resultScript.toString();
    }

    /**
     * Creates a CLI script for adding a Datasource. New format of script.
     *
     * @param datasourceAS7 object of Datasource
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes for creation of the CLI script are missing or are empty
     *                           (pool-name, jndi-name, connection-url, driver-name)
     * 
     * @deprecated  This should be buildable from the ModelNode.
     *              String cliCommand = AS7CliUtils.formatCommand( builder.getCommand() );
     */
    private static String createDatasourceScriptNew(DatasourceAS7Bean datasourceAS7) throws CliScriptException {

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("data-source add ");

        builder.addProperty("name", datasourceAS7.getPoolName());
        builder.addProperty("driver-name", datasourceAS7.getDriver());
        builder.addProperty("jndi-name", datasourceAS7.getJndiName());
        
        builder.addProperty("jta", datasourceAS7.getJta());
        builder.addProperty("use-java-context", datasourceAS7.getUseJavaContext());
        builder.addProperty("connection-url", datasourceAS7.getConnectionUrl());
        builder.addProperty("url-delimeter", datasourceAS7.getUrlDelimeter());
        builder.addProperty("url-selector-strategy-class-name", datasourceAS7.getUrlSelector());
        builder.addProperty("transaction-isolation", datasourceAS7.getTransIsolation());
        builder.addProperty("new-connection-sql", datasourceAS7.getNewConnectionSql());
        builder.addProperty("prefill", datasourceAS7.getPrefill());
        builder.addProperty("min-pool-size", datasourceAS7.getMinPoolSize());
        builder.addProperty("max-pool-size", datasourceAS7.getMaxPoolSize());
        builder.addProperty("password", datasourceAS7.getPassword());
        builder.addProperty("user-name", datasourceAS7.getUserName());
        builder.addProperty("security-domain", datasourceAS7.getSecurityDomain());
        builder.addProperty("check-valid-connection-sql", datasourceAS7.getCheckValidConSql());
        builder.addProperty("validate-on-match", datasourceAS7.getValidateOnMatch());
        builder.addProperty("background-validation", datasourceAS7.getBackgroundValid());
        builder.addProperty("background-validation-minutes", datasourceAS7.getBackgroundValidMin());
        builder.addProperty("use-fast-fail", datasourceAS7.getUseFastFail());
        builder.addProperty("exception-sorter-class-name", datasourceAS7.getExceptionSorter());
        builder.addProperty("valid-connection-checker-class-name", datasourceAS7.getValidateOnMatch());
        builder.addProperty("stale-connection-checker-class-name", datasourceAS7.getStaleConChecker());
        builder.addProperty("blocking-timeout-millis", datasourceAS7.getBlockingTimeoutMillis());
        builder.addProperty("idle-timeout-minutes", datasourceAS7.getIdleTimeoutMin());
        builder.addProperty("set-tx-query-timeout", datasourceAS7.getSetTxQueryTimeout());
        builder.addProperty("query-timeout", datasourceAS7.getQueryTimeout());
        builder.addProperty("allocation-retry", datasourceAS7.getAllocationRetry());
        builder.addProperty("allocation-retry-wait-millis", datasourceAS7.getAllocRetryWaitMillis());
        builder.addProperty("use-try-lock", datasourceAS7.getUseTryLock());
        builder.addProperty("prepared-statement-cache-size", datasourceAS7.getPreStatementCacheSize());
        builder.addProperty("track-statements", datasourceAS7.getTrackStatements());
        builder.addProperty("share-prepared-statements", datasourceAS7.getSharePreStatements());

        resultScript.append(builder.asStringNew());
        // TODO: Not sure if set datasource enabled. For now I don't know way enabling datasource in CLI API
        //resultScript.append("\n");
        //resultScript.append("data-source enable --name=").append(datasourceAS7.getPoolName());

        return resultScript.toString();
    }


    /**
     * Creates a CLI script for adding a Xa-Datasource. New format of script.
     *
     * @param xaDatasourceAS7 object of XaDatasource
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes for creation of the CLI script are missing or are empty
     *                           (pool-name, jndi-name, driver-name)
     * 
     * @deprecated  This should be buildable from the ModelNode.
     *              String cliCommand = AS7CliUtils.formatCommand( builder.getCommand() );
     */
    private static String createXaDatasourceScriptNew(XaDatasourceAS7Bean xaDatasourceAS7) throws CliScriptException {
        
        String errMsg = " in xaDatasource must be set.";
        Utils.throwIfBlank(xaDatasourceAS7.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(xaDatasourceAS7.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(xaDatasourceAS7.getDriver(), errMsg, "Driver name");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("xa-data-source add ");

        builder.addProperty("name", xaDatasourceAS7.getPoolName());
        builder.addProperty("jndi-name", xaDatasourceAS7.getJndiName());
        builder.addProperty("use-java-context", xaDatasourceAS7.getUseJavaContext());
        builder.addProperty("driver-name", xaDatasourceAS7.getDriver());
        builder.addProperty("url-delimeter", xaDatasourceAS7.getUrlDelimeter());
        builder.addProperty("url-selector-strategy-class-name", xaDatasourceAS7.getUrlSelector());
        builder.addProperty("transaction-isolation", xaDatasourceAS7.getTransIsolation());
        builder.addProperty("new-connection-sql", xaDatasourceAS7.getNewConnectionSql());
        builder.addProperty("prefill", xaDatasourceAS7.getPrefill());
        builder.addProperty("min-pool-size", xaDatasourceAS7.getMinPoolSize());
        builder.addProperty("max-pool-size", xaDatasourceAS7.getMaxPoolSize());
        builder.addProperty("is-same-rm-override", xaDatasourceAS7.getSameRmOverride());
        builder.addProperty("interleaving", xaDatasourceAS7.getInterleaving());
        builder.addProperty("no-tx-separate-pools", xaDatasourceAS7.getNoTxSeparatePools());
        builder.addProperty("password", xaDatasourceAS7.getPassword());
        builder.addProperty("user-name", xaDatasourceAS7.getUserName());
        builder.addProperty("security-domain", xaDatasourceAS7.getSecurityDomain());
        builder.addProperty("check-valid-connection-sql", xaDatasourceAS7.getCheckValidConSql());
        builder.addProperty("validate-on-match", xaDatasourceAS7.getValidateOnMatch());
        builder.addProperty("background-validation", xaDatasourceAS7.getBackgroundValid());
        builder.addProperty("background-validation-minutes", xaDatasourceAS7.getBackgroundValidMin());
        builder.addProperty("use-fast-fail", xaDatasourceAS7.getUseFastFail());
        builder.addProperty("exception-sorter-class-name", xaDatasourceAS7.getExceptionSorter());
        builder.addProperty("valid-connection-checker-class-name", xaDatasourceAS7.getValidateOnMatch());
        builder.addProperty("stale-connection-checker-class-name", xaDatasourceAS7.getStaleConChecker());
        builder.addProperty("blocking-timeout-millis", xaDatasourceAS7.getBlockingTimeoutMillis());
        builder.addProperty("idle-timeout-minutes", xaDatasourceAS7.getIdleTimeoutMin());
        builder.addProperty("set-tx-query-timeout", xaDatasourceAS7.getSetTxQueryTimeout());
        builder.addProperty("query-timeout", xaDatasourceAS7.getQueryTimeout());
        builder.addProperty("allocation-retry", xaDatasourceAS7.getAllocationRetry());
        builder.addProperty("allocation-retry-wait-millis", xaDatasourceAS7.getAllocRetryWaitMillis());
        builder.addProperty("use-try-lock", xaDatasourceAS7.getUseTryLock());
        builder.addProperty("xa-resource-timeout", xaDatasourceAS7.getXaResourceTimeout());
        builder.addProperty("prepared-statement-cache-size", xaDatasourceAS7.getPreStatementCacheSize());
        builder.addProperty("track-statements", xaDatasourceAS7.getTrackStatements());
        builder.addProperty("share-prepared-statements", xaDatasourceAS7.getSharePreStatements());

        resultScript.append(builder.asStringNew());
        //resultScript.append("\n");

        // TODO: Not sure if set datasource enabled. For now I don't know way enabling datasource in CLI API
        //resultScript.append("xa-data-source enable --name=").append(xaDatasourceAS7.getPoolName());

        return resultScript.toString();
    }

    /**
     * Creates a CLI script for adding one Xa-Datasource-Property of the specific Xa-Datasource
     *
     * @param datasource Xa-Datasource containing Xa-Datasource-Property
     * @param xaDatasourceProperty Xa-Datasource-Property
     * @return created string containing CLI script for adding Xa-Datasource-Property
     * @throws CliScriptException if required attributes for creation of the CLI script are missing or are empty
     *                           (property-name)
     * 
     * @deprecated  This should be buildable from the ModelNode.
     *              String cliCommand = AS7CliUtils.formatCommand( builder.getCommand() );
     */
    private static String createXaPropertyScript(XaDatasourceAS7Bean datasource, XaDatasourcePropertyBean xaDatasourceProperty)
            throws CliScriptException {
        
        String errMsg = "in xa-datasource property must be set";
        Utils.throwIfBlank(xaDatasourceProperty.getXaDatasourcePropName(), errMsg, "Property name");

        StringBuilder resultScript = new StringBuilder();
        resultScript.append("/subsystem=datasources/xa-data-source=").append(datasource.getPoolName());
        resultScript.append("/xa-datasource-properties=").append(xaDatasourceProperty.getXaDatasourcePropName());
        resultScript.append(":add(value=").append(xaDatasourceProperty.getXaDatasourceProp()).append(")");

        return resultScript.toString();
    }
    
}// class
