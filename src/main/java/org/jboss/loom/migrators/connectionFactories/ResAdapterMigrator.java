/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.connectionFactories;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.loom.utils.as7.CliAddScriptBuilder;
import org.jboss.loom.utils.as7.CliApiCommandBuilder;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ctx.MigratorData;
import org.jboss.loom.actions.CliCommandAction;
import org.jboss.loom.actions.CopyFileAction;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.CliScriptException;
import org.jboss.loom.ex.LoadMigrationException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.migrators.connectionFactories.jaxb.*;
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
import org.apache.commons.lang.StringUtils;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;
import org.jboss.loom.utils.XmlUtils;

/**
 * Migrator of Resource Adapter(Connection Factories in AS5) subsystem implementing IMigrator
 *
 * @author Roman Jakubco
 */
@ConfigPartDescriptor(
    name = "Resource adapters configuration"
)
public class ResAdapterMigrator extends AbstractMigrator {
    private static final Logger log = LoggerFactory.getLogger(ResAdapterMigrator.class);

    @Override
    protected String getConfigPropertyModuleName() {
        return "resourceAdapter";
    }

    public ResAdapterMigrator(GlobalConfiguration globalConfig) {
        super(globalConfig);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSourceServerConfig(MigrationContext ctx) throws LoadMigrationException {
        try {

            // Deployments AS 5 dir.
            File dsFiles = getGlobalConfig().getAS5Config().getDeployDir();
            if (!dsFiles.canRead())
                throw new LoadMigrationException("Can't read: " + dsFiles.getPath());


            // -ds.xml files.
            SuffixFileFilter sf = new SuffixFileFilter("-ds.xml");
            Collection<File> dsXmls = FileUtils.listFiles(dsFiles, sf, FileFilterUtils.trueFileFilter());
            log.debug("  Found -ds.xml files #: " + dsXmls.size());
            if (dsXmls.isEmpty())
                return;

            List<ConnectionFactoriesBean> connFactories = new LinkedList();
            Unmarshaller dataUnmarshaller = JAXBContext.newInstance(ConnectionFactoriesBean.class).createUnmarshaller();

            // For each -ds.xml
            for (File dsXml : dsXmls) {
                Document doc = XmlUtils.parseFileToXmlDoc(dsXml);

                Element element = doc.getDocumentElement();
                if ("connection-factories".equals(element.getTagName())) {
                    ConnectionFactoriesBean conn = (ConnectionFactoriesBean) dataUnmarshaller.unmarshal(dsXml);
                    connFactories.add(conn);
                }
            }

            MigratorData migrData = new MigratorData();

            for (ConnectionFactoriesBean cf : connFactories) {
                if (cf.getConnectionFactories() != null) {
                    migrData.getConfigFragments().addAll(cf.getConnectionFactories());
                }
                if (cf.getNoTxConnectionFactories() != null) {
                    migrData.getConfigFragments().addAll(cf.getNoTxConnectionFactories());
                }
            }

            ctx.getMigrationData().put(ResAdapterMigrator.class, migrData);

        } catch (JAXBException | SAXException | IOException e) {
            throw new LoadMigrationException(e);
        }
    }

    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void createActions(MigrationContext ctx) throws MigrationException {
        
        Set<String> referencedRARs = new HashSet();
        
        // Process the config fragments found in AS 5.
        for( IConfigFragment fragment : ctx.getMigrationData().get(ResAdapterMigrator.class).getConfigFragments() ) {

            String raType = null;
            try {
                if( fragment instanceof ConnectionFactoryAS5Bean ) {
                    raType = "tx-connection-factory";
                    ctx.getActions().addAll(createResourceAdapterCliCommand(
                            migrateConnFactory( (ConnectionFactoryAS5Bean) fragment, referencedRARs )));
                    continue;
                }
                if( fragment instanceof NoTxConnectionFactoryAS5Bean ) {
                    raType = "no-tx-connection-factory";
                    ctx.getActions().addAll(createResourceAdapterCliCommand(
                            migrateConnFactory( (NoTxConnectionFactoryAS5Bean) fragment, referencedRARs )));
                    continue;
                }
                throw new MigrationException("Config fragment unrecognized by " + this.getClass().getSimpleName() + ": " + fragment);

            } catch (CliScriptException e) {
                throw new MigrationException("Migration of " + raType +" failed: " + e.getMessage(), e);
            }
        }

        // Try to find each .rar referenced in the configuration XML.
        for( String referencedRAR : referencedRARs ) {
            
            final File profileDir = getGlobalConfig().getAS5Config().getProfileDir(); //  .../deployments ?
            Collection<File> foundRARs;
            try {
                foundRARs = Utils.searchForFileOrDir(referencedRAR, profileDir);
            } catch ( IOException ex ) {
                throw new MigrationException("Can't find " + referencedRAR + ": " + ex.getMessage(), ex);
            }
            File rarFrom = foundRARs.iterator().next();
            File rarTo = Utils.createPath(getGlobalConfig().getAS7Config().getDir(), "standalone", "deployments", rarFrom.getName());
            CopyFileAction action = new CopyFileAction( this.getClass(), rarFrom, rarTo, CopyFileAction.IfExists.SKIP);
            if( foundRARs.size() > 1 ){
                String warn = "Found multiple " + referencedRAR + " in " + profileDir + ":\n  " + StringUtils.join( foundRARs, "\n  ");
                action.addWarning( warn );
            }
            ctx.getActions().add( action );
        }
    }

    
    
    /**
     * Migrates Connection-Factory (both types) from AS5 to Resource-Adapter in AS7
     *
     * @param connFactoryAS5 Connection-Factory for migration
     * @param rars Files already set for copy into AS7
     * @return created Resource-Adapter with the configuration of the given Connection-Factory
     */
    private static ResourceAdapterBean migrateConnFactory(AbstractConnectionFactoryAS5Bean connFactoryAS5, Set<String> rars){
        ResourceAdapterBean resAdapter = new ResourceAdapterBean();

        rars.add(connFactoryAS5.getRarName());

        resAdapter.setJndiName(connFactoryAS5.getJndiName());
        resAdapter.setArchive(connFactoryAS5.getRarName());

        ConnectionDefinitionBean connDef = new ConnectionDefinitionBean();

        if(connFactoryAS5 instanceof ConnectionFactoryAS5Bean){
            if ( ( ( ConnectionFactoryAS5Bean )connFactoryAS5 ).getXaTransaction() != null ) {
                resAdapter.setTransactionSupport( "XATransaction" );
                ConnectionFactoryAS5Bean tempFactory = ( ConnectionFactoryAS5Bean ) connFactoryAS5;

                connDef.setXaResourceTimeout(tempFactory.getXaResourceTimeout());
                connDef.setXaMaxPoolSize(tempFactory.getMaxPoolSize());
                connDef.setXaMinPoolSize( tempFactory.getMinPoolSize() );
                connDef.setXaNoTxSeparatePools(tempFactory.getNoTxSeparatePools());
                connDef.setXaPrefill( tempFactory.getPrefill() );

            } else {
                resAdapter.setTransactionSupport( "LocalTransaction" );
                connDef.setMaxPoolSize( connFactoryAS5.getMaxPoolSize() );
                connDef.setMinPoolSize( connFactoryAS5.getMinPoolSize() );
                connDef.setPrefill( connFactoryAS5.getPrefill() );
            }
        } else{
            resAdapter.setTransactionSupport( "NoTransaction" );
        }

        connDef.setJndiName( "java:jboss/" + connFactoryAS5.getJndiName() );
        connDef.setPoolName( connFactoryAS5.getJndiName() );
        connDef.setEnabled( "true" );
        connDef.setUseJavaCont( "true" );
        connDef.setEnabled( "true" );
        connDef.setClassName( connFactoryAS5.getConnectionDefinition() );
        connDef.setPrefill( connFactoryAS5.getPrefill() );

        if ( connFactoryAS5.getConfigProperties() != null ) {
            for (ConfigPropertyBean configProperty : connFactoryAS5.getConfigProperties()) {
                configProperty.setType( null );
            }
            connDef.setConfigProperties( connFactoryAS5.getConfigProperties() );
        }

        if ( connFactoryAS5.getApplicationManagedSecurity() != null ) {
            connDef.setAppManagedSec(connFactoryAS5.getApplicationManagedSecurity());
        } else if ( connFactoryAS5.getSecurityDomain() != null ) {
            connDef.setSecurityDomain(connFactoryAS5.getSecurityDomain());
        } else if ( connFactoryAS5.getSecDomainAndApp() != null ) {
            connDef.setSecDomainAndApp(connFactoryAS5.getSecDomainAndApp());
        }

        connDef.setMinPoolSize( connFactoryAS5.getMinPoolSize() );
        connDef.setMaxPoolSize( connFactoryAS5.getMaxPoolSize() );

        connDef.setBackgroundValidation( connFactoryAS5.getBackgroundValid() );
        connDef.setBackgroundValiMillis( connFactoryAS5.getBackgroundValiMillis() );

        connDef.setBlockingTimeoutMillis( connFactoryAS5.getBlockingTimeoutMillis() );
        connDef.setIdleTimeoutMinutes( connFactoryAS5.getIdleTimeoutMin() );
        connDef.setAllocationRetry( connFactoryAS5.getAllocationRetry() );
        connDef.setAllocRetryWaitMillis( connFactoryAS5.getAllocRetryWaitMillis() );

        Set<ConnectionDefinitionBean> connDefColl = new HashSet();
        connDefColl.add(connDef);
        resAdapter.setConnectionDefinitions( connDefColl );

        return resAdapter;
    }

    /**
     * Creates a list of CliCommandActions for adding a Resource-Adapter
     *
     * @param adapter Resource-Adapter
     * @return list of created CliCommandActions for adding the Resource-Adapter
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Resource-Adapter are
     *                            missing or are empty (archive-name)
     */
    private static List<CliCommandAction> createResourceAdapterCliCommand(ResourceAdapterBean adapter)
            throws CliScriptException {
        String errMsg = " in resource-adapter(connection-factories in AS5) must be set.";
        Utils.throwIfBlank(adapter.getArchive(), errMsg, "Archive name");

        List<CliCommandAction> actions = new LinkedList();

        ModelNode adapterCmd = new ModelNode();
        adapterCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        adapterCmd.get(ClientConstants.OP_ADDR).add("subsystem", "resource-adapters");
        adapterCmd.get(ClientConstants.OP_ADDR).add("resource-adapter", adapter.getJndiName());

        CliApiCommandBuilder builder = new CliApiCommandBuilder(adapterCmd);

        builder.addPropertyIfSet("archive", adapter.getArchive());
        builder.addPropertyIfSet("transaction-support", adapter.getTransactionSupport());

        actions.add( new CliCommandAction( ResAdapterMigrator.class, createResAdapterScript(adapter), builder.getCommand()));

        if (adapter.getConnectionDefinitions() != null) {
            for (ConnectionDefinitionBean connDef : adapter.getConnectionDefinitions()) {
                actions.addAll(createConDefinitionCliAction(adapter, connDef));
            }
        }

        return actions;
    }

    /**
     * Creates CliCommandAction for adding a Connection-Definition of the specific Resource-Adapter
     *
     * @param adapter Resource-Adapter containing connection-definition
     * @param def     Connection-Definition
     * @return created CliCommandAction for adding the Connection-Definition
     * @throws CliScriptException
     *          if required attributes for a creation of the CLI command of the Connection-Definition
     *          are missing or are empty (class-name, pool-name)
     */
    private static List<CliCommandAction> createConDefinitionCliAction(ResourceAdapterBean adapter,
                                                                       ConnectionDefinitionBean def)
            throws CliScriptException {
        String errMsg = "in connection-definition in resource-adapter(connection-factories) must be set";
        Utils.throwIfBlank(def.getClassName(), errMsg, "Class-name");
        Utils.throwIfBlank(def.getPoolName(), errMsg, "Pool-name");

        List<CliCommandAction> actions = new LinkedList();

        ModelNode connDefCmd = new ModelNode();
        connDefCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        connDefCmd.get(ClientConstants.OP_ADDR).add("subsystem", "resource-adapters");
        connDefCmd.get(ClientConstants.OP_ADDR).add("resource-adapter", adapter.getJndiName());
        connDefCmd.get(ClientConstants.OP_ADDR).add("connection-definitions", def.getPoolName());

        CliApiCommandBuilder builder = new CliApiCommandBuilder(connDefCmd);

        builder.addPropertyIfSet("jndi-name", def.getJndiName());
        builder.addPropertyIfSet("enabled", def.getEnabled());
        builder.addPropertyIfSet("use-java-context", def.getUseJavaCont());
        builder.addPropertyIfSet("class-name", def.getClassName());
        builder.addPropertyIfSet("use-ccm", def.getUseCcm());

        builder.addPropertyIfSet("flush-strategy", def.getFlushStrategy());

        if(adapter.getTransactionSupport().equalsIgnoreCase("xatransaction")){
            builder.addPropertyIfSet("min-pool-size", def.getXaMinPoolSize());
            builder.addPropertyIfSet("max-pool-size", def.getXaMaxPoolSize());
            builder.addPropertyIfSet("prefill", def.getXaPrefill());
            builder.addPropertyIfSet("xa-resource-timeout", def.getXaResourceTimeout());
            builder.addPropertyIfSet("no-tx-separate-pools", def.getXaNoTxSeparatePools());
            builder.addPropertyIfSet("use-strict-min",def.getXaUseStrictMin());
        } else{
            builder.addPropertyIfSet("min-pool-size", def.getMinPoolSize());
            builder.addPropertyIfSet("max-pool-size", def.getMaxPoolSize());
            builder.addPropertyIfSet("prefill", def.getPrefill());
            builder.addPropertyIfSet("use-strict-min", def.getUseStrictMin());
        }

        if (def.getSecurityDomain() != null) {
            builder.addPropertyIfSet("security-domain", def.getSecurityDomain());
        } else if (def.getSecDomainAndApp() != null) {
            builder.addPropertyIfSet("security-domain-and-application", def.getSecDomainAndApp());
        } else if (def.getAppManagedSec() != null) {
            builder.addPropertyIfSet("application-managed-security", def.getAppManagedSec());
        }

        builder.addPropertyIfSet("background-validation", def.getBackgroundValidation());
        builder.addPropertyIfSet("background-validation-millis", def.getBackgroundValiMillis());
        builder.addPropertyIfSet("blocking-timeout-millis", def.getBlockingTimeoutMillis());
        builder.addPropertyIfSet("idle-timeout-minutes", def.getIdleTimeoutMinutes());
        builder.addPropertyIfSet("allocation-retry", def.getAllocationRetry());
        builder.addPropertyIfSet("allocation-retry-wait-millis", def.getAllocRetryWaitMillis());
        builder.addPropertyIfSet("xa-resource-timeout", def.getXaResourceTimeout());

        actions.add( new CliCommandAction( ResAdapterMigrator.class, createConnDefinitionScript(adapter, def), builder.getCommand()));

        if (def.getConfigProperties() != null) {
            for (ConfigPropertyBean configProperty : def.getConfigProperties()) {
                actions.add(createPropertyCliAction(adapter, def, configProperty));

            }
        }

        return actions;
    }

    /**
     * Creates CliCommandAction for adding a Config-Property of the specific Connection-Definition of the specific
     * Resource-Adapter
     *
     * @param adapter  Resource-Adapter containing Connection-Definition
     * @param def      Connection-Definition containg Config-Property
     * @param property Config-Property
     * @return created CliCommandAction for adding the Config-Property
     * @throws CliScriptException if required attributes for a creation of the CLI command of the config-property
     *                            are missing or are empty (name)
     */
    private static CliCommandAction createPropertyCliAction(ResourceAdapterBean adapter, ConnectionDefinitionBean def,
                                                            ConfigPropertyBean property) throws CliScriptException {
        String errMsg = "of config-property in connection-definition in resource-adapter must be set.";
        Utils.throwIfBlank(property.getConfigPropertyName(), errMsg, "Name");

        ModelNode propertyCmd = new ModelNode();
        propertyCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        propertyCmd.get(ClientConstants.OP_ADDR).add("subsystem", "resource-adapters");
        propertyCmd.get(ClientConstants.OP_ADDR).add("resource-adapter", adapter.getJndiName());
        propertyCmd.get(ClientConstants.OP_ADDR).add("connection-definitions", def.getPoolName());
        propertyCmd.get(ClientConstants.OP_ADDR).add("config-properties", property.getConfigPropertyName());

        propertyCmd.get("value").set(property.getConfigProperty());

        return new CliCommandAction( ResAdapterMigrator.class, createPropertyScript(adapter, def, property), propertyCmd);
    }

    /**
     * Creates CLI script for adding of the Resource-Adapter
     *
     * @param resourceAdapter object of Resource-Adapter
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    private static String createResAdapterScript(ResourceAdapterBean resourceAdapter)
            throws CliScriptException {
        String errMsg = " in resource-adapter(connection-factories in AS5) must be set.";
        Utils.throwIfBlank(resourceAdapter.getArchive(), errMsg, "Archive name");


        StringBuilder resultBuilder = new StringBuilder();
        CliAddScriptBuilder cliBuilder = new CliAddScriptBuilder();

        String adapterScript = "/subsystem=resource-adapters/resource-adapter=" + resourceAdapter.getJndiName() + ":add(";
        cliBuilder.addProperty("archive", resourceAdapter.getArchive());
        cliBuilder.addProperty("transaction-support", resourceAdapter.getTransactionSupport());

        adapterScript = adapterScript.concat(cliBuilder.asString() + ")");
        resultBuilder.append(adapterScript);

        return resultBuilder.toString();
    }

    /**
     * Creates String containing CLI script for adding a Connection-Definition of the specific Resource-Adapter
     *
     * @param adapter Resource-Adapter containing Connection-Definition
     * @param connDef Connection-Definition
     * @return String containing CLI script for adding of the Connection-Definition
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Connection-Definition
     *                            are missing or are empty (class-name, pool-name)
     */
    private static String createConnDefinitionScript(ResourceAdapterBean adapter, ConnectionDefinitionBean connDef)
            throws CliScriptException {
        String errMsg = "in connection-definition in resource-adapter(connection-factories) must be set";
        Utils.throwIfBlank(connDef.getClassName(), errMsg, "Class-name");
        Utils.throwIfBlank(connDef.getPoolName(), errMsg, "Pool-name");

        CliAddScriptBuilder cliBuilder = new CliAddScriptBuilder();

        StringBuilder script = new StringBuilder("/subsystem=resource-adapters/resource-adapter=" +
                adapter.getJndiName());

        script.append("/connection-definitions=").append(connDef.getPoolName()).append(":add(");

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
        } else if (connDef.getSecDomainAndApp() != null) {
            cliBuilder.addProperty("security-domain-and-application", connDef.getSecDomainAndApp());
        } else if (connDef.getAppManagedSec() != null) {
            cliBuilder.addProperty("application-managed-security", connDef.getAppManagedSec());
        }

        cliBuilder.addProperty("background-validation", connDef.getBackgroundValidation());
        cliBuilder.addProperty("background-validation-millis", connDef.getBackgroundValiMillis());
        cliBuilder.addProperty("blocking-timeout-millis", connDef.getBlockingTimeoutMillis());
        cliBuilder.addProperty("idle-timeout-minutes", connDef.getIdleTimeoutMinutes());
        cliBuilder.addProperty("allocation-retry", connDef.getAllocationRetry());
        cliBuilder.addProperty("allocation-retry-wait-millis", connDef.getAllocRetryWaitMillis());
        cliBuilder.addProperty("xa-resource-timeout", connDef.getXaResourceTimeout());

        script.append(cliBuilder.asString()).append(")");

        return script.toString();
    }

    /**
     * Creates CLI script for adding a Config-Property of the specific Connection-Definition of the specific
     * Resource-Adapter
     *
     * @param adapter  Resource-Adapter containing Connection-Definition
     * @param connDef  Connection-Definition containing Config-Property
     * @param property Config-Property
     * @return string containing CLI script for adding the Config-Property
     * @throws CliScriptException if required attributes for a creation of the CLI command of the config-property
     *                            are missing or are empty (name)
     */
    private static String createPropertyScript(ResourceAdapterBean adapter, ConnectionDefinitionBean connDef,
                                               ConfigPropertyBean property) throws CliScriptException {
        String errMsg = "of config-property in connection-definition in resource-adapter must be set.";
        Utils.throwIfBlank(property.getConfigPropertyName(), errMsg, "Name");

        StringBuilder script = new StringBuilder("/subsystem=resource-adapters/resource-adapter=");

        script.append(adapter.getJndiName());

        script.append("/connection-definitions=").append(connDef.getPoolName());

        script.append("/config-properties=");
        script.append(property.getConfigPropertyName());

        script.append(":add(");
        script.append("value=").append(property.getConfigProperty()).append(")");

        return script.toString();
    }

}// ResAdapterMigrator
