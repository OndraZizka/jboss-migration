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
import org.jboss.loom.utils.UtilsAS5;
import org.jboss.loom.utils.XmlUtils;
import org.jboss.loom.utils.as7.AS7CliUtils;
import org.jboss.loom.utils.as7.AS7ModuleUtils;

/**
 * Migrator of Datasource subsystem implementing IMigrator
 * 
 *   WARNING: This is the worst written migrator. DO NOT use it as inspiration for your migrator.
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
    public void createActions( MigrationContext ctx ) throws MigrationException {
        
        // E.g. "com.mysql.jdbc.Driver" -> "mysql"
        Map<String, String> classToDriverNameMap = new HashMap();
        
        Map<File, String> jarToModuleMap = new HashMap();
        
        
        for( IConfigFragment fragment : ctx.getMigrationData().get(DatasourceMigrator.class).getConfigFragments() ) {
            
            String dsType = null;
            try {
                if( ! (fragment instanceof AbstractDatasourceBean) )
                    throw new MigrationException("Config fragment unrecognized by " + this.getClass().getSimpleName() + ": " + fragment );
                
                final AbstractDatasourceAS5Bean dsBeanAS5 = (AbstractDatasourceAS5Bean) fragment;
                
                
                // Getting existing or creating new driver resource.
                final String cls = dsBeanAS5.getDriverClass(); // StringUtils.defaultIfEmpty( driver.getDatasourceClass(), driver.getXaDatasourceClass() );
                String driverName = classToDriverNameMap.get( cls );
                if( driverName == null ){
                    
                    // Search for driver class in both AS 5 and AS 7 jars and create module.
                    List<IMigrationAction> actions = new LinkedList();
                    driverName = JDBC_DRIVER_MODULE_PREFIX + "createdDriver" + this.namingSequence ++; // If new.
                    driverName = createDriverActions( ctx, dsBeanAS5.getDriverClass(), jarToModuleMap, actions, driverName );
                    ctx.getActions().addAll( actions );
                    classToDriverNameMap.put( cls, driverName );
                }
                
                AbstractDatasourceAS7Bean dsBean = migrateDatasouceAS5( dsBeanAS5 );
                dsBean.setDriver( driverName );

                // Creating the datasource resource.
                if( fragment instanceof DatasourceAS5Bean ) {
                    dsType = "local-tx-datasource";
                    final DatasourceAS7Bean ds = (DatasourceAS7Bean) dsBean;
                    ctx.getActions().add( createDatasourceCliAction(ds) );
                }
                else if( fragment instanceof XaDatasourceAS5Bean ) {
                    dsType = "xa-datasource";
                    final XaDatasourceAS7Bean ds = (XaDatasourceAS7Bean) dsBean;
                    ctx.getActions().addAll( createXaDatasourceCliActions(ds) );
                }
                else if( fragment instanceof NoTxDatasourceAS5Bean ){
                    dsType = "no-tx-datasource";
                    final DatasourceAS7Bean ds = (DatasourceAS7Bean) dsBean;
                    ctx.getActions().add( createDatasourceCliAction(ds) );
                }
            }
            catch (CliScriptException ex) {
                throw new MigrationException("Migration of " + dsType + " failed: " + ex.getMessage(), ex);
            }
        }

    }// createActions()

    

    /**
     * Creates CliCommandAction for given driver and if driver's module isn't already created then it creates
     * ModuleCreationAction.
     *
     * TODO: Much better than the original, but I still don't like the structure of the flow. Should be like:
     * 
     *   1) Check if driver for JDBC class exists in AS7
     *      1a) If not:
     *          1a1) Check if a module with it exists
     *             1a1a) If not, copy .jar from AS 5 and create a module.
     *             1a1b) Store the module name.
     *          1a2) Create the driver
     *      1b) Store driver name
     */
    private String createDriverActions( final MigrationContext ctx, final String driverClass, Map<File, String> tempModules, final List<IMigrationAction> actions, String driverNameIfNew )
            throws MigrationException {

        // Driver to create, if necessary.
        DriverBean driver = new DriverBean();
        driver.setDriverClass( driverClass );
        driver.setXaDatasourceClass( driverClass );

        
        // Find out if the driver already exists in AS 7. If so, find which module and which configured JDBC driver it is.
        try {
            File driverJarAS7 = Utils.lookForJarWithClass( driverClass, getGlobalConfig().getAS7Config().getModulesDir() );
            // A .jar with driver class found.
            if( driverJarAS7 != null ){
                log.info("Target server already contains JDBC driver '" + driverClass + "': " + driverJarAS7);
                String driverModuleName = AS7ModuleUtils.identifyModuleContainingJar( getGlobalConfig().getAS7Config(), driverJarAS7 );
                
                // If a driver with that class exists, no actions needed. Return it's name.
                String existingDiverName = AS7CliUtils.findJdbcDriverUsingModule( driverModuleName, ctx.getAS7Client() );
                if( existingDiverName != null )
                    return existingDiverName;
                
                // Otherwise, we need to create that driver.
                driver.setDriverModule( driverModuleName );
                driver.setDriverName( driverNameIfNew );
                actions.add( createDriverCliAction(driver) );
                return driverNameIfNew;
            }
        }
        catch( IOException ex ) {
            throw new MigrationException("Finding .jar containing driver class '"+driverClass+"' in the source server failed:\n    " + ex .getMessage(), ex );
        }

        // The driver .jar not found in AS 7 -> copy it from AS 5.
        // Find driver .jar in AS 5
        File driverJarAS5;
        try {
            driverJarAS5 = UtilsAS5.findJarFileWithClass( driverClass, // TODO: return List<FIle>
                getGlobalConfig().getAS5Config().getDir(),
                getGlobalConfig().getAS5Config().getProfileName());
        }
        catch( IOException ex ) {
            throw new MigrationException("Finding .jar containing driver class '"+driverClass+"' in the target server failed:\n    " + ex .getMessage(), ex );
        }

        // If there's already a module for that jar -> Just create the driver resource.
        if( tempModules.containsKey(driverJarAS5) ) {
            // ModuleCreationAction is already set. No need for another one => just create a CLI for the driver.
            try {
                driver.setDriverModule( tempModules.get(driverJarAS5) );
                actions.add( createDriverCliAction(driver) );
                return driver.getDriverName();
            }
            catch (CliScriptException ex) {
                throw new MigrationException("Migration of driver failed (CLI command): " + ex.getMessage(), ex);
            }
        }
        
        
        // Driver jar not processed yet => create ModuleCreationAction, new module and a CLI script.
        {
            // Driver name == module name (the operation has that constraint)
            driver.setDriverName( driverNameIfNew );
            driver.setDriverModule( driverNameIfNew );
            tempModules.put( driverJarAS5, driver.getDriverModule() );

            // ModuleCreationAction
            String[] deps = new String[]{"javax.api", "javax.transaction.api", null, "javax.servlet.api"}; // null = next is optional.
            IMigrationAction moduleAction = new ModuleCreationAction( this.getClass(), driver.getDriverModule(), deps, driverJarAS5, Configuration.IfExists.OVERWRITE);
            actions.add(moduleAction);
            
            // CliAction
            try{
                CliCommandAction action = createDriverCliAction(driver);
                action.addDependency( moduleAction );
                actions.add( action );
            }
            catch (CliScriptException ex) {
                throw new MigrationException("Migration of driver failed (CLI command): " + ex.getMessage(), ex);
            }
        }

        return driver.getDriverName();
    }
    
    

    /**
     * Migrates datasource (all types) from AS5 to its equivalent in AS7
     *
     * @param  dsAS5 datasource to be migrated
     * @param drivers map containing created drivers to this point
     * @return created AS7 datasource
     */
    private AbstractDatasourceAS7Bean migrateDatasouceAS5( AbstractDatasourceAS5Bean dsAS5 ){
        
        AbstractDatasourceAS7Bean dsAS7;
        
        DriverBean driver;
        if( dsAS5 instanceof XaDatasourceAS5Bean){
            dsAS7 = new XaDatasourceAS7Bean();
            setXaDatasourceProps( (XaDatasourceAS7Bean) dsAS7, (XaDatasourceAS5Bean) dsAS5 );
        } else {
            dsAS7 = new DatasourceAS7Bean();
            setDatasourceProps( (DatasourceAS7Bean) dsAS7,  dsAS5 );
        }

        // Standalone elements in AS7
        dsAS7.setJndiName("java:jboss/datasources/" + dsAS5.getJndiName());
        dsAS7.setPoolName( dsAS5.getJndiName());
        dsAS7.setEnabled("true");
        dsAS7.setUseJavaContext( dsAS5.getUseJavaContext());
        dsAS7.setUrlDelimeter( dsAS5.getUrlDelimeter());
        dsAS7.setUrlSelectorStrategyClassName(dsAS5.getUrlSelectorStrategyClassName());

        dsAS7.setNewConnectionSql( dsAS5.getNewConnectionSql());

        // Elements in element <security> in AS7
        dsAS7.setUserName( dsAS5.getUserName());
        dsAS7.setPassword( dsAS5.getPassword());

        dsAS7.setSecurityDomain( dsAS5.getSecurityDomain());

        // Elements in element <timeout> in AS7
        dsAS7.setBlockingTimeoutMillis( dsAS5.getBlockingTimeoutMillis());
        dsAS7.setIdleTimeoutMinutes(dsAS5.getIdleTimeoutMinutes());
        dsAS7.setQueryTimeout( dsAS5.getQueryTimeout());
        dsAS7.setAllocationRetry( dsAS5.getAllocationRetry());
        dsAS7.setAllocationRetryWaitMillis(dsAS5.getAllocationRetryWaitMillis());
        dsAS7.setSetTxQueryTimeout( dsAS5.getSetTxQueryTimeout());
        dsAS7.setUseTryLock( dsAS5.getUseTryLock());

        // Elements in element <validation> in AS7
        dsAS7.setCheckValidConnectionSql(dsAS5.getCheckValidConnectionSql());
        dsAS7.setValidateOnMatch( dsAS5.getValidateOnMatch());
        dsAS7.setBackgroundValidation(dsAS5.getBackgroundValidation());
        dsAS7.setExceptionSorter( dsAS5.getExceptionSorterClassName());
        dsAS7.setValidConnectionChecker(dsAS5.getValidConnectionCheckerClassName());
        dsAS7.setStaleConnectionChecker(dsAS5.getStaleConnectionCheckerClassName());
        // Millis represents Milliseconds?
        if ( dsAS5.getBackgroundValidationMillis()!= null) {
            Integer tmp = Integer.valueOf( dsAS5.getBackgroundValidationMillis()) / 60000;
            dsAS7.setBackgroundValidationMinutes(tmp.toString());
        }

        // Elements in element <statement> in AS7
        dsAS7.setTrackStatements( dsAS5.getTrackStatements());
        dsAS7.setSharePreparedStatements(dsAS5.getSharePreparedStatements());
        dsAS7.setQueryTimeout( dsAS5.getQueryTimeout());

        // Strange element use-fast-fail
        //datasourceAS7.setUseFastFail(datasourceAS5.gF);

        return dsAS7;
    }

    /**
     * Sets specific attributes for Xa-Datasource
     *
     * @param dsAS7 xa-datasource from AS7 for setting attributes
     * @param xaDatasourceAS5 xa-datasource from AS5 for getting attributes
     */
    private static void setXaDatasourceProps(XaDatasourceAS7Bean dsAS7, XaDatasourceAS5Bean dsAS5){
        // Elements in element <xa-pool> in AS7
        dsAS7.setMinPoolSize( dsAS5.getMinPoolSize());
        dsAS7.setMaxPoolSize( dsAS5.getMaxPoolSize());
        dsAS7.setPrefill( dsAS5.getPrefill());
        dsAS7.setSameRmOverride( dsAS5.getSameRM());
        dsAS7.setInterleaving( dsAS5.getInterleaving());
        dsAS7.setNoTxSeparatePools( dsAS5.getNoTxSeparatePools());
        if( dsAS5.getXaDatasourceProps() != null)
            dsAS7.setXaDatasourceProps( dsAS5.getXaDatasourceProps());
        dsAS7.setXaResourceTimeout( dsAS5.getXaResourceTimeout());
        dsAS7.setTransactionIsolation(dsAS5.getTransIsolation());
    }

    /**
     * Sets specific attributes for Datasource
     */
    private static void setDatasourceProps(DatasourceAS7Bean dsAS7, AbstractDatasourceAS5Bean dsAS5){
        dsAS7.setJta( ( dsAS5 instanceof NoTxDatasourceAS5Bean) ? "false" : "true");
        if( dsAS5.getConnectionProperties() != null)
            dsAS7.setConnectionProperties( dsAS5.getConnectionProperties() );
        dsAS7.setConnectionUrl( dsAS5.getConnectionUrl() );
        dsAS7.setMinPoolSize( dsAS5.getMinPoolSize());
        dsAS7.setMaxPoolSize( dsAS5.getMaxPoolSize());
        dsAS7.setPrefill( dsAS5.getPrefill());
    }

    /**
     * Creates CliCommandAction for adding a Datasource
     */
    private static CliCommandAction createDatasourceCliAction(DatasourceAS7Bean ds) throws CliScriptException {
        String errMsg = " in datasource must be set.";
        validateDatasource( ds, errMsg);
        Utils.throwIfBlank( ds.getConnectionUrl(), errMsg, "Connection url");

        return new CliCommandAction( DatasourceMigrator.class, 
                createDatasourceScript(ds), 
                createDatasourceModelNode(ds) );
    }

    private static ModelNode createDatasourceModelNode( DatasourceAS7Bean ds ){
        // TODO: Use @Property(...) and get values automatically.
        
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        request.get(ClientConstants.OP_ADDR).add("data-source", ds.getPoolName());

        CliApiCommandBuilder builder = new CliApiCommandBuilder(request);
        Map<String, String> props = new HashMap();
        
        props.put("jndi-name", ds.getJndiName());
        props.put("driver-name", ds.getDriver());
        props.put("enabled", "true"); // TODO: Try if this property works
        props.put("connection-url", ds.getConnectionUrl());
        
        fillDatasourcePropertiesMap( props, ds );

        // Non-XA specific
        props.put( "jta", ds.getJta());

        builder.addPropertiesIfSet( props );
        
        return builder.getCommand();
    }
    
    /**
     * Creates a CLI script for adding a Datasource. New format of script.
     *
     * @deprecated  This should be buildable from the ModelNode.
     *              String cliCommand = AS7CliUtils.formatCommand( builder.getCommand() );
     */
    private static String createDatasourceScript(DatasourceAS7Bean ds) throws CliScriptException {

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder sb = new StringBuilder("data-source add ");
        
        // TODO: BeanUtils.copyProperties?

        Map<String, String> props = new HashMap();

        props.put("name", ds.getPoolName());
        props.put("jndi-name", ds.getJndiName());
        props.put("driver-name", ds.getDriver());
        props.put("connection-url", ds.getConnectionUrl());

        fillDatasourcePropertiesMap( props, ds );

        // Non-XA specific
        props.put("jta", ds.getJta());
        
        builder.addProperties( props );
        sb.append(builder.formatAndClearProps());
        
        // TODO: Not sure whether to enabled the datasource .
        //resultScript.append("\n");
        //resultScript.append("data-source enable --name=").append(datasourceAS7.getPoolName());

        return sb.toString();
    }
    
    

    private static Map<String,String> fillDatasourcePropertiesMap( Map<String, String> props, AbstractDatasourceAS7Bean ds) {
        props.put("allocation-retry", ds.getAllocationRetry());
        props.put("allocation-retry-wait-millis", ds.getAllocationRetryWaitMillis());
        props.put("background-validation", ds.getBackgroundValidation());
        props.put("background-validation-minutes", ds.getBackgroundValidationMinutes());
        props.put("blocking-timeout-millis", ds.getBlockingTimeoutMillis());
        props.put("check-valid-connection-sql", ds.getCheckValidConnectionSql());
        props.put("exception-sorter-class-name", ds.getExceptionSorter());
        props.put("idle-timeout-minutes", ds.getIdleTimeoutMinutes());
        props.put("max-pool-size", ds.getMaxPoolSize());
        props.put("min-pool-size", ds.getMinPoolSize());
        props.put("new-connection-sql", ds.getNewConnectionSql());
        props.put("password", ds.getPassword());
        props.put("prefill", ds.getPrefill());
        props.put("prepared-statement-cache-size", ds.getPreparedStatementCacheSize());
        props.put("query-timeout", ds.getQueryTimeout());
        props.put("security-domain", ds.getSecurityDomain());
        props.put("set-tx-query-timeout", ds.getSetTxQueryTimeout());
        props.put("share-prepared-statements", ds.getSharePreparedStatements());
        props.put("stale-connection-checker-class-name", ds.getStaleConnectionChecker());
        props.put("track-statements", ds.getTrackStatements());
        props.put("transaction-isolation", ds.getTransactionIsolation());
        props.put("url-delimeter", ds.getUrlDelimeter());
        props.put("url-selector-strategy-class-name", ds.getUrlSelectorStrategyClassName());
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
    private static List<CliCommandAction> createXaDatasourceCliActions(XaDatasourceAS7Bean ds) throws CliScriptException {
        String errMsg = " in XA datasource must be set.";
        validateDatasource( ds, errMsg);

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

        // TODO: Doesn't have connection-url???
        // TODO: BeanUtils.copyProperties?
        Map<String,String> props = new HashMap();

        props.put("jndi-name", ds.getJndiName());
        props.put("driver-name", ds.getDriver());
        
        fillDatasourcePropertiesMap( props, ds );
        
        // XA specific
        props.put("interleaving", ds.getInterleaving());
        props.put("is-same-rm-override", ds.getSameRmOverride());
        props.put("no-tx-separate-pools", ds.getNoTxSeparatePools());
        props.put("xa-resource-timeout", ds.getXaResourceTimeout());

        builder.addPropertiesIfSet( props );
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
    private static String createXaDatasourceScript(XaDatasourceAS7Bean ds) throws CliScriptException {
        
        String errMsg = " in XA datasource must be set.";
        validateDatasource( ds, errMsg);

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder sb = new StringBuilder("xa-data-source add ");
        Map<String, String> props = new HashMap();

        props.put("name", ds.getPoolName());
        props.put("jndi-name", ds.getJndiName());
        props.put("driver-name", ds.getDriver());

        // TODO: Doesn't have connection-url???
        fillDatasourcePropertiesMap( props, ds );

        // XA specific
        props.put("interleaving", ds.getInterleaving());
        props.put("is-same-rm-override", ds.getSameRmOverride());
        props.put("no-tx-separate-pools", ds.getNoTxSeparatePools());
        props.put("xa-resource-timeout", ds.getXaResourceTimeout());
        
        builder.addProperties( props );
        sb.append(builder.formatAndClearProps());

        // TODO: Not sure if set datasource enabled. For now I don't know way enabling datasource in CLI API
        //resultScript.append("\n");
        //resultScript.append("xa-data-source enable --name=").append(xaDatasourceAS7.getPoolName());

        return sb.toString();
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
    private static String createXaPropertyScript(XaDatasourceAS7Bean datasource, XaDatasourcePropertyBean xaDatasourceProperty) throws CliScriptException {
        
        String errMsg = "in xa-datasource property must be set";
        Utils.throwIfBlank(xaDatasourceProperty.getXaDatasourcePropName(), errMsg, "Property name");

        StringBuilder sb = new StringBuilder();
        sb.append("/subsystem=datasources/xa-data-source=").append(datasource.getPoolName());
        sb.append("/xa-datasource-properties=").append(xaDatasourceProperty.getXaDatasourcePropName());
        sb.append(":add(value=").append(xaDatasourceProperty.getXaDatasourceProp()).append(")");

        return sb.toString();
    }

    
    
    /**
     * Creates CliCommandAction for adding a Driver
     *
     * @param driver object representing Driver
     * @return created CliCommandAction for adding the Driver
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Driver are missing or
     *                            are empty (module, driver-name)
     */
    private static CliCommandAction createDriverCliAction( DriverBean driver ) throws CliScriptException {
        
        String errMsg = " in driver must be set.";
        Utils.throwIfBlank(driver.getDriverModule(), errMsg, "Module");
        Utils.throwIfBlank(driver.getDriverName(), errMsg, "Driver-name");
        
        final ModelNode modelNode = createDriverModelNode(driver);
        return new CliCommandAction( DatasourceMigrator.class, /*createDriverScript(driver)*/ AS7CliUtils.formatCommand( modelNode ), modelNode);
    }
    
    private static ModelNode createDriverModelNode( DriverBean driver ){
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        //JBAS010414: the attribute driver-name (jdbcdrivers.createdDriver1) 
        //            cannot be different from driver resource name (createdDriver1)
        request.get(ClientConstants.OP_ADDR).add("jdbc-driver", driver.getDriverModule()); // getDriverName()

        CliApiCommandBuilder builder = new CliApiCommandBuilder(request);
        Map<String,String> props = new HashMap();
        props.put("driver-name", driver.getDriverModule());
        props.put("driver-module-name", driver.getDriverModule());
        props.put("driver-class-name", driver.getDriverClass());
        props.put("driver-xa-datasource-class-name", driver.getXaDatasourceClass());
        props.put("driver-major-version", driver.getMajorVersion());
        props.put("driver-minor-version", driver.getMinorVersion());
        builder.addPropertiesIfSet( props );
        return builder.getCommand();
    }    
    
    
    private static void validateDatasource( AbstractDatasourceAS7Bean datasource, String errMsg ) throws CliScriptException {
        Utils.throwIfBlank(datasource.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(datasource.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(datasource.getDriver(), errMsg, "Driver name");
    }
    
}// class
