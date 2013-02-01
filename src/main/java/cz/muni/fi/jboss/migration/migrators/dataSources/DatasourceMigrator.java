package cz.muni.fi.jboss.migration.migrators.dataSources;

import cz.muni.fi.jboss.migration.CopyMemory;
import cz.muni.fi.jboss.migration.GlobalConfiguration;
import cz.muni.fi.jboss.migration.MigrationContext;
import cz.muni.fi.jboss.migration.MigrationData;
import cz.muni.fi.jboss.migration.ex.ApplyMigrationException;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import javafx.util.Pair;
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

public class DatasourceMigrator implements IMigrator {

    private GlobalConfiguration globalConfig;

    private List<Pair<String,String>> config;

    private Set<String> drivers = new HashSet();

    private Set<String> xaDatasourceClasses = new HashSet();

    public DatasourceMigrator(GlobalConfiguration globalConfig, List<Pair<String,String>> config){
        this.globalConfig = globalConfig;
        this.config =  config;
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException{
        try {
            Unmarshaller dataUnmarshaller = JAXBContext.newInstance(DataSources.class).createUnmarshaller();
            List<DataSources> dsColl = new ArrayList();

            File dsFiles = new File(globalConfig.getDirAS5() + globalConfig.getProfileAS5() + File.separator + "deploy");

            if(dsFiles.canRead()){
                SuffixFileFilter sf = new SuffixFileFilter("-ds.xml");
                List<File> list = (List<File>) FileUtils.listFiles(dsFiles, sf, null);

                if(list.isEmpty()){
                    throw new LoadMigrationException("No \"-ds.xml\" to parse!");
                }

                for(int i = 0; i < list.size() ; i++){
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(list.get(i));

                    Element element = doc.getDocumentElement();

                    if(element.getTagName().equalsIgnoreCase("datasources")){
                        DataSources dataSources = (DataSources)dataUnmarshaller.unmarshal(list.get(i));
                        dsColl.add(dataSources);
                    }
                }
            } else {
                throw new LoadMigrationException("Error: don't have permission for reading files in directory \"AS5_Home"
                        + File.separator+"deploy\"");

            }

            MigrationData mData = new MigrationData();

            for(DataSources ds : dsColl){
                if(ds.getLocalDatasourceAS5s() != null){
                    mData.getConfigFragment().addAll(ds.getLocalDatasourceAS5s());
                }
                if(ds.getXaDatasourceAS5s() != null) {
                    mData.getConfigFragment().addAll(ds.getXaDatasourceAS5s());
                }

            }

            ctx.getMigrationData().put(DatasourceMigrator.class, mData);

        } catch (JAXBException | ParserConfigurationException | SAXException | IOException e) {
            throw new LoadMigrationException(e);
        }

    }

    @Override
    public void apply(MigrationContext ctx) throws ApplyMigrationException{
        try {
            Document doc = ctx.getStandaloneDoc();
            NodeList subsystems = doc.getElementsByTagName("subsystem");
            for(int i = 0; i < subsystems.getLength(); i++){
                if(!(subsystems.item(i) instanceof Element)){
                    continue;
                }
                if(((Element) subsystems.item(i)).getAttribute("xmlns").contains("datasource")){
                    Node parent = subsystems.item(i).getFirstChild();
                    while(!(parent instanceof Element)){
                        parent = parent.getNextSibling();
                    }

                    Node lastNode = parent.getLastChild();

                    while(!(lastNode instanceof Element)){
                        lastNode = lastNode.getPreviousSibling();
                    }

                    for(Node node : generateDomElements(ctx)){
                        ((Element) node).setAttribute("xmlns", "urn:jboss:domain:datasources:1.1");
                        Node adopted = doc.adoptNode(node.cloneNode(true));


                        if(node.getNodeName().equals("driver")){
                            lastNode.appendChild(adopted);
                        } else{
                            parent.insertBefore(adopted, lastNode);
                        }


                    }
                    break;

                }
            }
        } catch (MigrationException e) {
            throw new ApplyMigrationException(e);
        }
    }

    @Override
    public List<Node> generateDomElements(MigrationContext ctx) throws MigrationException{
        try {
            List<Node> nodeList = new ArrayList();
            Marshaller dataMarshaller = JAXBContext.newInstance(DatasourceAS7.class).createMarshaller();
            Marshaller xaDataMarshaller = JAXBContext.newInstance(XaDatasourceAS7.class).createMarshaller();
            Marshaller driverMarshaller = JAXBContext.newInstance(Driver.class).createMarshaller();

            for (IConfigFragment fragment : ctx.getMigrationData().get(DatasourceMigrator.class).getConfigFragment()) {
                if(fragment instanceof DatasourceAS5){
                    Document doc = ctx.getDocBuilder().newDocument();
                    dataMarshaller.marshal(datasourceMigration((DatasourceAS5)fragment), doc);
                    nodeList.add(doc.getDocumentElement()   );
                    continue;
                }
                if(fragment instanceof XaDatasourceAS5){
                    Document doc = ctx.getDocBuilder().newDocument();
                    xaDataMarshaller.marshal(xaDatasourceMigration((XaDatasourceAS5)fragment), doc);
                    nodeList.add(doc.getDocumentElement());
                    continue;
                }
                throw new MigrationException("Error: Object is not part of Datasource migration!");
            }

            for(String driverClass : drivers){
                Driver driver = new Driver();
                driver.setDriverClass(driverClass);
                driver.setDriverName(StringUtils.substringAfter(driverClass, "."));

                CopyMemory cp = new CopyMemory();
                cp.setDriverName(driverClass);
                cp.setType("driver");
                driver.setDriverModule(cp.driverModuleGen());

                ctx.getCopyMemories().add(cp);

                Document doc = ctx.getDocBuilder().newDocument();
                driverMarshaller.marshal(driver, doc);
                nodeList.add(doc.getDocumentElement());
            }

            for(String xaDsClass : xaDatasourceClasses){
                Driver driver = new Driver();
                driver.setXaDatasourceClass(xaDsClass);
                driver.setDriverName(StringUtils.substringAfter(xaDsClass, "."));

                CopyMemory cp = new CopyMemory();
                cp.setDriverName(xaDsClass);
                cp.setType("driver");
                driver.setDriverModule(cp.driverModuleGen());

                ctx.getCopyMemories().add(cp);

                Document doc = ctx.getDocBuilder().newDocument();
                driverMarshaller.marshal(driver, doc);
                nodeList.add(doc.getDocumentElement());
            }

            return nodeList;
        } catch (Exception e) {
            throw new MigrationException(e);
        }
    }

    @Override
    public List<String> generateCliScripts(MigrationContext ctx) throws CliScriptException{
        try {
            List<String> list = new ArrayList();
            Unmarshaller dataUnmarshaller = JAXBContext.newInstance(DatasourceAS7.class).createUnmarshaller();
            Unmarshaller driverUnmarshaller = JAXBContext.newInstance(Driver.class).createUnmarshaller();
            Unmarshaller xaDataUnmarshaller = JAXBContext.newInstance(XaDatasourceAS7.class).createUnmarshaller();
            for(Node node : generateDomElements(ctx)){
                if(node.getNodeName().equals("datasource")){
                    DatasourceAS7 data = (DatasourceAS7) dataUnmarshaller.unmarshal(node);
                    list.add(createDatasourceScript(data, ctx));
                    continue;
                }
                if(node.getNodeName().equals("xa-datasource")){
                    XaDatasourceAS7 xaData = (XaDatasourceAS7) xaDataUnmarshaller.unmarshal(node);
                    list.add(createXaDatasourceScript(xaData,ctx));
                    continue;
                }
                if(node.getNodeName().endsWith("driver")){
                    Driver driver = (Driver) driverUnmarshaller.unmarshal(node);
                    list.add(createDriverScript(driver, ctx));
                    continue;
                }
            }

            return list;
        } catch (MigrationException | JAXBException e) {
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
    public DatasourceAS7 datasourceMigration(DatasourceAS5 datasourceAS5) {
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

        return datasourceAS7;
    }

    /**
     * Method for migrating XaDatasource from AS5 to AS7
     *
     * @param xaDataAS5 object of XaDatasource in AS5
     * @return object representing migrated XaDatasource in AS7
     */
    public XaDatasourceAS7 xaDatasourceMigration(XaDatasourceAS5 xaDataAS5) {
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

        return xaDataAS7;
    }

    /**
     * Creating CLI script for adding Datasource
     *
     * @param datasourceAS7 object of Datasource
     * @param ctx  migration context
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public String createDatasourceScript(DatasourceAS7 datasourceAS7, MigrationContext ctx) throws CliScriptException {
        if((datasourceAS7.getPoolName() == null) || (datasourceAS7.getPoolName().isEmpty())){
            throw new CliScriptException("Error: pool-name of datasource cannot be null or empty",
                    new NullPointerException());
        }

        if((datasourceAS7.getJndiName() == null) || (datasourceAS7.getJndiName().isEmpty())){
            throw new CliScriptException("Error: jndi-name of datasource cannot be null or empty",
                    new NullPointerException());
        }

        if((datasourceAS7.getConnectionUrl() == null) || (datasourceAS7.getConnectionUrl().isEmpty())){
            throw new CliScriptException("Error: connection-url in datasource cannot be null or empty",
                    new NullPointerException());
        }

        if((datasourceAS7.getDriver() == null) || (datasourceAS7.getDriver().isEmpty())){
            throw new CliScriptException("Error: driver-name in datasource cannot be null or empty",
                    new NullPointerException());
        }

        String script = "/subsystem=datasources/data-source=";
        script = script.concat(datasourceAS7.getPoolName()+":add(");
        script = ctx.checkingMethod(script, "jndi-name", datasourceAS7.getJndiName());
        script = ctx.checkingMethod(script, ", enabled", datasourceAS7.getEnabled());
        script = ctx.checkingMethod(script, ", use-java-context", datasourceAS7.getUseJavaContext());
        script = ctx.checkingMethod(script, ", driver-name", datasourceAS7.getDriver());
        script = ctx.checkingMethod(script, ", connection-url", datasourceAS7.getConnectionUrl());
        script = ctx.checkingMethod(script, ", url-delimeter", datasourceAS7.getUrlDelimeter());
        script = ctx.checkingMethod(script, ", url-selector-strategy-class-name", datasourceAS7.getUrlSelector());
        script = ctx.checkingMethod(script, ", transaction-isolation", datasourceAS7.getTransIsolation());
        script = ctx.checkingMethod(script, ", new-connection-sql", datasourceAS7.getNewConnectionSql());
        script = ctx.checkingMethod(script, ", prefill", datasourceAS7.getPrefill());
        script = ctx.checkingMethod(script, ", min-pool-size", datasourceAS7.getMinPoolSize());
        script = ctx.checkingMethod(script, ", max-pool-size", datasourceAS7.getMaxPoolSize());
        script = ctx.checkingMethod(script, ", password", datasourceAS7.getPassword());
        script = ctx.checkingMethod(script, ", user-name", datasourceAS7.getUserName());
        script = ctx.checkingMethod(script, ", security-domain", datasourceAS7.getSecurityDomain());
        script = ctx.checkingMethod(script, ", check-valid-connection-sql", datasourceAS7.getCheckValidConSql());
        script = ctx.checkingMethod(script, ", validate-on-match", datasourceAS7.getValidateOnMatch());
        script = ctx.checkingMethod(script, ", background-validation", datasourceAS7.getBackgroundValid());
        script = ctx.checkingMethod(script, ", background-validation-minutes", datasourceAS7.getBackgroundValidMin());
        script = ctx.checkingMethod(script, ", use-fast-fail", datasourceAS7.getUseFastFail());
        script = ctx.checkingMethod(script, ", exception-sorter-class-name", datasourceAS7.getExceptionSorter());
        script = ctx.checkingMethod(script, ", valid-connection-checker-class-name", datasourceAS7.getValidateOnMatch());
        script = ctx.checkingMethod(script, ", stale-connection-checker-class-name", datasourceAS7.getStaleConChecker());
        script = ctx.checkingMethod(script, ", blocking-timeout-millis", datasourceAS7.getBlockingTimeoutMillis());
        script = ctx.checkingMethod(script, ", idle-timeout-minutes", datasourceAS7.getIdleTimeoutMin());
        script = ctx.checkingMethod(script, ", set-tx-query-timeout", datasourceAS7.getSetTxQueryTimeout());
        script = ctx.checkingMethod(script, ", query-timeout", datasourceAS7.getQueryTimeout());
        script = ctx.checkingMethod(script, ", allocation-retry", datasourceAS7.getAllocationRetry());
        script = ctx.checkingMethod(script, ", allocation-retry-wait-millis", datasourceAS7.getAllocRetryWaitMillis());
        script = ctx.checkingMethod(script, ", use-try-lock", datasourceAS7.getUseTryLock());
        script = ctx.checkingMethod(script, ", prepared-statement-cache-size", datasourceAS7.getPreStatementCacheSize());
        script = ctx.checkingMethod(script, ", track-statements", datasourceAS7.getTrackStatements());
        script = ctx.checkingMethod(script, ", share-prepared-statements", datasourceAS7.getSharePreStatements());
        script = script.concat(")\n");
        script = script.concat("data-source enable --name=" + datasourceAS7.getPoolName());

        return script;
    }

    /**
     * Creating CLI script for adding XaDatsource
     *
     * @param xaDatasourceAS7 object of XaDatasource
     * @param ctx  migration context
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public String createXaDatasourceScript(XaDatasourceAS7 xaDatasourceAS7, MigrationContext ctx) throws  CliScriptException{
        if((xaDatasourceAS7.getPoolName() == null) || (xaDatasourceAS7.getPoolName().isEmpty())){
            throw new CliScriptException("Error: pool-name of xa-datasource cannot be null or empty",
                    new NullPointerException());
        }

        if((xaDatasourceAS7.getJndiName() == null) || (xaDatasourceAS7.getJndiName().isEmpty())){
            throw new CliScriptException("Error: jndi-name of xa-datasource cannot be null or empty",
                    new NullPointerException());
        }

        if((xaDatasourceAS7.getDriver() == null) || (xaDatasourceAS7.getDriver().isEmpty())){
            throw new CliScriptException("Error: driver-name in xa-datasource cannot be null",
                    new NullPointerException());
        }

        String script = "/subsystem=datasources/xa-data-source=";
        script = script.concat(xaDatasourceAS7.getPoolName()+":add(");
        script = ctx.checkingMethod(script, "jndi-name", xaDatasourceAS7.getJndiName());
        script = ctx.checkingMethod(script, ", enabled", xaDatasourceAS7.getEnabled());
        script = ctx.checkingMethod(script, ", use-java-context", xaDatasourceAS7.getUseJavaContext());
        script = ctx.checkingMethod(script, ", driver-name", xaDatasourceAS7.getDriver());
        script = ctx.checkingMethod(script, ", url-delimeter", xaDatasourceAS7.getUrlDelimeter());
        script = ctx.checkingMethod(script, ", url-selector-strategy-class-name", xaDatasourceAS7.getUrlSelector());
        script = ctx.checkingMethod(script, ", transaction-isolation", xaDatasourceAS7.getTransIsolation());
        script = ctx.checkingMethod(script, ", new-connection-sql", xaDatasourceAS7.getNewConnectionSql());
        script = ctx.checkingMethod(script, ", prefill", xaDatasourceAS7.getPrefill());
        script = ctx.checkingMethod(script, ", min-pool-size", xaDatasourceAS7.getMinPoolSize());
        script = ctx.checkingMethod(script, ", max-pool-size", xaDatasourceAS7.getMaxPoolSize());
        script = ctx.checkingMethod(script, ", is-same-rm-override", xaDatasourceAS7.getSameRmOverride());
        script = ctx.checkingMethod(script, ", interleaving", xaDatasourceAS7.getInterleaving());
        script = ctx.checkingMethod(script, ", no-tx-separate-pools", xaDatasourceAS7.getNoTxSeparatePools());
        script = ctx.checkingMethod(script, ", password", xaDatasourceAS7.getPassword());
        script = ctx.checkingMethod(script, ", user-name", xaDatasourceAS7.getUserName());
        script = ctx.checkingMethod(script, ", security-domain", xaDatasourceAS7.getSecurityDomain());
        script = ctx.checkingMethod(script, ", check-valid-connection-sql", xaDatasourceAS7.getCheckValidConSql());
        script = ctx.checkingMethod(script, ", validate-on-match", xaDatasourceAS7.getValidateOnMatch());
        script = ctx.checkingMethod(script, ", background-validation", xaDatasourceAS7.getBackgroundValid());
        script = ctx.checkingMethod(script, ", background-validation-minutes", xaDatasourceAS7.getBackgroundValidMin());
        script = ctx.checkingMethod(script, ", use-fast-fail", xaDatasourceAS7.getUseFastFail());
        script = ctx.checkingMethod(script, ", exception-sorter-class-name", xaDatasourceAS7.getExceptionSorter());
        script = ctx.checkingMethod(script, ", valid-connection-checker-class-name", xaDatasourceAS7.getValidateOnMatch());
        script = ctx.checkingMethod(script, ", stale-connection-checker-class-name", xaDatasourceAS7.getStaleConChecker());
        script = ctx.checkingMethod(script, ", blocking-timeout-millis", xaDatasourceAS7.getBlockingTimeoutMillis());
        script = ctx.checkingMethod(script, ", idle-timeout-minutes", xaDatasourceAS7.getIdleTimeoutMinutes());
        script = ctx.checkingMethod(script, ", set-tx-query-timeout", xaDatasourceAS7.getSetTxQueryTimeout());
        script = ctx.checkingMethod(script, ", query-timeout", xaDatasourceAS7.getQueryTimeout());
        script = ctx.checkingMethod(script, ", allocation-retry", xaDatasourceAS7.getAllocationRetry());
        script = ctx.checkingMethod(script, ", allocation-retry-wait-millis", xaDatasourceAS7.getAllocRetryWaitMillis());
        script = ctx.checkingMethod(script, ", use-try-lock", xaDatasourceAS7.getUseTryLock());
        script = ctx.checkingMethod(script, ", xa-resource-timeout", xaDatasourceAS7.getXaResourceTimeout());
        script = ctx.checkingMethod(script, ", prepared-statement-cache-size", xaDatasourceAS7.getPreStatementCacheSize());
        script = ctx.checkingMethod(script, ", track-statements", xaDatasourceAS7.getTrackStatements());
        script = ctx.checkingMethod(script, ", share-prepared-statements", xaDatasourceAS7.getSharePreStatements());
        script = script.concat(")\n");

        if(xaDatasourceAS7.getXaDatasourceProps() != null){
            for(XaDatasourceProperty xaDatasourceProperty : xaDatasourceAS7.getXaDatasourceProps()){
                script = script.concat("/subsystem=datasources/xa-data-source=" + xaDatasourceAS7.getPoolName());
                script = script.concat("/xa-datasource-properties=" + xaDatasourceProperty.getXaDatasourcePropName());
                script = script.concat(":add(value=" + xaDatasourceProperty.getXaDatasourceProp() + ")\n");

            }
        }

        script = script.concat("xa-data-source enable --name=" + xaDatasourceAS7.getPoolName());
        return script;
    }

    /**
     * Creating CLI script for adding Driver
     *
     * @param driver object of Driver
     * @param ctx  migration context
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public String createDriverScript(Driver driver, MigrationContext ctx) throws CliScriptException {
        if((driver.getDriverModule() == null) || (driver.getDriverModule().isEmpty())){
            throw new CliScriptException("Error: Driver module in driver cannot be null or empty",
                    new NullPointerException());
        }

        if((driver.getDriverName() == null) || (driver.getDriverName().isEmpty())){
            throw new CliScriptException("Error: Driver name cannot be null or empty",
                    new NullPointerException());
        }

        String script = "/subsystem=datasources/jdbc-driver=";
        script = script.concat(driver.getDriverName() + ":add(");
        script = script.concat("driver-module-name=" + driver.getDriverModule());
        script = ctx.checkingMethod(script, ", driver-class-name", driver.getDriverClass());
        script = ctx.checkingMethod(script, ", driver-xa-datasource-class-name", driver.getXaDatasourceClass());
        script = ctx.checkingMethod(script, ", driver-major-version", driver.getMajorVersion());
        script = ctx.checkingMethod(script, ", driver-minor-version", driver.getMinorVersion());
        script = script.concat(")");

        return script;
    }



    // TODO: It seems that CLI script for adding Datasource and Xa-Datasource was changed.


    /**
     * Creating CLI script for adding Datasource
     *
     * @param datasourceAS7 object of Datasource
     * @param ctx  migration context
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public String createDatasourceScript2(DatasourceAS7 datasourceAS7, MigrationContext ctx) throws CliScriptException {
        if((datasourceAS7.getPoolName() == null) || (datasourceAS7.getPoolName().isEmpty())){
            throw new CliScriptException("Error: pool-name of datasource cannot be null or empty",
                    new NullPointerException());
        }

        if((datasourceAS7.getJndiName() == null) || (datasourceAS7.getJndiName().isEmpty())){
            throw new CliScriptException("Error: jndi-name of datasource cannot be null or empty",
                    new NullPointerException());
        }

        if((datasourceAS7.getConnectionUrl() == null) || (datasourceAS7.getConnectionUrl().isEmpty())){
            throw new CliScriptException("Error: connection-url in datasource cannot be null or empty",
                    new NullPointerException());
        }

        if((datasourceAS7.getDriver() == null) || (datasourceAS7.getDriver().isEmpty())){
            throw new CliScriptException("Error: driver-name in datasource cannot be null or empty",
                    new NullPointerException());
        }

        String script = "data-source add";
        script = script.concat(datasourceAS7.getPoolName()+":add(");
        script = ctx.checkingMethod(script, " --jndi-name", datasourceAS7.getJndiName());
        script = ctx.checkingMethod(script, " --enabled", datasourceAS7.getEnabled());
        script = ctx.checkingMethod(script, " --use-java-context", datasourceAS7.getUseJavaContext());
        script = ctx.checkingMethod(script, " --driver-name", datasourceAS7.getDriver());
        script = ctx.checkingMethod(script, " --connection-url", datasourceAS7.getConnectionUrl());
        script = ctx.checkingMethod(script, " --url-delimeter", datasourceAS7.getUrlDelimeter());
        script = ctx.checkingMethod(script, " --url-selector-strategy-class-name", datasourceAS7.getUrlSelector());
        script = ctx.checkingMethod(script, " --transaction-isolation", datasourceAS7.getTransIsolation());
        script = ctx.checkingMethod(script, " --new-connection-sql", datasourceAS7.getNewConnectionSql());
        script = ctx.checkingMethod(script, " --prefill", datasourceAS7.getPrefill());
        script = ctx.checkingMethod(script, " --min-pool-size", datasourceAS7.getMinPoolSize());
        script = ctx.checkingMethod(script, " --max-pool-size", datasourceAS7.getMaxPoolSize());
        script = ctx.checkingMethod(script, " --password", datasourceAS7.getPassword());
        script = ctx.checkingMethod(script, " --user-name", datasourceAS7.getUserName());
        script = ctx.checkingMethod(script, " --security-domain", datasourceAS7.getSecurityDomain());
        script = ctx.checkingMethod(script, " --check-valid-connection-sql", datasourceAS7.getCheckValidConSql());
        script = ctx.checkingMethod(script, " --validate-on-match", datasourceAS7.getValidateOnMatch());
        script = ctx.checkingMethod(script, " --background-validation", datasourceAS7.getBackgroundValid());
        script = ctx.checkingMethod(script, " --background-validation-minutes", datasourceAS7.getBackgroundValidMin());
        script = ctx.checkingMethod(script, " --use-fast-fail", datasourceAS7.getUseFastFail());
        script = ctx.checkingMethod(script, " --exception-sorter-class-name", datasourceAS7.getExceptionSorter());
        script = ctx.checkingMethod(script, " --valid-connection-checker-class-name", datasourceAS7.getValidateOnMatch());
        script = ctx.checkingMethod(script, " --stale-connection-checker-class-name", datasourceAS7.getStaleConChecker());
        script = ctx.checkingMethod(script, " --blocking-timeout-millis", datasourceAS7.getBlockingTimeoutMillis());
        script = ctx.checkingMethod(script, " --idle-timeout-minutes", datasourceAS7.getIdleTimeoutMin());
        script = ctx.checkingMethod(script, " --set-tx-query-timeout", datasourceAS7.getSetTxQueryTimeout());
        script = ctx.checkingMethod(script, " --query-timeout", datasourceAS7.getQueryTimeout());
        script = ctx.checkingMethod(script, " --allocation-retry", datasourceAS7.getAllocationRetry());
        script = ctx.checkingMethod(script, " --allocation-retry-wait-millis", datasourceAS7.getAllocRetryWaitMillis());
        script = ctx.checkingMethod(script, " --use-try-lock", datasourceAS7.getUseTryLock());
        script = ctx.checkingMethod(script, " --prepared-statement-cache-size", datasourceAS7.getPreStatementCacheSize());
        script = ctx.checkingMethod(script, " --track-statements", datasourceAS7.getTrackStatements());
        script = ctx.checkingMethod(script, " --share-prepared-statements", datasourceAS7.getSharePreStatements());
        script = script.concat("\n");
        script = script.concat("data-source enable --name=" + datasourceAS7.getPoolName());

        return script;
    }


    /**
     * Creating CLI script for adding XaDatsource
     *
     * @param xaDatasourceAS7 object of XaDatasource
     * @param ctx  migration context
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public String createXaDatasourceScript2(XaDatasourceAS7 xaDatasourceAS7, MigrationContext ctx) throws  CliScriptException{
        if((xaDatasourceAS7.getPoolName() == null) || (xaDatasourceAS7.getPoolName().isEmpty())){
            throw new CliScriptException("Error: pool-name of xa-datasource cannot be null or empty",
                    new NullPointerException());
        }

        if((xaDatasourceAS7.getJndiName() == null) || (xaDatasourceAS7.getJndiName().isEmpty())){
            throw new CliScriptException("Error: jndi-name of xa-datasource cannot be null or empty",
                    new NullPointerException());
        }

        if((xaDatasourceAS7.getDriver() == null) || (xaDatasourceAS7.getDriver().isEmpty())){
            throw new CliScriptException("Error: driver-name in xa-datasource cannot be null",
                    new NullPointerException());
        }

        String script = "xa-data-source add";
        script = script.concat(xaDatasourceAS7.getPoolName()+":add(");
        script = ctx.checkingMethod(script, " --jndi-name", xaDatasourceAS7.getJndiName());
        script = ctx.checkingMethod(script, " --enabled", xaDatasourceAS7.getEnabled());
        script = ctx.checkingMethod(script, " --use-java-context", xaDatasourceAS7.getUseJavaContext());
        script = ctx.checkingMethod(script, " --driver-name", xaDatasourceAS7.getDriver());
        script = ctx.checkingMethod(script, " --url-delimeter", xaDatasourceAS7.getUrlDelimeter());
        script = ctx.checkingMethod(script, " --url-selector-strategy-class-name", xaDatasourceAS7.getUrlSelector());
        script = ctx.checkingMethod(script, " --transaction-isolation", xaDatasourceAS7.getTransIsolation());
        script = ctx.checkingMethod(script, " --new-connection-sql", xaDatasourceAS7.getNewConnectionSql());
        script = ctx.checkingMethod(script, " --prefill", xaDatasourceAS7.getPrefill());
        script = ctx.checkingMethod(script, " --min-pool-size", xaDatasourceAS7.getMinPoolSize());
        script = ctx.checkingMethod(script, " --max-pool-size", xaDatasourceAS7.getMaxPoolSize());
        script = ctx.checkingMethod(script, " --is-same-rm-override", xaDatasourceAS7.getSameRmOverride());
        script = ctx.checkingMethod(script, " --interleaving", xaDatasourceAS7.getInterleaving());
        script = ctx.checkingMethod(script, " --no-tx-separate-pools", xaDatasourceAS7.getNoTxSeparatePools());
        script = ctx.checkingMethod(script, " --password", xaDatasourceAS7.getPassword());
        script = ctx.checkingMethod(script, " --user-name", xaDatasourceAS7.getUserName());
        script = ctx.checkingMethod(script, " --security-domain", xaDatasourceAS7.getSecurityDomain());
        script = ctx.checkingMethod(script, " --check-valid-connection-sql", xaDatasourceAS7.getCheckValidConSql());
        script = ctx.checkingMethod(script, " --validate-on-match", xaDatasourceAS7.getValidateOnMatch());
        script = ctx.checkingMethod(script, " --background-validation", xaDatasourceAS7.getBackgroundValid());
        script = ctx.checkingMethod(script, " --background-validation-minutes", xaDatasourceAS7.getBackgroundValidMin());
        script = ctx.checkingMethod(script, " --use-fast-fail", xaDatasourceAS7.getUseFastFail());
        script = ctx.checkingMethod(script, " --exception-sorter-class-name", xaDatasourceAS7.getExceptionSorter());
        script = ctx.checkingMethod(script, " --valid-connection-checker-class-name", xaDatasourceAS7.getValidateOnMatch());
        script = ctx.checkingMethod(script, " --stale-connection-checker-class-name", xaDatasourceAS7.getStaleConChecker());
        script = ctx.checkingMethod(script, " --blocking-timeout-millis", xaDatasourceAS7.getBlockingTimeoutMillis());
        script = ctx.checkingMethod(script, " --idle-timeout-minutes", xaDatasourceAS7.getIdleTimeoutMinutes());
        script = ctx.checkingMethod(script, " --set-tx-query-timeout", xaDatasourceAS7.getSetTxQueryTimeout());
        script = ctx.checkingMethod(script, " --query-timeout", xaDatasourceAS7.getQueryTimeout());
        script = ctx.checkingMethod(script, " --allocation-retry", xaDatasourceAS7.getAllocationRetry());
        script = ctx.checkingMethod(script, " --allocation-retry-wait-millis", xaDatasourceAS7.getAllocRetryWaitMillis());
        script = ctx.checkingMethod(script, " --use-try-lock", xaDatasourceAS7.getUseTryLock());
        script = ctx.checkingMethod(script, " --xa-resource-timeout", xaDatasourceAS7.getXaResourceTimeout());
        script = ctx.checkingMethod(script, " --prepared-statement-cache-size", xaDatasourceAS7.getPreStatementCacheSize());
        script = ctx.checkingMethod(script, " --track-statements", xaDatasourceAS7.getTrackStatements());
        script = ctx.checkingMethod(script, " --share-prepared-statements", xaDatasourceAS7.getSharePreStatements());
        script = script.concat("\n");

        if(xaDatasourceAS7.getXaDatasourceProps() != null){
            for(XaDatasourceProperty xaDatasourceProperty : xaDatasourceAS7.getXaDatasourceProps()){
                script = script.concat("/subsystem=datasources/xa-data-source=" + xaDatasourceAS7.getPoolName());
                script = script.concat("/xa-datasource-properties=" + xaDatasourceProperty.getXaDatasourcePropName());
                script = script.concat(":add(value=" + xaDatasourceProperty.getXaDatasourceProp() + ")\n");

            }
        }

        script = script.concat("xa-data-source enable --name=" + xaDatasourceAS7.getPoolName());
        return script;
    }
}
