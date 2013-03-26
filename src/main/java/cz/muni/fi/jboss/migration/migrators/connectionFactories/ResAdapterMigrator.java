package cz.muni.fi.jboss.migration.migrators.connectionFactories;

import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.ex.ApplyMigrationException;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.NodeGenerationException;
import cz.muni.fi.jboss.migration.migrators.connectionFactories.jaxb.*;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Migrator of Resource-Adapter(Connection-Factories in AS5) subsystem implementing IMigrator
 *
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:41 AM
 */

public class ResAdapterMigrator extends AbstractMigrator {

    private Set<String> rars = new HashSet<>();

    @Override protected String getConfigPropertyModuleName() { return "resourceAdapter"; }
    
    

    public ResAdapterMigrator(GlobalConfiguration globalConfig, MultiValueMap config) {
        super(globalConfig, config);
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException {
        try {
            Unmarshaller dataUnmarshaller = JAXBContext.newInstance(ConnectionFactoriesBean.class).createUnmarshaller();
            List<ConnectionFactoriesBean> connFactories = new ArrayList();

            // Deployments AS 5 dir.
            File dsFiles = getGlobalConfig().getAS5DeployDir();

            if( ! dsFiles.canRead() ) {
                throw new LoadMigrationException("Can't read: " + dsFiles.getPath() );
            }
            
            // -ds.xml files.
            //SuffixFileFilter sf = new SuffixFileFilter("-ds.xml");
            //List<File> dsXmls = (List<File>) FileUtils.listFiles(dsFiles, sf, FileFilterUtils.makeCVSAware(null));
            Collection<File> dsXmls = FileUtils.listFiles(dsFiles, new String[]{"-ds.xml"}, true);
            
            if( dsXmls.isEmpty() ) {
                return;
            }
            
            // For each -ds.xml
            for (File dsXml : dsXmls) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(dsXml);

                Element element = doc.getDocumentElement();

                if (element.getTagName().equalsIgnoreCase("connection-factories")) {
                    ConnectionFactoriesBean conn = (ConnectionFactoriesBean) dataUnmarshaller.unmarshal(dsXml);
                    connFactories.add(conn);
                }
            }

            MigrationData migrData = new MigrationData();

            for (ConnectionFactoriesBean cf : connFactories) {
                if(cf.getConnectionFactories() != null){
                    migrData.getConfigFragment().addAll(cf.getConnectionFactories());
                }
                if(cf.getNoTxConnectionFactories() != null){
                    migrData.getConfigFragment().addAll(cf.getNoTxConnectionFactories());
                }

            }

            ctx.getMigrationData().put(ResAdapterMigrator.class, migrData);

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
                if( ! (subsystems.item(i) instanceof Element)) {
                    continue;
                }
                if (((Element) subsystems.item(i)).getAttribute("xmlns").contains("resource-adapters")) {
                    Node parent = doc.createElement("resource-adapters");

                    for (Node node : generateDomElements(ctx)) {
                        Node adopted = doc.adoptNode(node.cloneNode(true));
                        parent.appendChild(adopted);
                    }
                    subsystems.item(i).appendChild(parent);
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
            JAXBContext resAdapCtx = JAXBContext.newInstance(ResourceAdapterBean.class);
            List<Node> nodeList = new ArrayList();
            Marshaller resAdapMarshaller = resAdapCtx.createMarshaller();

            for (IConfigFragment fragment : ctx.getMigrationData().get(ResAdapterMigrator.class).getConfigFragment()) {
                Document doc = ctx.getDocBuilder().newDocument();
                if (fragment instanceof ConnectionFactoryAS5Bean) {
                    resAdapMarshaller.marshal(txConnFactoryMigration((ConnectionFactoryAS5Bean) fragment), doc);
                    nodeList.add(doc.getDocumentElement());
                    continue;
                }
                if(fragment instanceof NoTxConnectionFactoryAS5Bean) {
                    resAdapMarshaller.marshal(noTxConnFactoryMigration((NoTxConnectionFactoryAS5Bean) fragment), doc);
                    nodeList.add(doc.getDocumentElement());
                    continue;
                }
                throw new NodeGenerationException("Object is not part of resource-adapter" +
                        "(connection-factories) migration!");
            }

            for(String rar : this.rars){
                RollbackData rollbackData = new RollbackData();
                rollbackData.setName(rar);
                rollbackData.setType(RollbackData.Type.RESOURCE);
                ctx.getRollbackData().add(rollbackData);
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
            Unmarshaller resUnmarshaller = JAXBContext.newInstance(ResourceAdapterBean.class).createUnmarshaller();

            for (Node node : generateDomElements(ctx)) {
                ResourceAdapterBean resAdapter = (ResourceAdapterBean) resUnmarshaller.unmarshal(node);
                list.add(createResAdapterScript(resAdapter));
            }

            return list;
        } catch (NodeGenerationException | JAXBException e) {
            throw new CliScriptException(e);
        }
    }

    /**
     * Method for migrating tx-connection-factory from AS5 to AS7
     *
     * @param connFactoryAS5 object representing tx-connection-factory
     * @return created resource-adapter
     */
    public ResourceAdapterBean txConnFactoryMigration(ConnectionFactoryAS5Bean connFactoryAS5){

        ResourceAdapterBean resAdapter = new ResourceAdapterBean();
        resAdapter.setJndiName(connFactoryAS5.getJndiName());
        this.rars.add(connFactoryAS5.getRarName());

        resAdapter.setArchive(connFactoryAS5.getRarName());

        if(connFactoryAS5.getXaTransaction() != null){
            resAdapter.setTransactionSupport("XATransaction");
        } else {
            resAdapter.setTransactionSupport("LocalTransaction");
        }

        ConnectionDefinitionBean connDef = new ConnectionDefinitionBean();
        connDef.setJndiName("java:jboss/" + connFactoryAS5.getJndiName());
        connDef.setPoolName(connFactoryAS5.getJndiName());
        connDef.setEnabled("true");
        connDef.setUseJavaCont("true");
        connDef.setEnabled("true");
        connDef.setClassName(connFactoryAS5.getConnectionDefinition());
        connDef.setPrefill(connFactoryAS5.getPrefill());

        for (ConfigPropertyBean configProperty : connFactoryAS5.getConfigProperties()) {
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

        Set<ConnectionDefinitionBean> connDefColl = new HashSet();
        connDefColl.add(connDef);
        resAdapter.setConnectionDefinitions(connDefColl);

        return resAdapter;
    }

    /**
     *  Method for migrating no-tx-connection-factory from AS5 to AS7
     *
     * @param connFactoryAS5  object representing no-tx-connection-factory
     * @return created resource-adapter
     */
    public ResourceAdapterBean noTxConnFactoryMigration(NoTxConnectionFactoryAS5Bean connFactoryAS5){

        ResourceAdapterBean resAdapter = new ResourceAdapterBean();
        resAdapter.setJndiName(connFactoryAS5.getJndiName());
        this.rars.add(connFactoryAS5.getRarName());

        resAdapter.setArchive(connFactoryAS5.getRarName());

        resAdapter.setTransactionSupport("NoTransaction");

        ConnectionDefinitionBean connDef = new ConnectionDefinitionBean();
        connDef.setJndiName("java:jboss/" + connFactoryAS5.getJndiName());
        connDef.setPoolName(connFactoryAS5.getJndiName());
        connDef.setEnabled("true");
        connDef.setUseJavaCont("true");
        connDef.setEnabled("true");
        connDef.setClassName(connFactoryAS5.getConnectionDefinition());
        connDef.setPrefill(connFactoryAS5.getPrefill());

        for (ConfigPropertyBean configProperty : connFactoryAS5.getConfigProperties()) {
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

        Set<ConnectionDefinitionBean> connDefColl = new HashSet();
        connDefColl.add(connDef);
        resAdapter.setConnectionDefinitions(connDefColl);

        return resAdapter;
    }


    /**
     * Creating CLI script for adding Resource-Adapter
     *
     * @param resourceAdapter object of Resource-Adapter
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createResAdapterScript(ResourceAdapterBean resourceAdapter)
            throws CliScriptException {
        String errMsg = " in resource-adapter(connection-factories in AS5) must be set.";
        Utils.throwIfBlank(resourceAdapter.getArchive(), errMsg, "Archive name");


        StringBuilder resultBuilder = new StringBuilder();
        CliAddCommandBuilder cliBuilder = new CliAddCommandBuilder();

        String adapterScript = "/subsystem=resource-adapters/resource-adapter=" + resourceAdapter.getArchive() + ":add(";
        cliBuilder.addProperty("archive", resourceAdapter.getArchive());
        cliBuilder.addProperty("transaction-support", resourceAdapter.getTransactionSupport());

        adapterScript = adapterScript.concat(cliBuilder.asString() + ")\n");
        resultBuilder.append(adapterScript);


        if (resourceAdapter.getConnectionDefinitions() != null) {
            for (ConnectionDefinitionBean connDef : resourceAdapter.getConnectionDefinitions()) {
                errMsg = "in connection-definition in resource-adapter(connection-factories) must be set";
                Utils.throwIfBlank(connDef.getClassName(), errMsg, "Class-name");
                Utils.throwIfBlank(connDef.getPoolName(), errMsg, "Pool-name");

                String connDefScript = "/subsystem=resource-adapters/resource-adapter=" + resourceAdapter.getArchive();
                connDefScript = connDefScript.concat("/connection-definitions=" + connDef.getPoolName() + ":add(");
                cliBuilder.addProperty("jndi-name", connDef.getJndiName());
                cliBuilder.addProperty("enabled", connDef.getEnabled());
                cliBuilder.addProperty("use-java-context", connDef.getUseJavaCont());
                cliBuilder.addProperty("class-name", connDef.getClassName());
                cliBuilder.addProperty("use-ccm", connDef.getUseCcm());
                cliBuilder.addProperty("prefill", connDef.getPrefill());
                cliBuilder.addProperty("use-strict-min", connDef.getUseStrictMin());
                cliBuilder.addProperty("flush-strategy", connDef.getFlushStrategy());
                cliBuilder.addProperty("min-pool-size", connDef.getMinPoolSize());
                cliBuilder.addProperty("max-pool-size", connDef.getMaxPoolSize());

                if (connDef.getSecurityDomain() != null) {
                    cliBuilder.addProperty("security-domain", connDef.getSecurityDomain());
                }

                if (connDef.getSecDomainAndApp() != null) {
                    cliBuilder.addProperty("security-domain-and-application", connDef.getSecDomainAndApp());
                }

                if (connDef.getAppManagedSec() != null) {
                    cliBuilder.addProperty("application-managed-security", connDef.getAppManagedSec());
                }

                cliBuilder.addProperty("background-validation", connDef.getBackgroundValidation());
                cliBuilder.addProperty("background-validation-millis", connDef.getBackgroundValiMillis());
                cliBuilder.addProperty("blocking-timeout-millis", connDef.getBlockingTimeoutMillis());
                cliBuilder.addProperty("idle-timeout-minutes", connDef.getIdleTimeoutMinutes());
                cliBuilder.addProperty("allocation-retry", connDef.getAllocationRetry());
                cliBuilder.addProperty("allocation-retry-wait-millis", connDef.getAllocRetryWaitMillis());
                cliBuilder.addProperty("xa-resource-timeout", connDef.getXaResourceTimeout());

                connDefScript = connDefScript.concat(cliBuilder.asString() + ")\n");
                resultBuilder.append(connDefScript);

                if (connDef.getConfigProperties() != null) {
                    for (ConfigPropertyBean configProperty : connDef.getConfigProperties()) {
                        errMsg = "of config-property in connection-definition in resource-adapter must be set.";
                        Utils.throwIfBlank(configProperty.getConfigPropertyName(), errMsg, "Name");

                        resultBuilder.append("/subsystem=resource-adapters/resource-adapter=");
                        resultBuilder.append(resourceAdapter.getArchive());

                        resultBuilder.append("/connection-definitions=").append(connDef.getPoolName());

                        resultBuilder.append("/config-properties=");
                        resultBuilder.append(configProperty.getConfigPropertyName());

                        resultBuilder.append(":add(");
                        resultBuilder.append("value=").append(configProperty.getConfigProperty()).append(")\n");
                    }
                }
            }
        }

        return resultBuilder.toString();
    }
}
