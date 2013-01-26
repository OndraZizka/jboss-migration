package cz.muni.fi.jboss.migration.migrators.dataSources;

import cz.muni.fi.jboss.migration.CopyMemory;
import cz.muni.fi.jboss.migration.GlobalConfiguration;
import cz.muni.fi.jboss.migration.MigrationContext;
import cz.muni.fi.jboss.migration.MigrationData;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.migrators.security.SecurityDomain;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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

            File dsFiles = new File(globalConfig.getDirAS5() + File.separator + "deploy" );

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
                mData.getConfigFragment().addAll(ds.getLocalDatasourceAS5s());
                mData.getConfigFragment().addAll(ds.getXaDatasourceAS5s());
            }

            ctx.getMigrationData().put(DatasourceMigrator.class, mData);

        } catch (JAXBException e) {
            throw new LoadMigrationException(e);
        } catch (ParserConfigurationException e) {
            throw new LoadMigrationException(e);
        } catch (SAXException e) {
            throw new LoadMigrationException(e);
        } catch (IOException e) {
            throw new LoadMigrationException(e);
        }

    }

    @Override
    public void apply(MigrationContext ctx) {

    }

    @Override
    public List<Node> generateDomElements(MigrationContext ctx) throws MigrationException{
        try {
            JAXBContext dataCtx = JAXBContext.newInstance(DatasourceAS7.class);
            JAXBContext xaDataCtx = JAXBContext.newInstance(XaDatasourceAS7.class);
            JAXBContext driverCtx = JAXBContext.newInstance(Driver.class);
            List<Node> nodeList = new ArrayList();
            Marshaller dataMarshaller = dataCtx.createMarshaller();
            Marshaller xaDataMarshaller = xaDataCtx.createMarshaller();
            Marshaller driverMarshaller = driverCtx.createMarshaller();

            for (IConfigFragment fragment : ctx.getMigrationData().get(DatasourceMigrator.class).getConfigFragment()) {
                if(fragment instanceof DatasourceAS5){
                    Document doc = ctx.getDocBuilder().newDocument();
                    dataMarshaller.marshal(datasourceMigration((DatasourceAS5)fragment), doc);
                    nodeList.add(doc.getDocumentElement());
                }
                if(fragment instanceof XaDatasourceAS5){
                    Document doc = ctx.getDocBuilder().newDocument();
                    xaDataMarshaller.marshal(xaDatasourceMigration((XaDatasourceAS5)fragment), doc);
                    nodeList.add(doc.getDocumentElement());
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

                // TODO: Problem with copy memory class and setting name for Driver so it can be find in server dir
                CopyMemory cp = new CopyMemory();
                cp.setName(StringUtils.substringAfter(xaDsClass, "."));
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
    public List<String> generateCliScripts(MigrationContext ctx) {
        return null;
    }

    // TODO: Security-Domain must reference something what exists in subsystem security...

    private DatasourceAS7 datasourceMigration(DatasourceAS5 datasourceAS5) {
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
}
