package cz.muni.fi.jboss.migration.migrators.dataSources;

import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.ex.ApplyMigrationException;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.NodeGenerationException;
import cz.muni.fi.jboss.migration.migrators.dataSources.jaxb.*;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.utils.AS7ModuleUtils;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Migrator of Datasource subsystem implementing IMigrator
 *
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:41 AM
 */

public class DatasourceMigrator extends AbstractMigrator {
    
    @Override protected String getConfigPropertyModuleName() { return "datasource"; }
    

    private Set<String> drivers = new HashSet();

    private Set<String> xaDatasourceClasses = new HashSet();

    public DatasourceMigrator(GlobalConfiguration globalConfig, MultiValueMap config) {
        super(globalConfig, config);

    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException {
        try {
            Unmarshaller dataUnmarshaller = JAXBContext.newInstance(DataSourcesBean.class).createUnmarshaller();
            List<DataSourcesBean> dsColl = new ArrayList();

            File dsFiles = new File(super.getGlobalConfig().getDirAS5() + super.getGlobalConfig().getProfileAS5() +
                    File.separator + "deploy");

            if (dsFiles.canRead()) {
                SuffixFileFilter sf = new SuffixFileFilter("-ds.xml");
                List<File> list = (List<File>) FileUtils.listFiles(dsFiles, sf, null);

                if (list.isEmpty()) {
                    throw new LoadMigrationException("No \"-ds.xml\" to parse!");
                }

                for (File aList : list) {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(aList);

                    Element element = doc.getDocumentElement();

                    if (element.getTagName().equalsIgnoreCase("datasources")) {
                        DataSourcesBean dataSources = (DataSourcesBean) dataUnmarshaller.unmarshal(aList);
                        dsColl.add(dataSources);
                    }
                }
            } else {
                throw new LoadMigrationException("Don't have permission for reading files in directory \"AS5_Home"
                        + File.separator + "deploy\"");

            }

            MigrationData mData = new MigrationData();

            for (DataSourcesBean ds : dsColl) {
                if (ds.getLocalDatasourceAS5s() != null) {
                    mData.getConfigFragment().addAll(ds.getLocalDatasourceAS5s());
                }
                if (ds.getXaDatasourceAS5s() != null) {
                    mData.getConfigFragment().addAll(ds.getXaDatasourceAS5s());
                }

            }

            ctx.getMigrationData().put(DatasourceMigrator.class, mData);

        } catch (JAXBException | ParserConfigurationException | SAXException | IOException e) {
            throw new LoadMigrationException(e);
        }

    }

    @Override
    public void apply(MigrationContext ctx) throws ApplyMigrationException {
        try {
            Document doc = ctx.getStandaloneDoc();
            NodeList subsystems = doc.getElementsByTagName("subsystem");
            for (int i = 0; i < subsystems.getLength(); i++) {
                if (!(subsystems.item(i) instanceof Element)) {
                    continue;
                }
                if (((Element) subsystems.item(i)).getAttribute("xmlns").contains("datasource")) {
                    Node parent = subsystems.item(i).getFirstChild();
                    while (!(parent instanceof Element)) {
                        parent = parent.getNextSibling();
                    }

                    Node lastNode = parent.getLastChild();

                    while (!(lastNode instanceof Element)) {
                        lastNode = lastNode.getPreviousSibling();
                    }

                    for (Node node : generateDomElements(ctx)) {
                        ((Element) node).setAttribute("xmlns", "urn:jboss:domain:datasources:1.1");
                        Node adopted = doc.adoptNode(node.cloneNode(true));


                        if (node.getNodeName().equals("driver")) {
                            lastNode.appendChild(adopted);
                        } else {
                            parent.insertBefore(adopted, lastNode);
                        }


                    }
                    break;

                }
            }
        } catch (NodeGenerationException e) {
            throw new ApplyMigrationException(e);
        }
    }

    @Override
    public List<Node> generateDomElements(MigrationContext ctx) throws NodeGenerationException {
        try {
            List<Node> nodeList = new ArrayList();
            Marshaller dataMarshaller = JAXBContext.newInstance(DatasourceAS7Bean.class).createMarshaller();
            Marshaller xaDataMarshaller = JAXBContext.newInstance(XaDatasourceAS7Bean.class).createMarshaller();
            Marshaller driverMarshaller = JAXBContext.newInstance(DriverBean.class).createMarshaller();

            for (IConfigFragment fragment : ctx.getMigrationData().get(DatasourceMigrator.class).getConfigFragment()) {
                if (fragment instanceof DatasourceAS5Bean) {
                    Document doc = ctx.getDocBuilder().newDocument();
                    dataMarshaller.marshal(datasourceMigration((DatasourceAS5Bean) fragment), doc);
                    nodeList.add(doc.getDocumentElement());
                    continue;
                }
                if (fragment instanceof XaDatasourceAS5Bean) {
                    Document doc = ctx.getDocBuilder().newDocument();
                    xaDataMarshaller.marshal(xaDatasourceMigration((XaDatasourceAS5Bean) fragment), doc);
                    nodeList.add(doc.getDocumentElement());
                    continue;
                }
                throw new NodeGenerationException("Object is not part of Datasource migration!");
            }

            for (String driverClass : this.drivers) {
                DriverBean driver = new DriverBean();
                driver.setDriverClass(driverClass);
                driver.setDriverName(StringUtils.substringAfter(driverClass, "."));

                RollbackData cp = new RollbackData();
                cp.setDriverName(driverClass);
                cp.setType("driver");
                cp.setModule(AS7ModuleUtils.createDriverModule(driverClass));
                driver.setDriverModule(AS7ModuleUtils.createDriverModule(driverClass));

                ctx.getRollbackData().add(cp);

                Document doc = ctx.getDocBuilder().newDocument();
                driverMarshaller.marshal(driver, doc);
                nodeList.add(doc.getDocumentElement());
            }

            for (String xaDsClass : this.xaDatasourceClasses) {
                DriverBean driver = new DriverBean();
                driver.setXaDatasourceClass(xaDsClass);
                driver.setDriverName(StringUtils.substringAfter(xaDsClass, "."));

                RollbackData cp = new RollbackData();
                cp.setDriverName(xaDsClass);
                cp.setType("driver");
                cp.setModule(AS7ModuleUtils.createDriverModule(xaDsClass));
                driver.setDriverModule(AS7ModuleUtils.createDriverModule(xaDsClass));

                ctx.getRollbackData().add(cp);

                Document doc = ctx.getDocBuilder().newDocument();
                driverMarshaller.marshal(driver, doc);
                nodeList.add(doc.getDocumentElement());
            }

            return nodeList;
        } catch (JAXBException e) {
            throw new NodeGenerationException(e);
        }
    }

    @Override
    public List<String> generateCliScripts(MigrationContext ctx) throws CliScriptException {
        try {
            List<String> list = new ArrayList();
            Unmarshaller dataUnmarshaller = JAXBContext.newInstance(DatasourceAS7Bean.class).createUnmarshaller();
            Unmarshaller driverUnmarshaller = JAXBContext.newInstance(DriverBean.class).createUnmarshaller();
            Unmarshaller xaDataUnmarshaller = JAXBContext.newInstance(XaDatasourceAS7Bean.class).createUnmarshaller();

            for (Node node : generateDomElements(ctx)) {
                if (node.getNodeName().equals("datasource")) {
                    DatasourceAS7Bean data = (DatasourceAS7Bean) dataUnmarshaller.unmarshal(node);
                    list.add(createDatasourceScriptOld(data));
                    //list.add(createDatasourceScriptNew(data));
                    continue;
                }
                if (node.getNodeName().equals("xa-datasource")) {
                    XaDatasourceAS7Bean xaData = (XaDatasourceAS7Bean) xaDataUnmarshaller.unmarshal(node);
                    list.add(createXaDatasourceScriptOld(xaData));
                    continue;
                }
                if (node.getNodeName().endsWith("driver")) {
                    DriverBean driver = (DriverBean) driverUnmarshaller.unmarshal(node);
                    list.add(createDriverScript(driver));
                }
            }

            return list;
        } catch (NodeGenerationException | JAXBException e) {
            throw new CliScriptException(e);
        }
    }

    // TODO: Security-Domain must reference something what exists in subsystem security...

    /**
     * Method for migrating Datasource from AS5 to AS7
     *
     * @param datasourceAS5 object of Datasource in AS5
     * @return object representing migrated Datasource in AS7
     */
    public DatasourceAS7Bean datasourceMigration(DatasourceAS5Bean datasourceAS5) {
        DatasourceAS7Bean datasourceAS7 = new DatasourceAS7Bean();

        this.drivers.add(datasourceAS5.getDriverClass());

        // Standalone elements in AS7
        datasourceAS7.setJndiName("java:jboss/datasources/" + datasourceAS5.getJndiName());
        datasourceAS7.setPoolName(datasourceAS5.getJndiName());
        datasourceAS7.setEnabled("true");
        datasourceAS7.setUseJavaContext(datasourceAS5.getUseJavaContext());
        datasourceAS7.setUrlDelimeter(datasourceAS5.getUrlDelimeter());
        datasourceAS7.setUrlSelector(datasourceAS5.getUrlSelectStratClName());
        datasourceAS7.setConnectionUrl(datasourceAS5.getConnectionUrl());

        if (datasourceAS5.getConnectionProperties() != null) {
            datasourceAS7.setConnectionProperties(datasourceAS5.getConnectionProperties());
        }

        datasourceAS7.setTransIsolation(datasourceAS5.getTransIsolation());
        datasourceAS7.setNewConnectionSql(datasourceAS5.getNewConnectionSql());

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
     * Method for migrating XaDatasource from AS5 to AS7
     *
     * @param xaDataAS5 object of XaDatasource in AS5
     * @return object representing migrated XaDatasource in AS7
     */
    public XaDatasourceAS7Bean xaDatasourceMigration(XaDatasourceAS5Bean xaDataAS5) {
        XaDatasourceAS7Bean xaDataAS7 = new XaDatasourceAS7Bean();

        xaDataAS7.setJndiName("java:jboss/datasources/" + xaDataAS5.getJndiName());
        xaDataAS7.setPoolName(xaDataAS5.getJndiName());
        xaDataAS7.setUseJavaContext(xaDataAS5.getUseJavaContext());
        xaDataAS7.setEnabled("true");

        // xa-datasource-class should be declared in drivers no in datasource.
        // xa-datasource then reference xa-datasource-class with element name
        //xaDatasourceAS7.setXaDatasourceClass(xaDatasourceAS5.getXaDatasourceClass());
        this.xaDatasourceClasses.add(xaDataAS5.getXaDatasourceClass());

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
            Integer tmp = Integer.valueOf(xaDataAS5.getBackgroundValidMillis()) / 60000;
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

        return xaDataAS7;
    }

    /**
     * Creating CLI script for adding Datasource. Old format of script.
     *
     * @param datasourceAS7 object of Datasource
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createDatasourceScriptOld(DatasourceAS7Bean datasourceAS7)
            throws CliScriptException {
        String errMsg = " in datasource must be set.";
        Utils.throwIfBlank(datasourceAS7.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(datasourceAS7.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(datasourceAS7.getConnectionUrl(), errMsg, "Connection url");
        Utils.throwIfBlank(datasourceAS7.getDriver(), errMsg, "Driver name");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=datasources/data-source=");

        resultScript.append(datasourceAS7.getPoolName()).append(":add(");

        builder.addProperty("jndi-name", datasourceAS7.getJndiName());

        //builder.addProperty("enabled", datasourceAS7.getEnabled());

        builder.addProperty("use-java-context", datasourceAS7.getUseJavaContext());
        builder.addProperty("driver-name", datasourceAS7.getDriver());
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

        resultScript.append(builder.asString()).append(")\n");
        resultScript.append("data-source enable --name=").append(datasourceAS7.getPoolName());

        return resultScript.toString();
    }

    /**
     * Creating CLI script for adding XaDatsource. Old format of script.
     *
     * @param xaDatasourceAS7 object of XaDatasource
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createXaDatasourceScriptOld(XaDatasourceAS7Bean xaDatasourceAS7)
            throws CliScriptException {
        String errMsg = " in xaDatasource must be set.";
        Utils.throwIfBlank(xaDatasourceAS7.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(xaDatasourceAS7.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(xaDatasourceAS7.getDriver(), errMsg, "Driver name");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=datasources/xa-data-source=");

        resultScript.append(xaDatasourceAS7.getPoolName()).append(":add(");

        builder.addProperty("jndi-name", xaDatasourceAS7.getJndiName());

        //builder.addProperty("enabled", xaDatasourceAS7.getEnabled());

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
        builder.addProperty("idle-timeout-minutes", xaDatasourceAS7.getIdleTimeoutMinutes());
        builder.addProperty("set-tx-query-timeout", xaDatasourceAS7.getSetTxQueryTimeout());
        builder.addProperty("query-timeout", xaDatasourceAS7.getQueryTimeout());
        builder.addProperty("allocation-retry", xaDatasourceAS7.getAllocationRetry());
        builder.addProperty("allocation-retry-wait-millis", xaDatasourceAS7.getAllocRetryWaitMillis());
        builder.addProperty("use-try-lock", xaDatasourceAS7.getUseTryLock());
        builder.addProperty("xa-resource-timeout", xaDatasourceAS7.getXaResourceTimeout());
        builder.addProperty("prepared-statement-cache-size", xaDatasourceAS7.getPreStatementCacheSize());
        builder.addProperty("track-statements", xaDatasourceAS7.getTrackStatements());
        builder.addProperty("share-prepared-statements", xaDatasourceAS7.getSharePreStatements());

        resultScript.append(builder.asString()).append(")\n");

        if (xaDatasourceAS7.getXaDatasourceProps() != null) {
            for (XaDatasourcePropertyBean xaDatasourceProperty : xaDatasourceAS7.getXaDatasourceProps()) {
                errMsg = "in xa-datasource property must be set";
                Utils.throwIfBlank(xaDatasourceProperty.getXaDatasourcePropName(), errMsg, "Property name");

                resultScript.append("/subsystem=datasources/xa-data-source=").append(xaDatasourceAS7.getPoolName());
                resultScript.append("/xa-datasource-properties=").append(xaDatasourceProperty.getXaDatasourcePropName());
                resultScript.append(":add(value=").append(xaDatasourceProperty.getXaDatasourceProp()).append(")\n");

            }
        }

        resultScript.append("xa-data-source enable --name=").append(xaDatasourceAS7.getPoolName());

        return resultScript.toString();
    }

    /**
     * Creating CLI script for adding DriverBean
     *
     * @param driver object of DriverBean
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createDriverScript(DriverBean driver) throws CliScriptException {
        String errMsg = " in driver must be set.";
        Utils.throwIfBlank(driver.getDriverModule(), errMsg, "Module");
        Utils.throwIfBlank(driver.getDriverName(), errMsg, "Driver-name");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=datasources/jdbc-driver=");

        resultScript.append(driver.getDriverName()).append(":add(");
        resultScript.append("driver-module-name=").append(driver.getDriverModule());

        builder.addProperty("driver-class-name", driver.getDriverClass());
        builder.addProperty("driver-xa-datasource-class-name", driver.getXaDatasourceClass());
        builder.addProperty("driver-major-version", driver.getMajorVersion());
        builder.addProperty("driver-minor-version", driver.getMinorVersion());

        resultScript.append(builder.asString()).append(")");

        return resultScript.toString();
    }

    /**
     * Creating CLI script for adding Datasource. New format of script.
     *
     * @param datasourceAS7 object of Datasource
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createDatasourceScriptNew(DatasourceAS7Bean datasourceAS7) throws CliScriptException {
        String errMsg = " in datasource must be set.";
        Utils.throwIfBlank(datasourceAS7.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(datasourceAS7.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(datasourceAS7.getConnectionUrl(), errMsg, "Connection url");
        Utils.throwIfBlank(datasourceAS7.getDriver(), errMsg, "Driver name");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        StringBuilder resultScript = new StringBuilder("data-source add ");

        builder.addProperty("name", datasourceAS7.getPoolName());
        builder.addProperty("jndi-name", datasourceAS7.getJndiName());

        //builder.addProperty("--enabled", datasourceAS7.getEnabled());

        builder.addProperty("use-java-context", datasourceAS7.getUseJavaContext());
        builder.addProperty("driver-name", datasourceAS7.getDriver());
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

        resultScript.append(builder.asStringDriverNew());
        resultScript.append("\n");
        resultScript.append("data-source enable --name=").append(datasourceAS7.getPoolName());

        return resultScript.toString();
    }


    /**
     * Creating CLI script for adding XaDatsource. New format of script.
     *
     * @param xaDatasourceAS7 object of XaDatasource
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createXaDatasourceScriptNew(XaDatasourceAS7Bean xaDatasourceAS7) throws CliScriptException {
        String errMsg = " in xaDatasource must be set.";
        Utils.throwIfBlank(xaDatasourceAS7.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(xaDatasourceAS7.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(xaDatasourceAS7.getDriver(), errMsg, "Driver name");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        StringBuilder resultScript = new StringBuilder("xa-data-source add");

        builder.addProperty("name", xaDatasourceAS7.getPoolName());
        builder.addProperty("jndi-name", xaDatasourceAS7.getJndiName());

        //builder.addProperty("--enabled", xaDatasourceAS7.getEnabled());

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
        builder.addProperty("idle-timeout-minutes", xaDatasourceAS7.getIdleTimeoutMinutes());
        builder.addProperty("set-tx-query-timeout", xaDatasourceAS7.getSetTxQueryTimeout());
        builder.addProperty("query-timeout", xaDatasourceAS7.getQueryTimeout());
        builder.addProperty("allocation-retry", xaDatasourceAS7.getAllocationRetry());
        builder.addProperty("allocation-retry-wait-millis", xaDatasourceAS7.getAllocRetryWaitMillis());
        builder.addProperty("use-try-lock", xaDatasourceAS7.getUseTryLock());
        builder.addProperty("xa-resource-timeout", xaDatasourceAS7.getXaResourceTimeout());
        builder.addProperty("prepared-statement-cache-size", xaDatasourceAS7.getPreStatementCacheSize());
        builder.addProperty("track-statements", xaDatasourceAS7.getTrackStatements());
        builder.addProperty("share-prepared-statements", xaDatasourceAS7.getSharePreStatements());

        resultScript.append(builder.asStringDriverNew());
        resultScript.append("\n");

        if (xaDatasourceAS7.getXaDatasourceProps() != null) {
            for (XaDatasourcePropertyBean xaDatasourceProperty : xaDatasourceAS7.getXaDatasourceProps()) {
                errMsg = "in xa-datasource property must be set";
                Utils.throwIfBlank(xaDatasourceProperty.getXaDatasourcePropName(), errMsg, "Property name");

                resultScript.append("/subsystem=datasources/xa-data-source=").append(xaDatasourceAS7.getPoolName());
                resultScript.append("/xa-datasource-properties=").append(xaDatasourceProperty.getXaDatasourcePropName());
                resultScript.append(":add(value=").append(xaDatasourceProperty.getXaDatasourceProp()).append(")\n");

            }
        }

        resultScript.append("xa-data-source enable --name=").append(xaDatasourceAS7.getPoolName());

        return resultScript.toString();
    }
}
