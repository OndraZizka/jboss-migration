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

    protected int namingSequence = 1;

    

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
                if (ds.getLocalDatasourceAS5s() != null) 
                    mData.getConfigFragments().addAll(ds.getLocalDatasourceAS5s());

                if (ds.getXaDatasourceAS5s() != null)
                    mData.getConfigFragments().addAll(ds.getXaDatasourceAS5s());

                if(ds.getNoTxDatasourceAS5s() != null)
                    mData.getConfigFragments().addAll(ds.getNoTxDatasourceAS5s());
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
     * @param  dsAS5 datasource to be migrated
     * @param drivers map containing created drivers to this point
     * @return created AS7 datasource
     */
    private AbstractDatasourceAS7Bean migrateDatasouceAS5(AbstractDatasourceAS5Bean  dsAS5, Map<String, DriverBean> drivers){
        AbstractDatasourceAS7Bean dsAS7;
        DriverBean driver;

        if( dsAS5 instanceof XaDatasourceAS5Bean){
            dsAS7 = new XaDatasourceAS7Bean();
            driver = drivers.get( ((XaDatasourceAS5Bean)  dsAS5).getXaDatasourceClass() );
            setXaDatasourceProps( (XaDatasourceAS7Bean) dsAS7, (XaDatasourceAS5Bean)  dsAS5 );
        } else {
            dsAS7 = new DatasourceAS7Bean();
            driver = drivers.get(  dsAS5.getDriverClass() );
            setDatasourceProps((DatasourceAS7Bean) dsAS7,  dsAS5);
        }

        // Setting name for driver
        if( null != driver ){
            dsAS7.setDriver( driver.getDriverName());
        }
        else {
            driver = new DriverBean();
            driver.setDriverClass(  dsAS5.getDriverClass() );

            String driverName = JDBC_DRIVER_MODULE_PREFIX + "createdDriver" + this.namingSequence ++;
            dsAS7.setDriver(driverName);
            driver.setDriverName(driverName);
            drivers.put(  dsAS5.getDriverClass(), driver );
        }

        // Standalone elements in AS7
        dsAS7.setJndiName("java:jboss/datasources/" +  dsAS5.getJndiName());
        dsAS7.setPoolName( dsAS5.getJndiName());
        dsAS7.setEnabled("true");
        dsAS7.setUseJavaContext( dsAS5.getUseJavaContext());
        dsAS7.setUrlDelimeter( dsAS5.getUrlDelimeter());
        dsAS7.setUrlSelector( dsAS5.getUrlSelectStratClName());

        dsAS7.setNewConnectionSql( dsAS5.getNewConnectionSql());

        // Elements in element <security> in AS7
        dsAS7.setUserName( dsAS5.getUserName());
        dsAS7.setPassword( dsAS5.getPassword());

        dsAS7.setSecurityDomain( dsAS5.getSecurityDomain());

        // Elements in element <timeout> in AS7
        dsAS7.setBlockingTimeoutMillis( dsAS5.getBlockingTimeMillis());
        dsAS7.setIdleTimeoutMin( dsAS5.getIdleTimeoutMin());
        dsAS7.setQueryTimeout( dsAS5.getQueryTimeout());
        dsAS7.setAllocationRetry( dsAS5.getAllocationRetry());
        dsAS7.setAllocRetryWaitMillis( dsAS5.getAllocRetryWaitMillis());
        dsAS7.setSetTxQueryTimeout( dsAS5.getSetTxQueryTime());
        dsAS7.setUseTryLock( dsAS5.getUseTryLock());

        // Elements in element <validation> in AS7
        dsAS7.setCheckValidConSql( dsAS5.getCheckValidConSql());
        dsAS7.setValidateOnMatch( dsAS5.getValidateOnMatch());
        dsAS7.setBackgroundValid( dsAS5.getBackgroundValid());
        dsAS7.setExceptionSorter( dsAS5.getExcepSorterClName());
        dsAS7.setValidConChecker( dsAS5.getValidConCheckerClName());
        dsAS7.setStaleConChecker( dsAS5.getStaleConCheckerClName());
        // Millis represents Milliseconds?
        if ( dsAS5.getBackgroundValidMillis() != null) {
            Integer tmp = Integer.valueOf( dsAS5.getBackgroundValidMillis()) / 60000;
            dsAS7.setBackgroundValidMin(tmp.toString());

        }

        // Elements in element <statement> in AS7
        dsAS7.setTrackStatements( dsAS5.getTrackStatements());
        dsAS7.setSharePreStatements( dsAS5.getSharePreStatements());
        dsAS7.setQueryTimeout( dsAS5.getQueryTimeout());

        // Strange element use-fast-fail
        //datasourceAS7.setUseFastFail(datasourceAS5.gF);

        return dsAS7;
    }

    /**
     * Sets specific attributes for Xa-Datasource
     *
     * @param xadsAS7 xa-datasource from AS7 for setting attributes
     * @param xaDatasourceAS5 xa-datasource from AS5 for getting attributes
     */
    private static void setXaDatasourceProps(XaDatasourceAS7Bean xadsAS7, XaDatasourceAS5Bean xadsAS5){
        // Elements in element <xa-pool> in AS7
        xadsAS7.setMinPoolSize( xadsAS5.getMinPoolSize());
        xadsAS7.setMaxPoolSize( xadsAS5.getMaxPoolSize());
        xadsAS7.setPrefill( xadsAS5.getPrefill());
        xadsAS7.setSameRmOverride( xadsAS5.getSameRM());
        xadsAS7.setInterleaving( xadsAS5.getInterleaving());
        xadsAS7.setNoTxSeparatePools( xadsAS5.getNoTxSeparatePools());
        if( xadsAS5.getXaDatasourceProps() != null)
            xadsAS7.setXaDatasourceProps( xadsAS5.getXaDatasourceProps());
        xadsAS7.setXaResourceTimeout( xadsAS5.getXaResourceTimeout());
        xadsAS7.setTransIsolation( xadsAS5.getTransIsolation());
    }

    /**
     * Sets specific attributes for Datasource
     *
     * @param datasourceAS7 datasource from AS7 for setting attributes
     * @param  dsAS5 datasource from AS5 for getting attributes
     */
    private static void setDatasourceProps(DatasourceAS7Bean datasourceAS7, AbstractDatasourceAS5Bean dsAS5){
        datasourceAS7.setJta( ( dsAS5 instanceof NoTxDatasourceAS5Bean) ? "false" : "true");
        if( dsAS5.getConnectionProperties() != null)
            datasourceAS7.setConnectionProperties( dsAS5.getConnectionProperties() );
        datasourceAS7.setConnectionUrl( dsAS5.getConnectionUrl() );
        datasourceAS7.setMinPoolSize( dsAS5.getMinPoolSize());
        datasourceAS7.setMaxPoolSize( dsAS5.getMaxPoolSize());
        datasourceAS7.setPrefill( dsAS5.getPrefill());
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
                createDatasourceScript(datasource), 
                createDatasourceModelNode(datasource) );
    }

    private static ModelNode createDatasourceModelNode( DatasourceAS7Bean ds ){
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        request.get(ClientConstants.OP_ADDR).add("data-source", ds.getPoolName());

        CliApiCommandBuilder builder = new CliApiCommandBuilder(request);
        builder.addPropertyIfSet("jndi-name", ds.getJndiName());
        builder.addPropertyIfSet("driver-name", ds.getDriver());
        builder.addPropertyIfSet("enabled", "true"); // TODO: Try if this property works
        builder.addPropertyIfSet("connection-url", ds.getConnectionUrl());
        
        // NOT TODO: Refactor to be able to use addDatasourceProperties(builder, ds);
        // TODO: Introduce @Eap6CliProperty(["allocation-retry"]) and get values automatically.
        Map<String, String> props = new HashMap();
        fillDatasourcePropertiesMap( props, ds );
        builder.addPropertiesIfSet( props );

        // Non-XA specific
        builder.addPropertyIfSet( "jta", ds.getJta());
        
        return builder.getCommand();
    }
    
    /**
     * Creates a CLI script for adding a Datasource. New format of script.
     *
     * @deprecated  This should be buildable from the ModelNode.
     *              String cliCommand = AS7CliUtils.formatCommand( builder.getCommand() );
     */
    private static String createDatasourceScript(DatasourceAS7Bean dsAS7) throws CliScriptException {

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("data-source add ");

        builder.addProperty("name", dsAS7.getPoolName());
        builder.addProperty("driver-name", dsAS7.getDriver());
        builder.addProperty("jndi-name", dsAS7.getJndiName());
        builder.addProperty("connection-url", dsAS7.getConnectionUrl());

        // TODO: BeanUtils.copyProperties?
        Map<String, String> props = new HashMap();
        fillDatasourcePropertiesMap( props, dsAS7 );
        builder.addProperties( props );

        // Non-XA specific
        builder.addProperty("jta", dsAS7.getJta());
        
        resultScript.append(builder.formatAndClearProps());
        
        // TODO: Not sure whether to enabled the datasource .
        //resultScript.append("\n");
        //resultScript.append("data-source enable --name=").append(datasourceAS7.getPoolName());

        return resultScript.toString();
    }
    
    

    private static Map<String,String> fillDatasourcePropertiesMap( Map<String, String> props, AbstractDatasourceAS7Bean ds) {
        props.put("allocation-retry", ds.getAllocationRetry());
        props.put("allocation-retry-wait-millis", ds.getAllocRetryWaitMillis());
        props.put("background-validation", ds.getBackgroundValid());
        props.put("background-validation-minutes", ds.getBackgroundValidMin());
        props.put("blocking-timeout-millis", ds.getBlockingTimeoutMillis());
        props.put("check-valid-connection-sql", ds.getCheckValidConSql());
        props.put("exception-sorter-class-name", ds.getExceptionSorter());
        props.put("idle-timeout-minutes", ds.getIdleTimeoutMin());
        props.put("max-pool-size", ds.getMaxPoolSize());
        props.put("min-pool-size", ds.getMinPoolSize());
        props.put("new-connection-sql", ds.getNewConnectionSql());
        props.put("password", ds.getPassword());
        props.put("prefill", ds.getPrefill());
        props.put("prepared-statement-cache-size", ds.getPreStatementCacheSize());
        props.put("query-timeout", ds.getQueryTimeout());
        props.put("security-domain", ds.getSecurityDomain());
        props.put("set-tx-query-timeout", ds.getSetTxQueryTimeout());
        props.put("share-prepared-statements", ds.getSharePreStatements());
        props.put("stale-connection-checker-class-name", ds.getStaleConChecker());
        props.put("track-statements", ds.getTrackStatements());
        props.put("transaction-isolation", ds.getTransIsolation());
        props.put("url-delimeter", ds.getUrlDelimeter());
        props.put("url-selector-strategy-class-name", ds.getUrlSelector());
        props.put("use-fast-fail", ds.getUseFastFail());
        props.put("use-java-context", ds.getUseJavaContext());
        props.put("use-try-lock", ds.getUseTryLock());
        props.put("user-name", ds.getUserName());
        props.put("valid-connection-checker-class-name", ds.getValidateOnMatch());
        props.put("validate-on-match", ds.getValidateOnMatch());
        return props;
    }
    


    /**
     * Creates a list of CliCommandActions for adding a Xa-Datasource
     *
     * @param  ds Xa-Datasource for adding
     * @return  list containing CliCommandActions for adding the Xa-Datasource
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Xa-Datasource
     *                            are missing or are empty (pool-name, jndi-name, driver-name)
     */
    private static List<CliCommandAction> createXaDatasourceCliActions(XaDatasourceAS7Bean ds)
            throws CliScriptException {
        String errMsg = " in xaDatasource must be set.";
        Utils.throwIfBlank( ds.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank( ds.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank( ds.getDriver(), errMsg, "Driver name");

        List<CliCommandAction> actions = new LinkedList();
        actions.add( new CliCommandAction( DatasourceMigrator.class, 
                createXaDatasourceScript(ds),
                createXaDatasourceModelNode(ds)));

        // Properties
        if( ds.getXaDatasourceProps() != null){
            for(XaDatasourcePropertyBean property :  ds.getXaDatasourceProps()){
                actions.add(createXaPropertyCliAction( ds, property));
            }
        }

        return actions;
    }

    
    // === XA datasource ===
    
    private static ModelNode createXaDatasourceModelNode( XaDatasourceAS7Bean ds){
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        request.get(ClientConstants.OP_ADDR).add("xa-data-source", ds.getPoolName());

        CliApiCommandBuilder builder = new CliApiCommandBuilder(request);

        builder.addPropertyIfSet("jndi-name", ds.getJndiName());
        builder.addPropertyIfSet("driver-name", ds.getDriver());
        
        // TODO: Doesn't have connection-url???
        // TODO: Most are the same as for non-XA. Refactor.
        // TODO: BeanUtils.copyProperties?
        Map<String,String> props = new HashMap();
        fillDatasourcePropertiesMap( props, ds );
        builder.addPropertiesIfSet( props );
        
        // XA specific
        builder.addPropertyIfSet("interleaving", ds.getInterleaving());
        builder.addPropertyIfSet("is-same-rm-override", ds.getSameRmOverride());
        builder.addPropertyIfSet("no-tx-separate-pools", ds.getNoTxSeparatePools());
        builder.addPropertyIfSet("xa-resource-timeout", ds.getXaResourceTimeout());

        return builder.getCommand();
    }
    
    /**
     * Creates a CLI script for adding a Xa-Datasource. New format of script.
     *
     * @param xadsAS7 object of XaDatasource
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes for creation of the CLI script are missing or are empty
     *                           (pool-name, jndi-name, driver-name)
     * 
     * @deprecated  This should be buildable from the ModelNode.
     *              String cliCommand = AS7CliUtils.formatCommand( builder.getCommand() );
     */
    private static String createXaDatasourceScript(XaDatasourceAS7Bean xadsAS7) throws CliScriptException {
        
        String errMsg = " in XA datasource must be set.";
        Utils.throwIfBlank(xadsAS7.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(xadsAS7.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(xadsAS7.getDriver(), errMsg, "Driver name");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("xa-data-source add ");

        builder.addProperty("name", xadsAS7.getPoolName());
        builder.addProperty("jndi-name", xadsAS7.getJndiName());
        builder.addProperty("driver-name", xadsAS7.getDriver());

        // TODO: Doesn't have connection-url???
        Map<String, String> props = new HashMap();
        fillDatasourcePropertiesMap( props, xadsAS7 );
        builder.addProperties( props );

        // XA specific
        builder.addProperty("interleaving", xadsAS7.getInterleaving());
        builder.addProperty("is-same-rm-override", xadsAS7.getSameRmOverride());
        builder.addProperty("no-tx-separate-pools", xadsAS7.getNoTxSeparatePools());
        builder.addProperty("xa-resource-timeout", xadsAS7.getXaResourceTimeout());
        
        resultScript.append(builder.formatAndClearProps());
        //resultScript.append("\n");

        // TODO: Not sure if set datasource enabled. For now I don't know way enabling datasource in CLI API
        //resultScript.append("xa-data-source enable --name=").append(xaDatasourceAS7.getPoolName());

        return resultScript.toString();
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
     * Creates a CLI script for adding one Xa-Datasource-Property of the specific Xa-Datasource
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
        resultScript.append("driver-module-name=").append(driver.getDriverModule()).append(", ");

        builder.addProperty("driver-class-name", driver.getDriverClass());
        builder.addProperty("driver-xa-datasource-class-name", driver.getXaDatasourceClass());
        builder.addProperty("driver-major-version", driver.getMajorVersion());
        builder.addProperty("driver-minor-version", driver.getMinorVersion());

        resultScript.append(builder.formatAndClearProps()).append(")");

        return resultScript.toString();
    }


    
}// class
