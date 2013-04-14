package cz.muni.fi.jboss.migration.migrators.dataSources;

import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.actions.CliCommandAction;
import cz.muni.fi.jboss.migration.actions.IMigrationAction;
import cz.muni.fi.jboss.migration.actions.ModuleCreationAction;
import cz.muni.fi.jboss.migration.conf.GlobalConfiguration;
import cz.muni.fi.jboss.migration.ex.*;
import cz.muni.fi.jboss.migration.migrators.dataSources.jaxb.*;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Migrator of Datasource subsystem implementing IMigrator
 *
 * @author Roman Jakubco
 */

public class DatasourceMigrator extends AbstractMigrator {
    private static final Logger log = LoggerFactory.getLogger(DatasourceMigrator.class);

    @Override
    protected String getConfigPropertyModuleName() {
        return "datasource";
    }


    private static final String ROOT_ELEMENT_NAME = "datasources";

    // iterating number for names of drivers
    // TODO: Perhaps move this property to migration context.
    private int it = 1;

    private Set<DriverBean> drivers = new HashSet();

    public DatasourceMigrator(GlobalConfiguration globalConfig, MultiValueMap config) {
        super(globalConfig, config);

    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException {
        try {

            // Get a list of -ds.xml files.
            File dsFiles = getGlobalConfig().getAS5Config().getDeployDir();
            if (!dsFiles.canRead())
                throw new LoadMigrationException("Can't read: " + dsFiles);

            SuffixFileFilter sf = new SuffixFileFilter("-ds.xml");
            Collection<File> dsXmls = FileUtils.listFiles(dsFiles, sf, FileFilterUtils.trueFileFilter());
            log.debug("  Found -ds.xml files #: " + dsXmls.size());
            if (dsXmls.isEmpty())
                return;

            List<DatasourcesBean> dsColl = new ArrayList();
            Unmarshaller dataUnmarshaller = JAXBContext.newInstance(DatasourcesBean.class).createUnmarshaller();

            for (File dsXml : dsXmls) {
                Document doc = Utils.parseXmlToDoc(dsXml);

                Element element = doc.getDocumentElement();
                if (ROOT_ELEMENT_NAME.equals(element.getTagName())) {
                    DatasourcesBean dataSources = (DatasourcesBean) dataUnmarshaller.unmarshal(dsXml);
                    dsColl.add(dataSources);
                }
            }

            MigrationData mData = new MigrationData();

            for (DatasourcesBean ds : dsColl) {
                if (ds.getLocalDatasourceAS5s() != null) {
                    mData.getConfigFragments().addAll(ds.getLocalDatasourceAS5s());
                }

                if (ds.getXaDatasourceAS5s() != null) {
                    mData.getConfigFragments().addAll(ds.getXaDatasourceAS5s());
                }

                if (ds.getNoTxDatasourceAS5s() != null) {
                    mData.getConfigFragments().addAll(ds.getNoTxDatasourceAS5s());
                }

            }

            ctx.getMigrationData().put(DatasourceMigrator.class, mData);

        } catch (JAXBException | SAXException | IOException ex) {
            throw new LoadMigrationException(ex);
        }
    }

    @Override
    public void createActions(MigrationContext ctx) throws ActionException {
        // Helping list of CliCommnadAction. For successful migration driver CliCommandAction must be added=performed
        // before datasource.
        List<IMigrationAction> tempActions = new ArrayList<>();
        for (IConfigFragment fragment : ctx.getMigrationData().get(DatasourceMigrator.class).getConfigFragments()) {
            if (fragment instanceof DatasourceAS5Bean) {
                try {
                    tempActions.add(createDatasourceCliAction(migrateLocalTxDatasource((DatasourceAS5Bean) fragment)));
                } catch (CliScriptException e) {
                    throw new ActionException("Migration of local-tx-datasource failed: " + e.getMessage(), e);
                }
                continue;
            }

            if (fragment instanceof XaDatasourceAS5Bean) {
                try {
                    tempActions.addAll(createXaDatasourceCliAction(migrateXaDatasource((XaDatasourceAS5Bean) fragment)));
                } catch (CliScriptException e) {
                    throw new ActionException("Migration of xa-datasource failed: " + e.getMessage(), e);
                }
                continue;
            }

            if (fragment instanceof NoTxDatasourceAS5Bean) {
                try {
                    tempActions.add(createDatasourceCliAction(migrateNoTxDatasource((NoTxDatasourceAS5Bean) fragment)));
                } catch (CliScriptException e) {
                    throw new ActionException("Migration of no-tx-datasource failed: " + e.getMessage(), e);
                }
                continue;
            }
            throw new ActionException("Config fragment unrecognized by " + this.getClass().getSimpleName() + ": " + fragment);
        }

        HashMap<File, String> tempModules = new HashMap();

        for (DriverBean driver : this.drivers) {
            // New approach to drivers. Similar to finding logging classes. Search for driver class in jars and create module
            ctx.getActions().addAll(createDriverActions(driver, tempModules));
        }

        // Add datasource CliCommandActions after drivers.
        ctx.getActions().addAll(tempActions);
    }

    private List<IMigrationAction> createDriverActions(DriverBean driver, HashMap<File, String> tempModules)
            throws ActionException {
        File src;
        try {
            src = driver.getDriverClass() != null
                    ? Utils.findJarFileWithClass(driver.getDriverClass(), getGlobalConfig().getAS5Config().getDir(),
                    getGlobalConfig().getAS5Config().getProfileName())
                    : Utils.findJarFileWithClass(driver.getXaDatasourceClass(), getGlobalConfig().getAS5Config().getDir(),
                    getGlobalConfig().getAS5Config().getProfileName());

        } catch (IOException e) {
            throw new ActionException("Finding jar containing driver class failed: " + e.getMessage(), e);
        }

        List<IMigrationAction> actions = new ArrayList();

        if (tempModules.containsKey(src)) {
            // It means that moduleAction is already set. No need for another one => create CLI for driver and
            // continue on the next iteration
            try {
                driver.setDriverModule(tempModules.get(src));
                actions.add(createDriverCliAction(driver));

            } catch (CliScriptException e) {
                throw new ActionException("Migration of driver failed (CLI command): " + e.getMessage(), e);
            }
        } else {
            try {
                // Driver file is new => create ModuleCreationAction, new module and CLI script for driver
                driver.setDriverModule("migration.drivers." + driver.getDriverName());
                tempModules.put(src, driver.getDriverModule());

                actions.add(createDriverCliAction(driver));

                File targetDir = Utils.createPath(getGlobalConfig().getAS7Config().getDir(), "modules", "migration",
                        "drivers", driver.getDriverName(), "main", src.getName());

                Document doc = createDriverModuleXML(driver.getDriverModule(), src.getName());

                // Default for now => false
                ModuleCreationAction moduleAction = new ModuleCreationAction(src, targetDir, doc, false);

                actions.add(moduleAction);

            } catch (ParserConfigurationException e) {
                throw new ActionException("Creation of Document representing module.xml for driver failed: "
                        + e.getMessage(), e);
            } catch (CliScriptException e) {
                throw new ActionException("Migration of driver failed (CLI command): " + e.getMessage(), e);
            }
        }

        return actions;
    }

    @Override
    public void apply(MigrationContext ctx) throws ApplyMigrationException {
        try {
            Document doc = ctx.getAS7ConfigXmlDoc();
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

            for (IConfigFragment fragment : ctx.getMigrationData().get(DatasourceMigrator.class).getConfigFragments()) {
                Document doc = Utils.createXmlDocumentBuilder().newDocument();

                if (fragment instanceof DatasourceAS5Bean) {
                    dataMarshaller.marshal(migrateLocalTxDatasource((DatasourceAS5Bean) fragment), doc);
                    nodeList.add(doc.getDocumentElement());
                    continue;
                }
                if (fragment instanceof XaDatasourceAS5Bean) {
                    xaDataMarshaller.marshal(migrateXaDatasource((XaDatasourceAS5Bean) fragment), doc);
                    nodeList.add(doc.getDocumentElement());
                    continue;
                }
                if (fragment instanceof NoTxDatasourceAS5Bean) {
                    dataMarshaller.marshal(migrateNoTxDatasource((NoTxDatasourceAS5Bean) fragment), doc);
                    nodeList.add(doc.getDocumentElement());
                    continue;
                }

                throw new NodeGenerationException("Config fragment unrecognized by " + this.getClass().getSimpleName() + ": " + fragment);
            }


            for (DriverBean driver : this.drivers) {
                FileTransferInfo rollbackData = new FileTransferInfo();
                rollbackData.setType(FileTransferInfo.Type.DRIVER);

                if (driver.getDriverClass() != null) {
                    //rollbackData.deriveDriverName(driver.getDriverClass());
                    DatasourceUtils.deriveAndSetDriverName(rollbackData, driver.getDriverClass());
                    rollbackData.setModuleName(DatasourceUtils.deriveDriverModuleName(driver.getDriverClass()));
                    driver.setDriverModule(DatasourceUtils.deriveDriverModuleName(driver.getDriverClass()));
                } else {
                    rollbackData.setName(driver.getXaDatasourceClass());
                    rollbackData.setModuleName(DatasourceUtils.deriveDriverModuleName(driver.getXaDatasourceClass()));
                    driver.setDriverModule(DatasourceUtils.deriveDriverModuleName(driver.getXaDatasourceClass()));
                }

                ctx.getFileTransfers().add(rollbackData);

                Document doc = Utils.createXmlDocumentBuilder().newDocument();
                driverMarshaller.marshal(driver, doc);
                nodeList.add(doc.getDocumentElement());
            }

            return nodeList;
        } catch (JAXBException e) {
            throw new NodeGenerationException(e);
        }
    }// generateDomElements()

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
    }// generateCliScripts()


