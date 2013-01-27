package cz.muni.fi.jboss.migration.migrators.connectionFactories;

import cz.muni.fi.jboss.migration.CopyMemory;
import cz.muni.fi.jboss.migration.GlobalConfiguration;
import cz.muni.fi.jboss.migration.MigrationContext;
import cz.muni.fi.jboss.migration.MigrationData;
import cz.muni.fi.jboss.migration.ex.ApplyMigrationException;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.migrators.dataSources.DatasourceAS7;
import cz.muni.fi.jboss.migration.migrators.dataSources.Driver;
import cz.muni.fi.jboss.migration.migrators.dataSources.XaDatasourceAS7;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
public class ResAdapterMigrator implements IMigrator {

    private GlobalConfiguration globalConfig;

    private List<Pair<String,String>> config;

    public ResAdapterMigrator(GlobalConfiguration globalConfig, List<Pair<String,String>> config){
        this.globalConfig = globalConfig;
        this.config =  config;
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws  LoadMigrationException{
        try {
            Unmarshaller dataUnmarshaller = JAXBContext.newInstance(ConnectionFactories.class).createUnmarshaller();
            List<ConnectionFactories> connFactories = new ArrayList();

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

                    if(element.getTagName().equalsIgnoreCase("connection-factories")){
                        ConnectionFactories conn = (ConnectionFactories)dataUnmarshaller.unmarshal(list.get(i));
                        connFactories.add(conn);
                    }
                }
            } else {
                throw new LoadMigrationException("Error: don't have permission for reading files in directory \"AS5_Home"
                        + File.separator+"deploy\"");
            }

            MigrationData mData = new MigrationData();

            for(ConnectionFactories cf : connFactories){
                mData.getConfigFragment().addAll(cf.getConnectionFactories());
            }

            ctx.getMigrationData().put(ResAdapterMigrator.class, mData);

        } catch (JAXBException | ParserConfigurationException | SAXException | IOException e) {
            throw new LoadMigrationException(e);
        }
    }

    @Override
    public void apply(MigrationContext ctx) throws ApplyMigrationException{
        try {
            File standalone = new File(globalConfig.getStandaloneFilePath());
            Document doc = ctx.getDocBuilder().parse(standalone);
            NodeList subsystems = doc.getElementsByTagName("subsystem");
            for(int i = 0; i < subsystems.getLength(); i++){
                if(!(subsystems.item(i) instanceof Element)){
                    continue;
                }
                if(((Element) subsystems.item(i)).getAttribute("xmlns").contains("resource-adapters")){
                    Node parent = doc.createElement("resource-adapters");

                    for(Node node : generateDomElements(ctx)){
                        Node adopted = doc.adoptNode(node.cloneNode(true));
                        parent.appendChild(adopted);
                    }
                    subsystems.item(i).appendChild(parent);
                    break;
                }
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult result = new StreamResult(standalone);
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);

        } catch (SAXException | IOException | MigrationException | TransformerException e) {
            throw new ApplyMigrationException(e);
        }
    }

    @Override
    public List<Node> generateDomElements(MigrationContext ctx) throws MigrationException{
        try {
            JAXBContext resAdapCtx = JAXBContext.newInstance(ResourceAdapter.class);
            List<Node> nodeList = new ArrayList();
            Marshaller resAdapMarshaller = resAdapCtx.createMarshaller();

            for(IConfigFragment fragment : ctx.getMigrationData().get(ResAdapterMigrator.class).getConfigFragment()){
                if(!(fragment instanceof ConnectionFactoryAS5)){
                    throw new MigrationException("Error: Object is not part of resource-adapter(connection-factories) migration!");
                }
                ConnectionFactoryAS5 connFactoryAS5 = (ConnectionFactoryAS5) fragment;
                ResourceAdapter resAdapter = new ResourceAdapter();
                resAdapter.setJndiName(connFactoryAS5.getJndiName());

                CopyMemory copyMemory = new CopyMemory();
                copyMemory.setName(connFactoryAS5.getRarName());
                copyMemory.setType("resource");
                ctx.getCopyMemories().add(copyMemory);

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

                Document doc = ctx.getDocBuilder().newDocument();
                resAdapMarshaller.marshal(resAdapter, doc);
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
            Unmarshaller resUnmarshaller = JAXBContext.newInstance(ResourceAdapter.class).createUnmarshaller();

            for(Node node : generateDomElements(ctx)){
                ResourceAdapter resAdapter = (ResourceAdapter) resUnmarshaller.unmarshal(node);
                list.add(createResAdapterScript(resAdapter, ctx));
            }

            return list;
        } catch (MigrationException | JAXBException e) {
            throw new CliScriptException(e);
        }
    }

    private String createResAdapterScript(ResourceAdapter resourceAdapter, MigrationContext ctx) throws CliScriptException{
//        if((resourceAdapter.getJndiName() == null) || (resourceAdapter.getJndiName().isEmpty())){
//            throw new CliScriptException("Error: name of the resource-adapter cannot be null or empty",
//                    new NullPointerException());
//        }

        if((resourceAdapter.getArchive() == null) || (resourceAdapter.getArchive().isEmpty())){
            throw new CliScriptException("Error: archive in the resource-adapter cannot be null or empty",
                    new NullPointerException());
        }

        String script = "/subsystem=resource-adapters/resource-adapter=";
        // TODO: Problem with JndiName setting(not in Node so it is always null). Provisional implementation with archive name
        script = script.concat(resourceAdapter.getArchive() + ":add(");
        script = script.concat("archive=" + resourceAdapter.getArchive());
        script = ctx.checkingMethod(script, ", transaction-support", resourceAdapter.getTransactionSupport());
        script = script.concat(")\n");

        if(resourceAdapter.getConnectionDefinitions() != null){
            for(ConnectionDefinition connectionDefinition : resourceAdapter.getConnectionDefinitions()){
                if((connectionDefinition.getClassName() == null) || (connectionDefinition.getClassName().isEmpty())){
                    throw new CliScriptException("Error: class-name in the connection definition cannot be null or empty",
                            new NullPointerException());
                }

                script =  script.concat("/subsystem=resource-adapters/resource-adapter=" + resourceAdapter.getJndiName());
                script = script.concat("/connection-definitions=" + connectionDefinition.getPoolName() + ":add(");
                script = ctx.checkingMethod(script, " jndi-name", connectionDefinition.getJndiName());
                script = ctx.checkingMethod(script, ", enabled", connectionDefinition.getEnabled());
                script = ctx.checkingMethod(script, ", use-java-context", connectionDefinition.getUseJavaCont());
                script = ctx.checkingMethod(script, ", class-name", connectionDefinition.getClassName());
                script = ctx.checkingMethod(script, ", use-ccm", connectionDefinition.getUseCcm());
                script = ctx.checkingMethod(script, ", prefill", connectionDefinition.getPrefill());
                script = ctx.checkingMethod(script, ", use-strict-min", connectionDefinition.getUseStrictMin());
                script = ctx.checkingMethod(script, ", flush-strategy", connectionDefinition.getFlushStrategy());
                script = ctx.checkingMethod(script, ", min-pool-size", connectionDefinition.getMinPoolSize());
                script = ctx.checkingMethod(script, ", max-pool-size", connectionDefinition.getMaxPoolSize());

                if(connectionDefinition.getSecurityDomain() != null){
                    script = ctx.checkingMethod(script, ", security-domain", connectionDefinition.getSecurityDomain());
                }

                if(connectionDefinition.getSecDomainAndApp() != null){
                    script = ctx.checkingMethod(script, ", security-domain-and-application",
                            connectionDefinition.getSecDomainAndApp());
                }

                if(connectionDefinition.getAppManagedSec() != null){
                    script = ctx.checkingMethod(script, ", application-managed-security",
                            connectionDefinition.getAppManagedSec());
                }

                script = ctx.checkingMethod(script, ", background-validation", connectionDefinition.getBackgroundValidation());
                script = ctx.checkingMethod(script, ", background-validation-millis", connectionDefinition.getBackgroundValiMillis());
                script = ctx.checkingMethod(script, ", blocking-timeout-millis", connectionDefinition.getBackgroundValiMillis());
                script = ctx.checkingMethod(script, ", idle-timeout-minutes", connectionDefinition.getIdleTimeoutMinutes());
                script = ctx.checkingMethod(script, ", allocation-retry", connectionDefinition.getAllocationRetry());
                script = ctx.checkingMethod(script, ", allocation-retry-wait-millis", connectionDefinition.getAllocRetryWaitMillis());
                script = ctx.checkingMethod(script, ", xa-resource-timeout", connectionDefinition.getXaResourceTimeout());
                script = script.concat(")\n");

                if(connectionDefinition.getConfigProperties() != null){
                    for(ConfigProperty configProperty : connectionDefinition.getConfigProperties()){
                        script = script.concat("/subsystem=resource-adapters/resource-adapter=" + resourceAdapter.getJndiName());
                        script = script.concat("/connection-definitions=" + connectionDefinition.getPoolName());
                        script = script.concat("/config-properties=" + configProperty.getConfigPropertyName() + ":add(");
                        script = script.concat("value=" + configProperty.getConfigProperty() + ")\n");
                    }
                }
            }
        }
        return script;
    }


}
