package cz.muni.fi.jboss.migration.migrators.connectionFactories;

import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.ex.*;

import cz.muni.fi.jboss.migration.migrators.connectionFactories.jaxb.*;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import cz.muni.fi.jboss.migration.utils.Utils;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
            Unmarshaller dataUnmarshaller = JAXBContext.newInstance(ConnectionFactoriesBean.class).createUnmarshaller();
            List<ConnectionFactoriesBean> connFactories = new ArrayList();

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

                    if(element.getTagName().equalsIgnoreCase("connection-factories")){
                        ConnectionFactoriesBean conn = (ConnectionFactoriesBean)dataUnmarshaller.unmarshal(list.get(i));
                        connFactories.add(conn);
                    }
                }
            } else {
                throw new LoadMigrationException("Don't have permission for reading files in directory \"AS5_Home"
                        + File.separator+"deploy\"");
            }

            MigrationData mData = new MigrationData();

            for(ConnectionFactoriesBean cf : connFactories){
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
            Document doc = ctx.getStandaloneDoc();
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

            for(IConfigFragment fragment : ctx.getMigrationData().get(ResAdapterMigrator.class).getConfigFragment()){
                if(!(fragment instanceof ConnectionFactoryAS5Bean)){
                    throw new NodeGenerationException("Object is not part of resource-adapter" +
                            "(connection-factories) migration!");
                }
                ConnectionFactoryAS5Bean connFactoryAS5 = (ConnectionFactoryAS5Bean) fragment;
                ResourceAdapterBean resAdapter = new ResourceAdapterBean();
                resAdapter.setJndiName(connFactoryAS5.getJndiName());

                RollbackData rollbackData = new RollbackData();
                rollbackData.setName(connFactoryAS5.getRarName());
                rollbackData.setType("resource");
                ctx.getRollbackDatas().add(rollbackData);

                resAdapter.setArchive(connFactoryAS5.getRarName());

                // TODO: Not sure what exactly this element represents and what it is in AS5
                resAdapter.setTransactionSupport("XATransaction");

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

                Document doc = ctx.getDocBuilder().newDocument();
                resAdapMarshaller.marshal(resAdapter, doc);
                nodeList.add(doc.getDocumentElement());
            }

            return nodeList;

        } catch (JAXBException e) {
            throw new NodeGenerationException(e);
        }
    }

    @Override
    public List<String> generateCliScripts(MigrationContext ctx) throws CliScriptException{
        try {
            List<String> list = new ArrayList();
            Unmarshaller resUnmarshaller = JAXBContext.newInstance(ResourceAdapterBean.class).createUnmarshaller();

            for(Node node : generateDomElements(ctx)){
                ResourceAdapterBean resAdapter = (ResourceAdapterBean) resUnmarshaller.unmarshal(node);
                list.add(createResAdapterScript(resAdapter, ctx));
            }

            return list;
        } catch (NodeGenerationException | JAXBException e) {
            throw new CliScriptException(e);
        }
    }

    /**
     * Creating CLI script for adding Resource-Adapter
     *
     * @param resourceAdapter object of Resource-Adapter
     * @param ctx  migration context
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createResAdapterScript(ResourceAdapterBean resourceAdapter, MigrationContext ctx)
            throws CliScriptException{
        String errMsg = " in Resource Adapter(Connection-Factories in AS5) must be set.";
        Utils.throwIfBlank(resourceAdapter.getArchive(), errMsg, "Archive name");

        StringBuilder resultBuilder = new StringBuilder();
        CliAddCommandBuilder cliBuilder = new CliAddCommandBuilder();

        String adapterScript = "/subsystem=resource-adapters/resource-adapter=" + resourceAdapter.getArchive() + ":add(";
        cliBuilder.addProperty("archive", resourceAdapter.getArchive());
        cliBuilder.addProperty("transaction-support", resourceAdapter.getTransactionSupport());

        adapterScript = adapterScript.concat(cliBuilder.asString() + ")\n");
        resultBuilder.append(adapterScript);



        if(resourceAdapter.getConnectionDefinitions() != null){
            for(ConnectionDefinitionBean connDef : resourceAdapter.getConnectionDefinitions()){
                if((connDef.getClassName() == null) || (connDef.getClassName().isEmpty())){
                    throw new CliScriptException("Class-name in the connection definition cannot be null or empty");
                }
                cliBuilder.clear();

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

                if(connDef.getSecurityDomain() != null){
                    cliBuilder.addProperty("security-domain", connDef.getSecurityDomain());
                }

                if(connDef.getSecDomainAndApp() != null){
                    cliBuilder.addProperty("security-domain-and-application", connDef.getSecDomainAndApp());
                }

                if(connDef.getAppManagedSec() != null){
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

                if(connDef.getConfigProperties() != null){
                    for(ConfigPropertyBean configProperty : connDef.getConfigProperties()){
                        // TODO: Check if something is required or if something can be not specified
                        resultBuilder.append("/subsystem=resource-adapters/resource-adapter=" + resourceAdapter.getArchive());
                        resultBuilder.append("/connection-definitions=" + connDef.getPoolName());
                        resultBuilder.append("/config-properties=" + configProperty.getConfigPropertyName() + ":add(");
                        resultBuilder.append("value=" + configProperty.getConfigProperty() + ")\n");
                    }
                }
            }
        }
        return resultBuilder.toString();

//        String script = "/subsystem=resource-adapters/resource-adapter=";
//
//        script = script.concat(resourceAdapter.getArchive() + ":add(");
//        script = script.concat("archive=" + resourceAdapter.getArchive());
//        script = ctx.checkingMethod(script, ", transaction-support", resourceAdapter.getTransactionSupport());
//        script = script.concat(")\n");
//
//        if(resourceAdapter.getConnectionDefinitions() != null){
//            for(ConnectionDefinitionBean connectionDefinition : resourceAdapter.getConnectionDefinitions()){
//                if((connectionDefinition.getClassName() == null) || (connectionDefinition.getClassName().isEmpty())){
//                    throw new CliScriptException("Class-name in the connection definition cannot be null or empty");
//                }
//
//                script =  script.concat("/subsystem=resource-adapters/resource-adapter=" + resourceAdapter.getArchive());
//                script = script.concat("/connection-definitions=" + connectionDefinition.getPoolName() + ":add(");
//                script = ctx.checkingMethod(script, " jndi-name", connectionDefinition.getJndiName());
//                script = ctx.checkingMethod(script, ", enabled", connectionDefinition.getEnabled());
//                script = ctx.checkingMethod(script, ", use-java-context", connectionDefinition.getUseJavaCont());
//                script = ctx.checkingMethod(script, ", class-name", connectionDefinition.getClassName());
//                script = ctx.checkingMethod(script, ", use-ccm", connectionDefinition.getUseCcm());
//                script = ctx.checkingMethod(script, ", prefill", connectionDefinition.getPrefill());
//                script = ctx.checkingMethod(script, ", use-strict-min", connectionDefinition.getUseStrictMin());
//                script = ctx.checkingMethod(script, ", flush-strategy", connectionDefinition.getFlushStrategy());
//                script = ctx.checkingMethod(script, ", min-pool-size", connectionDefinition.getMinPoolSize());
//                script = ctx.checkingMethod(script, ", max-pool-size", connectionDefinition.getMaxPoolSize());
//
//                // TODO: Change structure of ifs...
//                if(connectionDefinition.getSecurityDomain() != null){
//                    script = ctx.checkingMethod(script, ", security-domain", connectionDefinition.getSecurityDomain());
//                }
//
//                if(connectionDefinition.getSecDomainAndApp() != null){
//                    script = ctx.checkingMethod(script, ", security-domain-and-application",
//                            connectionDefinition.getSecDomainAndApp());
//                }
//
//                if(connectionDefinition.getAppManagedSec() != null){
//                    script = ctx.checkingMethod(script, ", application-managed-security",
//                            connectionDefinition.getAppManagedSec());
//                }
//
//                script = ctx.checkingMethod(script, ", background-validation", connectionDefinition.getBackgroundValidation());
//                script = ctx.checkingMethod(script, ", background-validation-millis", connectionDefinition.getBackgroundValiMillis());
//                script = ctx.checkingMethod(script, ", blocking-timeout-millis", connectionDefinition.getBackgroundValiMillis());
//                script = ctx.checkingMethod(script, ", idle-timeout-minutes", connectionDefinition.getIdleTimeoutMinutes());
//                script = ctx.checkingMethod(script, ", allocation-retry", connectionDefinition.getAllocationRetry());
//                script = ctx.checkingMethod(script, ", allocation-retry-wait-millis", connectionDefinition.getAllocRetryWaitMillis());
//                script = ctx.checkingMethod(script, ", xa-resource-timeout", connectionDefinition.getXaResourceTimeout());
//                script = script.concat(")\n");
//
//                if(connectionDefinition.getConfigProperties() != null){
//                    for(ConfigPropertyBean configProperty : connectionDefinition.getConfigProperties()){
//                        script = script.concat("/subsystem=resource-adapters/resource-adapter=" + resourceAdapter.getArchive());
//                        script = script.concat("/connection-definitions=" + connectionDefinition.getPoolName());
//                        script = script.concat("/config-properties=" + configProperty.getConfigPropertyName() + ":add(");
//                        script = script.concat("value=" + configProperty.getConfigProperty() + ")\n");
//                    }
//                }
//            }
//        }
//        return script;
    }


}