    /**
     * Migrates a No-Tx-Datasource from AS5 to AS7
     *
     * @param noTxDatasourceAS5 object representing No-Tx-Datasource in AS5
     * @return object representing migrated Datasource in AS7
     */
    public DatasourceAS7Bean migrateNoTxDatasource(NoTxDatasourceAS5Bean noTxDatasourceAS5) {
        DatasourceAS7Bean datasourceAS7 = new DatasourceAS7Bean();

        // Setting name for driver
        DriverBean driver = new DriverBean();
        driver.setDriverClass(noTxDatasourceAS5.getDriverClass());
        if (this.drivers.add(driver)) {
            datasourceAS7.setDriver("createdDriver" + this.it);
            driver.setDriverName("createdDriver" + this.it);
            this.it++;
        } else {
            for (DriverBean temp : this.drivers) {
                if (temp.equals(driver)) {
                    datasourceAS7.setDriver(temp.getDriverName());
                    break;
                }
            }
        }

        //this.drivers.add(noTxDatasourceAS5.getDriverClass());

        // Standalone elements in AS7
        datasourceAS7.setJta("false");
        datasourceAS7.setJndiName("java:jboss/datasources/" + noTxDatasourceAS5.getJndiName());
        datasourceAS7.setPoolName(noTxDatasourceAS5.getJndiName());
        datasourceAS7.setEnabled("true");
        datasourceAS7.setUseJavaContext(noTxDatasourceAS5.getUseJavaContext());
        datasourceAS7.setUrlDelimeter(noTxDatasourceAS5.getUrlDelimeter());
        datasourceAS7.setUrlSelector(noTxDatasourceAS5.getUrlSelectStratClName());
        datasourceAS7.setConnectionUrl(noTxDatasourceAS5.getConnectionUrl());

        if (noTxDatasourceAS5.getConnectionProperties() != null) {
            datasourceAS7.setConnectionProperties(noTxDatasourceAS5.getConnectionProperties());
        }

        datasourceAS7.setNewConnectionSql(noTxDatasourceAS5.getNewConnectionSql());

        // Elements in element <security> in AS7
        datasourceAS7.setUserName(noTxDatasourceAS5.getUserName());
        datasourceAS7.setPassword(noTxDatasourceAS5.getPassword());

        datasourceAS7.setSecurityDomain(noTxDatasourceAS5.getSecurityDomain());

        // Elements in element <pool> in AS7
        datasourceAS7.setMinPoolSize(noTxDatasourceAS5.getMinPoolSize());
        datasourceAS7.setMaxPoolSize(noTxDatasourceAS5.getMaxPoolSize());
        datasourceAS7.setPrefill(noTxDatasourceAS5.getPrefill());

        // Elements in element <timeout> in AS7
        datasourceAS7.setBlockingTimeoutMillis(noTxDatasourceAS5.getBlockingTimeMillis());
        datasourceAS7.setIdleTimeoutMin(noTxDatasourceAS5.getIdleTimeoutMinutes());
        datasourceAS7.setQueryTimeout(noTxDatasourceAS5.getQueryTimeout());
        datasourceAS7.setAllocationRetry(noTxDatasourceAS5.getAllocationRetry());
        datasourceAS7.setAllocRetryWaitMillis(noTxDatasourceAS5.getAllocRetryWaitMillis());
        datasourceAS7.setSetTxQueryTimeout(noTxDatasourceAS5.getSetTxQueryTime());
        datasourceAS7.setUseTryLock(noTxDatasourceAS5.getUseTryLock());

        // Elements in element <validation> in AS7
        datasourceAS7.setCheckValidConSql(noTxDatasourceAS5.getCheckValidConSql());
        datasourceAS7.setValidateOnMatch(noTxDatasourceAS5.getValidateOnMatch());
        datasourceAS7.setBackgroundValid(noTxDatasourceAS5.getBackgroundValid());
        datasourceAS7.setExceptionSorter(noTxDatasourceAS5.getExcepSorterClName());
        datasourceAS7.setValidConChecker(noTxDatasourceAS5.getValidConCheckerClName());
        datasourceAS7.setStaleConChecker(noTxDatasourceAS5.getStaleConCheckerClName());
        // Millis represents Milliseconds?
        if (noTxDatasourceAS5.getBackgroundValidMillis() != null) {
            Integer tmp = Integer.valueOf(noTxDatasourceAS5.getBackgroundValidMillis()) / 60000;
            datasourceAS7.setBackgroundValidMin(tmp.toString());

        }

        // Elements in element <statement> in AS7
        datasourceAS7.setTrackStatements(noTxDatasourceAS5.getTrackStatements());
        datasourceAS7.setSharePreStatements(noTxDatasourceAS5.getSharePreStatements());
        datasourceAS7.setQueryTimeout(noTxDatasourceAS5.getQueryTimeout());

        // Strange element use-fast-fail
        //datasourceAS7.setUseFastFail(datasourceAS5.gF);

        return datasourceAS7;
    }// migrateNoTxDatasource()


    /**
     * Migrates a Local-Tx-Datasource from AS5 to AS7
     *
     * @param datasourceAS5 object representing Local-Tx-Datasource in AS5
     * @return object representing migrated Datasource in AS7
     */
    public DatasourceAS7Bean migrateLocalTxDatasource(DatasourceAS5Bean datasourceAS5) {
        DatasourceAS7Bean datasourceAS7 = new DatasourceAS7Bean();

        DriverBean driver = new DriverBean();
        driver.setDriverClass(datasourceAS5.getDriverClass());
        if (this.drivers.add(driver)) {
            datasourceAS7.setDriver("createdDriver" + this.it);
            driver.setDriverName("createdDriver" + this.it);
            this.it++;
        } else {
            for (DriverBean temp : this.drivers) {
                if (temp.equals(driver)) {
                    datasourceAS7.setDriver(temp.getDriverName());
                    break;
                }
            }
        }

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

        // Elements in element <security> in AS7
        datasourceAS7.setUserName(datasourceAS5.getUserName());
        datasourceAS7.setPassword(datasourceAS5.getPassword());

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

    }// migrateLocalTxDatasource()


    /**
     * Migrates a Xa-Datasource from AS5 to AS7
     *
     * @param xaDataAS5 object representing Xa-Datasource in AS5
     * @return object representing migrated Xa-Datasource in AS7
     */
    public XaDatasourceAS7Bean migrateXaDatasource(XaDatasourceAS5Bean xaDataAS5) {
        XaDatasourceAS7Bean xaDataAS7 = new XaDatasourceAS7Bean();

        xaDataAS7.setJndiName("java:jboss/datasources/" + xaDataAS5.getJndiName());
        xaDataAS7.setPoolName(xaDataAS5.getJndiName());
        xaDataAS7.setUseJavaContext(xaDataAS5.getUseJavaContext());
        xaDataAS7.setEnabled("true");

        DriverBean driver = new DriverBean();
        driver.setXaDatasourceClass(xaDataAS5.getXaDatasourceClass());
        if (this.drivers.add(driver)) {
            xaDataAS7.setDriver("createdDriver" + this.it);
            driver.setDriverName("createdDriver" + this.it);
            this.it++;
        } else {
            for (DriverBean temp : this.drivers) {
                if (temp.equals(driver)) {
                    xaDataAS7.setDriver(temp.getDriverName());
                    break;
                }
            }
        }

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

    }// migrateXaDatasource()

    /**
     * Creates CliCommandAction for adding a Datasource
     *
     * @param dataSource Datasource for adding
     * @return created CliCommandAction for adding the Datasource
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Datasource
     *                            are missing or are empty (pool-name, jndi-name, connection-url, driver-name)
     */
    public static CliCommandAction createDatasourceCliAction(DatasourceAS7Bean dataSource)
            throws CliScriptException {
        String errMsg = " in datasource must be set.";
        Utils.throwIfBlank(dataSource.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(dataSource.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(dataSource.getConnectionUrl(), errMsg, "Connection url");
        Utils.throwIfBlank(dataSource.getDriver(), errMsg, "Driver name");

        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        request.get(ClientConstants.OP_ADDR).add("data-source", dataSource.getPoolName());

        CliApiCommandBuilder builder = new CliApiCommandBuilder(request);
        builder.addProperty("jndi-name", dataSource.getJndiName());

        // TODO: Try if property enabled works
        builder.addProperty("enabled", "true");

        builder.addProperty("jta", dataSource.getJta());
        builder.addProperty("use-java-context", dataSource.getUseJavaContext());
        builder.addProperty("driver-name", dataSource.getDriver());
        builder.addProperty("connection-url", dataSource.getConnectionUrl());
        builder.addProperty("url-delimeter", dataSource.getUrlDelimeter());
        builder.addProperty("url-selector-strategy-class-name", dataSource.getUrlSelector());
        builder.addProperty("transaction-isolation", dataSource.getTransIsolation());
        builder.addProperty("new-connection-sql", dataSource.getNewConnectionSql());
        builder.addProperty("prefill", dataSource.getPrefill());
        builder.addProperty("min-pool-size", dataSource.getMinPoolSize());
        builder.addProperty("max-pool-size", dataSource.getMaxPoolSize());
        builder.addProperty("password", dataSource.getPassword());
        builder.addProperty("user-name", dataSource.getUserName());
        builder.addProperty("security-domain", dataSource.getSecurityDomain());
        builder.addProperty("check-valid-connection-sql", dataSource.getCheckValidConSql());
        builder.addProperty("validate-on-match", dataSource.getValidateOnMatch());
        builder.addProperty("background-validation", dataSource.getBackgroundValid());
        builder.addProperty("background-validation-minutes", dataSource.getBackgroundValidMin());
        builder.addProperty("use-fast-fail", dataSource.getUseFastFail());
        builder.addProperty("exception-sorter-class-name", dataSource.getExceptionSorter());
        builder.addProperty("valid-connection-checker-class-name", dataSource.getValidateOnMatch());
        builder.addProperty("stale-connection-checker-class-name", dataSource.getStaleConChecker());
        builder.addProperty("blocking-timeout-millis", dataSource.getBlockingTimeoutMillis());
        builder.addProperty("idle-timeout-minutes", dataSource.getIdleTimeoutMin());
        builder.addProperty("set-tx-query-timeout", dataSource.getSetTxQueryTimeout());
        builder.addProperty("query-timeout", dataSource.getQueryTimeout());
        builder.addProperty("allocation-retry", dataSource.getAllocationRetry());
        builder.addProperty("allocation-retry-wait-millis", dataSource.getAllocRetryWaitMillis());
        builder.addProperty("use-try-lock", dataSource.getUseTryLock());
        builder.addProperty("prepared-statement-cache-size", dataSource.getPreStatementCacheSize());
        builder.addProperty("track-statements", dataSource.getTrackStatements());
        builder.addProperty("share-prepared-statements", dataSource.getSharePreStatements());

        //return builder.getCommand();
        return new CliCommandAction(createDatasourceScriptNew(dataSource), builder.getCommand());
    }

    /**
     * Creates a list of CliCommandActions for adding a Xa-Datasource
     *
     * @param dataSource Xa-Datasource for adding
     * @return list containing CliCommandActions for adding the Xa-Datasource
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Xa-Datasource
     *                            are missing or are empty (pool-name, jndi-name, driver-name)
     */
    public static List<CliCommandAction> createXaDatasourceCliAction(XaDatasourceAS7Bean dataSource)
            throws CliScriptException {
        String errMsg = " in xaDatasource must be set.";
        Utils.throwIfBlank(dataSource.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(dataSource.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(dataSource.getDriver(), errMsg, "Driver name");

        List<CliCommandAction> actions = new ArrayList();

        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        request.get(ClientConstants.OP_ADDR).add("xa-data-source", dataSource.getPoolName());

        CliApiCommandBuilder builder = new CliApiCommandBuilder(request);

        builder.addProperty("jndi-name", dataSource.getJndiName());
        builder.addProperty("use-java-context", dataSource.getUseJavaContext());
        builder.addProperty("driver-name", dataSource.getDriver());
        builder.addProperty("url-delimeter", dataSource.getUrlDelimeter());
        builder.addProperty("url-selector-strategy-class-name", dataSource.getUrlSelector());
        builder.addProperty("transaction-isolation", dataSource.getTransIsolation());
        builder.addProperty("new-connection-sql", dataSource.getNewConnectionSql());
        builder.addProperty("prefill", dataSource.getPrefill());
        builder.addProperty("min-pool-size", dataSource.getMinPoolSize());
        builder.addProperty("max-pool-size", dataSource.getMaxPoolSize());
        builder.addProperty("is-same-rm-override", dataSource.getSameRmOverride());
        builder.addProperty("interleaving", dataSource.getInterleaving());
        builder.addProperty("no-tx-separate-pools", dataSource.getNoTxSeparatePools());
        builder.addProperty("password", dataSource.getPassword());
        builder.addProperty("user-name", dataSource.getUserName());
        builder.addProperty("security-domain", dataSource.getSecurityDomain());
        builder.addProperty("check-valid-connection-sql", dataSource.getCheckValidConSql());
        builder.addProperty("validate-on-match", dataSource.getValidateOnMatch());
        builder.addProperty("background-validation", dataSource.getBackgroundValid());
        builder.addProperty("background-validation-minutes", dataSource.getBackgroundValidMin());
        builder.addProperty("use-fast-fail", dataSource.getUseFastFail());
        builder.addProperty("exception-sorter-class-name", dataSource.getExceptionSorter());
        builder.addProperty("valid-connection-checker-class-name", dataSource.getValidateOnMatch());
        builder.addProperty("stale-connection-checker-class-name", dataSource.getStaleConChecker());
        builder.addProperty("blocking-timeout-millis", dataSource.getBlockingTimeoutMillis());
        builder.addProperty("idle-timeout-minutes", dataSource.getIdleTimeoutMinutes());
        builder.addProperty("set-tx-query-timeout", dataSource.getSetTxQueryTimeout());
        builder.addProperty("query-timeout", dataSource.getQueryTimeout());
        builder.addProperty("allocation-retry", dataSource.getAllocationRetry());
        builder.addProperty("allocation-retry-wait-millis", dataSource.getAllocRetryWaitMillis());
        builder.addProperty("use-try-lock", dataSource.getUseTryLock());
        builder.addProperty("xa-resource-timeout", dataSource.getXaResourceTimeout());
        builder.addProperty("prepared-statement-cache-size", dataSource.getPreStatementCacheSize());
        builder.addProperty("track-statements", dataSource.getTrackStatements());
        builder.addProperty("share-prepared-statements", dataSource.getSharePreStatements());

        actions.add(new CliCommandAction(createXaDatasourceScriptNew(dataSource), builder.getCommand()));

        if (dataSource.getXaDatasourceProps() != null) {
            for (XaDatasourcePropertyBean property : dataSource.getXaDatasourceProps()) {
                actions.add(createXaPropertyCliAction(dataSource, property));
            }
        }

        return actions;
    }


    /**
     * Creates CliCommandAction for adding a Xa-Datasource-Property of the specific Xa-Datasource
     *
     * @param datasource Xa-Datasource containing Xa-Datasource-Property
     * @param property   Xa-Datasource-property
     * @return created CliCommandAction for adding the Xa-Datasource-Property
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Xa-Datasource-Property
     *                            are missing or are empty (property-name)
     */
    public static CliCommandAction createXaPropertyCliAction(XaDatasourceAS7Bean datasource, XaDatasourcePropertyBean property)
            throws CliScriptException {
        String errMsg = "in xa-datasource property must be set";
        Utils.throwIfBlank(property.getXaDatasourcePropName(), errMsg, "Property name");

        ModelNode connProperty = new ModelNode();
        connProperty.get(ClientConstants.OP).set(ClientConstants.ADD);
        connProperty.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        connProperty.get(ClientConstants.OP_ADDR).add("xa-data-source", datasource.getPoolName());

        connProperty.get(ClientConstants.OP_ADDR).add
                ("xa-datasource-properties", property.getXaDatasourcePropName());
        connProperty.get("value").set(property.getXaDatasourceProp());

        return new CliCommandAction(createXaPropertyScript(datasource, property), connProperty);
    }

    /**
     * Creates CliCommandAction for adding a Driver
     *
     * @param driver object representing Driver
     * @return created CliCommandAction for adding the Driver
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Driver are missing or
     *                            are empty (module, driver-name)
     */
    public static CliCommandAction createDriverCliAction(DriverBean driver) throws CliScriptException {
        String errMsg = " in driver must be set.";
        Utils.throwIfBlank(driver.getDriverModule(), errMsg, "Module");
        Utils.throwIfBlank(driver.getDriverName(), errMsg, "Driver-name");

        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem", "datasources");
        request.get(ClientConstants.OP_ADDR).add("jdbc-driver", driver.getDriverName());

        CliApiCommandBuilder builder = new CliApiCommandBuilder(request);

        builder.addProperty("driver-module-name", driver.getDriverModule());
        builder.addProperty("driver-class-name", driver.getDriverClass());
        builder.addProperty("driver-xa-datasource-class-name", driver.getXaDatasourceClass());
        builder.addProperty("driver-major-version", driver.getMajorVersion());
        builder.addProperty("driver-minor-version", driver.getMinorVersion());

        return new CliCommandAction(createDriverScript(driver), builder.getCommand());
    }

    /**
     * Creates CLI script for adding Datasource. Old format of script.
     *
     * @param datasourceAS7 object of Datasource
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes for creation of the CLI script are missing or are empty
     *                            (property-name)
     */
    private static String createDatasourceScriptOld(DatasourceAS7Bean datasourceAS7)
            throws CliScriptException {
        String errMsg = " in datasource must be set.";
        Utils.throwIfBlank(datasourceAS7.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(datasourceAS7.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(datasourceAS7.getConnectionUrl(), errMsg, "Connection url");
        Utils.throwIfBlank(datasourceAS7.getDriver(), errMsg, "Driver name");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=datasources/data-source=");

        resultScript.append(datasourceAS7.getPoolName()).append(":add(");

        builder.addProperty("jndi-name", datasourceAS7.getJndiName());
        builder.addProperty("jta", datasourceAS7.getJta());
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
     * Creates CLI script for adding XaDatsource. Old format of script.
     *
     * @param xaDatasourceAS7 object of XaDatasource
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes for creation of the CLI script are missing or are empty
     *                            (pool-name, jndi-name, driver-name)
     */
    private static String createXaDatasourceScriptOld(XaDatasourceAS7Bean xaDatasourceAS7)
            throws CliScriptException {
        String errMsg = " in xaDatasource must be set.";
        Utils.throwIfBlank(xaDatasourceAS7.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(xaDatasourceAS7.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(xaDatasourceAS7.getDriver(), errMsg, "Driver name");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=datasources/xa-data-source=");

        resultScript.append(xaDatasourceAS7.getPoolName()).append(":add(");

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
     * Creates a CLI script for adding a Driver
     *
     * @param driver object of DriverBean
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes for creation of the CLI script are missing or are empty
     *                            (module, driver-name)
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
     *                            (pool-name, jndi-name, connection-url, driver-name)
     */
    private static String createDatasourceScriptNew(DatasourceAS7Bean datasourceAS7) throws CliScriptException {
        String errMsg = " in datasource must be set.";
        Utils.throwIfBlank(datasourceAS7.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(datasourceAS7.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(datasourceAS7.getConnectionUrl(), errMsg, "Connection url");
        Utils.throwIfBlank(datasourceAS7.getDriver(), errMsg, "Driver name");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("data-source add ");

        builder.addProperty("name", datasourceAS7.getPoolName());
        builder.addProperty("jndi-name", datasourceAS7.getJndiName());
        builder.addProperty("jta", datasourceAS7.getJta());
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
     *                            (pool-name, jndi-name, driver-name)
     */
    private static String createXaDatasourceScriptNew(XaDatasourceAS7Bean xaDatasourceAS7) throws CliScriptException {
        String errMsg = " in xaDatasource must be set.";
        Utils.throwIfBlank(xaDatasourceAS7.getPoolName(), errMsg, "Pool-name");
        Utils.throwIfBlank(xaDatasourceAS7.getJndiName(), errMsg, "Jndi-name");
        Utils.throwIfBlank(xaDatasourceAS7.getDriver(), errMsg, "Driver name");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("xa-data-source add");

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

        resultScript.append(builder.asStringNew());
        //resultScript.append("\n");

        // TODO: Not sure if set datasource enabled. For now I don't know way enabling datasource in CLI API
        //resultScript.append("xa-data-source enable --name=").append(xaDatasourceAS7.getPoolName());

        return resultScript.toString();
    }

    /**
     * Creates a CLI script for adding one Xa-Datasource-Property of the specific Xa-Datasource
     *
     * @param datasource           Xa-Datasource containing Xa-Datasource-Property
     * @param xaDatasourceProperty Xa-Datasource-Property
     * @return created string containing CLI script for adding Xa-Datasource-Property
     * @throws CliScriptException if required attributes for creation of the CLI script are missing or are empty
     *                            (property-name)
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
     * Method for creating module.xml for JDBC drivers, which will be copied to modules in AS7
     *
     * @param moduleName name of the created module
     * @param fileName   name of the file deployed as module
     * @return Document representing created module.xml for given driver
     * @throws javax.xml.parsers.ParserConfigurationException
     *          if parser cannot be initialized
     */
    private static Document createDriverModuleXML(String moduleName, String fileName) throws ParserConfigurationException {

        /**
         * module.xml for JDBC driver module
         *
         * Example of module xml,
         *  <module xmlns="urn:jboss:module:1.1" name="com.h2database.h2">
         *       <resources>
         *          <resource-root path="h2-1.3.168.jar"/>
         *       <!-- Insert resources here -->
         *       </resources>
         *       <dependencies>
         *          <module name="javax.api"/>
         *          <module name="javax.transaction.api"/>
         *          <module name="javax.servlet.api" optional="true"/>
         *       </dependencies>
         *  </module>
         */
        Document doc = Utils.createDoc();

        Element root = doc.createElement("module");
        doc.appendChild(root);

        root.setAttribute("xmlns", "urn:jboss:module:1.1");
        root.setAttribute("name", moduleName);

        Element resources = doc.createElement("resources");
        root.appendChild(resources);

        Element resource = doc.createElement("resource-root");
        resource.setAttribute("path", fileName);
        resources.appendChild(resource);

        Element dependencies = doc.createElement("dependencies");
        Element module1 = doc.createElement("module");
        module1.setAttribute("name", "javax.api");
        Element module2 = doc.createElement("module");
        module2.setAttribute("name", "javax.transaction.api");
        Element module3 = doc.createElement("module");
        module3.setAttribute("name", "javax.servlet.api");
        module3.setAttribute("optional", "true");

        dependencies.appendChild(module1);
        dependencies.appendChild(module2);
        dependencies.appendChild(module3);

        root.appendChild(dependencies);

        return doc;
    }

}// class
