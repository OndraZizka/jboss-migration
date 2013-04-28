package org.jboss.loom.migrators.server;

import org.apache.commons.collections.map.MultiValueMap;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.loom.CliAddScriptBuilder;
import org.jboss.loom.CliApiCommandBuilder;
import org.jboss.loom.MigrationContext;
import org.jboss.loom.MigrationData;
import org.jboss.loom.actions.CliCommandAction;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.CliScriptException;
import org.jboss.loom.ex.LoadMigrationException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.ex.NodeGenerationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.migrators.server.jaxb.*;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.utils.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Migrator of server subsystem implementing IMigrator.
 *
 * @author Roman Jakubco
 */

public class ServerMigrator extends AbstractMigrator {

    @Override
    protected String getConfigPropertyModuleName() {
        return "server";
    }


    private Set<SocketBindingBean> socketTemp = new HashSet();

    private Set<SocketBindingBean> socketBindings = new HashSet();

    private Integer randomSocket = 1;

    private Integer randomConnector = 1;

    public ServerMigrator(GlobalConfiguration globalConfig, MultiValueMap config) {
        super(globalConfig, config);
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException {

        // TBC: Maybe use FileUtils and list all files with that name?
        File file = Utils.createPath(
                super.getGlobalConfig().getAS5Config().getDir(), "server",
                super.getGlobalConfig().getAS5Config().getProfileName(), "deploy",
                "jbossweb.sar", "server.xml");

        if (!file.canRead())
            throw new LoadMigrationException("Cannot find/open file: " + file.getAbsolutePath(), new FileNotFoundException());

        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(ServerAS5Bean.class).createUnmarshaller();

            ServerAS5Bean serverAS5 = (ServerAS5Bean) unmarshaller.unmarshal(file);

            MigrationData mData = new MigrationData();
            for (ServiceBean s : serverAS5.getServices()) {
                mData.getConfigFragments().add(s.getEngine());
                mData.getConfigFragments().addAll(s.getConnectorAS5s());
            }

            ctx.getMigrationData().put(ServerMigrator.class, mData);
        } catch (JAXBException e) {
            throw new LoadMigrationException("Failed parsing logging config file: " + file.getPath(), e);
        }
    }

    @Override
    public void createActions(MigrationContext ctx) throws MigrationException {
        try {
            createDefaultSockets(ctx);
        } catch (LoadMigrationException e) {
            throw new MigrationException("Migration of web server failed: " + e.getMessage(), e);
        }
        Set<String> keystores = new HashSet();

        for( IConfigFragment fragment : ctx.getMigrationData().get(ServerMigrator.class).getConfigFragments() ) {
            String what = null;
            try {
                if (fragment instanceof ConnectorAS5Bean) {
                    what = "connector";
                    ctx.getActions().addAll(createConnectorCliAction(migrateConnector((ConnectorAS5Bean) fragment, keystores)));
                }
                else if (fragment instanceof EngineBean) {
                    what = "Engine (virtual-server)";
                    ctx.getActions().add(createVirtualServerCliAction(migrateEngine((EngineBean) fragment)));
                }
                else
                    throw new MigrationException("Config fragment unrecognized by " + this.getClass().getSimpleName() + ": " + fragment);
            }
            catch (CliScriptException | NodeGenerationException e) {
                throw new MigrationException("Migration of the " + what + " failed: " + e.getMessage(), e);
            }
        }

        for (SocketBindingBean sb : this.socketBindings) {
            try {
                ctx.getActions().add(createSocketBindingCliAction(sb));
            } catch (CliScriptException e) {
                throw new MigrationException("Creation of the new socket-binding failed: " + e.getMessage(), e);
            }
       }
    }

    /**
     * Migrates a connector from AS5 to AS7
     *
     * @param connector object representing connector in AS5
     * @return migrated AS7's connector
     * @throws NodeGenerationException if socket-binding cannot be created or set
     */
    private ConnectorAS7Bean migrateConnector(ConnectorAS5Bean connector, Set<String> keystores)
            throws NodeGenerationException {
        ConnectorAS7Bean connAS7 = new ConnectorAS7Bean();

        connAS7.setEnabled("true");
        connAS7.setEnableLookups(connector.getEnableLookups());
        connAS7.setMaxPostSize(connector.getMaxPostSize());
        connAS7.setMaxSavePostSize(connector.getMaxSavePostSize());
        connAS7.setProtocol(connector.getProtocol());
        connAS7.setProxyName(connector.getProxyName());
        connAS7.setProxyPort(connector.getProxyPort());
        connAS7.setRedirectPort(connector.getRedirectPort());

        // Ajp connector need scheme too. So http is set.
        connAS7.setScheme("http");

        
        // Socket-binding
        String protocol = null;
        if( connector.getProtocol().equals("HTTP/1.1") ) {
                protocol = "true".equalsIgnoreCase( connector.getSslEnabled() ) ? "https" : "http";
        } else {
            // TODO: This can't be just assumed!
            protocol = "ajp";
        }
        connAS7.setSocketBinding(createSocketBinding(connector.getPort(), protocol));

        // Name
        connAS7.setConnectorName(protocol);

        
        // SSL enabled?
        if( "true".equalsIgnoreCase( connector.getSslEnabled() ) ) {
            connAS7.setScheme("https");
            connAS7.setSecure(connector.getSecure());

            connAS7.setSslName("ssl");
            connAS7.setVerifyClient(connector.getClientAuth());
            // TODO: Problem with file location
            if( connector.getKeystoreFile() != null ) {
//                File fName =
//                keystores.add(connector.getKeystoreFile());


                connAS7.setCertifKeyFile("${jboss.server.config.dir}/keystores/" + connector.getKeystoreFile());
            }

            // TODO: No sure which protocols can be in AS5.
            if ((connector.getSslProtocol().equals("TLS")) || (connector.getSslProtocol() == null)) {
                connAS7.setSslProtocol("TLSv1");
            } else{
                connAS7.setSslProtocol(connector.getSslProtocol());
            }

            connAS7.setCiphers(connector.getCiphers());
            connAS7.setKeyAlias(connAS7.getKeyAlias());

            // TODO: AS 7 has just one password, while AS 5 has keystorePass and truststorePass.
            connAS7.setPassword(connector.getKeystorePass());
        }

        return connAS7;
    }

    /**
     * Migrates a Engine from AS5 to AS7
     *
     * @param engine object representing Engine
     * @return created virtual-server
     */
    private static VirtualServerBean migrateEngine(EngineBean engine) {
        VirtualServerBean virtualServer = new VirtualServerBean();
        virtualServer.setVirtualServerName(engine.getEngineName());
        virtualServer.setEnableWelcomeRoot("true");
        virtualServer.setAliasName(engine.getHostNames());

        if (engine.getAliases() != null) {
            virtualServer.getAliasName().addAll(engine.getAliases());
        }

        return virtualServer;
    }

    /**
     * Loads socket-bindings, which are already defined in fresh standalone files.
     *
     * @param ctx migration context
     * @throws LoadMigrationException if unmarshalling socket-bindings from standalone file fails
     */
    private void createDefaultSockets(MigrationContext ctx) throws LoadMigrationException {
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(SocketBindingBean.class).createUnmarshaller();

            // TODO:  Read over Management API.
            NodeList bindings = ctx.getAS7ConfigXmlDoc().getElementsByTagName("socket-binding");
            for (int i = 0; i < bindings.getLength(); i++) {
                if (!(bindings.item(i) instanceof Element)) {
                    continue;
                }
                SocketBindingBean socketBinding = (SocketBindingBean) unmarshaller.unmarshal(bindings.item(i));
                if ((socketBinding.getSocketName() != null) || (socketBinding.getSocketPort() != null)) {
                    this.socketTemp.add(socketBinding);
                }

            }
        } catch (JAXBException e) {
            throw new LoadMigrationException("Parsing of socket-bindings in standalone file failed: " + e.getMessage(), e);
        }

    }

    /**
     * Creates a socket-binding if it doesn't already exists.
     *
     * @param port port of the connector, which will be converted to socket-binding
     * @param name name of the protocol which is used by connector (ajp/http/https)
     * @return name of the socket-binding so it cant be referenced in connector
     * @throws NodeGenerationException if createDefaultSocket fails to unmarshall socket-bindings
     */
    private String createSocketBinding(String port, String name) throws NodeGenerationException {
        for (SocketBindingBean sb : this.socketTemp) {
            if (sb.getSocketPort().equals(port)) {
                return sb.getSocketName();
            }
            if (sb.getSocketName().equals(name)) {
                name = "createdSocket";
            }
        }

        SocketBindingBean socketBinding = new SocketBindingBean();

        for (SocketBindingBean sb : this.socketBindings) {
            if (sb.getSocketPort().equals(port)) {
                return sb.getSocketName();
            }
        }

        for (SocketBindingBean sb : this.socketBindings) {
            if (sb.getSocketName().equals(name)) {
                name = name.concat(this.randomSocket.toString());
                this.randomSocket++;
            }
        }

        socketBinding.setSocketName(name);
        socketBinding.setSocketPort(port);

        this.socketBindings.add(socketBinding);

        return name;
    }

    /**
     * Creates a list of CliCommandActions for adding a Connector
     *
     * @param connAS7 Connector
     * @return created list containing CliCommandActions for adding the Connector
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Connector
     *                            are missing or are empty (socket-binding, connector-name, protocol)
     */
    private static List<CliCommandAction> createConnectorCliAction(ConnectorAS7Bean connAS7) throws CliScriptException {
        String errMsg = " in connector must be set.";
        Utils.throwIfBlank(connAS7.getScheme(), errMsg, "Scheme");
        Utils.throwIfBlank(connAS7.getSocketBinding(), errMsg, "Socket-binding");
        Utils.throwIfBlank(connAS7.getConnectorName(), errMsg, "Connector name");
        Utils.throwIfBlank(connAS7.getProtocol(), errMsg, "Protocol");

        List<CliCommandAction> actions = new LinkedList();


        actions.add( new CliCommandAction( ServerMigrator.class, createConnectorScript(connAS7), createConnectorModelNode( connAS7 )));

        if (connAS7.getScheme().equals("https")) {
            actions.add( new CliCommandAction( ServerMigrator.class, 
                    createSSLConfScript( connAS7 ), 
                    createSSLConfModelNode( connAS7 )));
        }

        return actions;
    }
    
    private static ModelNode createConnectorModelNode( ConnectorAS7Bean connAS7 ){
        ModelNode connCmd = new ModelNode();
        connCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        connCmd.get(ClientConstants.OP_ADDR).add("subsystem", "web");
        connCmd.get(ClientConstants.OP_ADDR).add("connector", connAS7.getConnectorName());

        CliApiCommandBuilder builder = new CliApiCommandBuilder(connCmd);

        builder.addProperty("socket-binding", connAS7.getSocketBinding());
        builder.addProperty("enable-lookups", connAS7.getEnableLookups());
        builder.addProperty("max-post-size", connAS7.getMaxPostSize());
        builder.addProperty("max-save-post-size", connAS7.getMaxSavePostSize());
        builder.addProperty("max-connections", connAS7.getMaxConnections());
        builder.addProperty("protocol", connAS7.getProtocol());
        builder.addProperty("proxy-name", connAS7.getProxyName());
        builder.addProperty("proxy-port", connAS7.getProxyPort());
        builder.addProperty("redirect-port", connAS7.getRedirectPort());
        builder.addProperty("scheme", connAS7.getScheme());
        builder.addProperty("secure", connAS7.getSecure());
        builder.addProperty("enabled", connAS7.getEnabled());
        return builder.getCommand();
    }
    
    private static ModelNode createSSLConfModelNode( ConnectorAS7Bean connAS7 ){
        ModelNode sslConf = new ModelNode();
        sslConf.get(ClientConstants.OP).set(ClientConstants.ADD);
        sslConf.get(ClientConstants.OP_ADDR).add("subsystem", "web");
        sslConf.get(ClientConstants.OP_ADDR).add("connector", connAS7.getConnectorName());
        sslConf.get(ClientConstants.OP_ADDR).add("ssl", "configuration");

        CliApiCommandBuilder sslBuilder = new CliApiCommandBuilder(sslConf);

        sslBuilder.addProperty("name", connAS7.getSslName());
        sslBuilder.addProperty("verify-client", connAS7.getVerifyClient());
        sslBuilder.addProperty("verify-depth", connAS7.getVerifyDepth());
        sslBuilder.addProperty("certificate-key-file", connAS7.getCertifKeyFile());
        sslBuilder.addProperty("password", connAS7.getPassword());
        sslBuilder.addProperty("protocol", connAS7.getProtocol());
        sslBuilder.addProperty("ciphers", connAS7.getCiphers());
        sslBuilder.addProperty("key-alias", connAS7.getKeyAlias());
        sslBuilder.addProperty("ca-certificate-file", connAS7.getCaCertifFile());
        sslBuilder.addProperty("session-cache-size", connAS7.getSessionCacheSize());
        sslBuilder.addProperty("session-timeout", connAS7.getSessionTimeout());
        return sslBuilder.getCommand();
    }
    

    /**
     * Creates CliCommandAction for adding a Virtual-Server
     *
     * @param server Virtual-Server
     * @return created CliCommandAction for adding the Virtual-Server
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Virtual-Server
     *                            are missing or are empty (server-name)
     */
    public static CliCommandAction createVirtualServerCliAction(VirtualServerBean server) throws CliScriptException {
        String errMsg = "in virtual-server (engine in AS5) must be set";
        Utils.throwIfBlank(server.getVirtualServerName(), errMsg, "Server name");

        ModelNode serverCmd = new ModelNode();
        serverCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        serverCmd.get(ClientConstants.OP_ADDR).add("subsystem", "web");
        serverCmd.get(ClientConstants.OP_ADDR).add("virtual-server", server.getVirtualServerName());

        if (server.getAliasName() != null) {
            ModelNode aliasesNode = new ModelNode();
            for (String alias : server.getAliasName()) {
                ModelNode aliasNode = new ModelNode();

                aliasNode.set(alias);
                aliasesNode.add(aliasNode);
            }
            serverCmd.get("alias").set(aliasesNode);
        }

        CliApiCommandBuilder builder = new CliApiCommandBuilder(serverCmd);

        builder.addProperty("enable-welcome-root", server.getEnableWelcomeRoot());
        builder.addProperty("default-web-module", server.getDefaultWebModule());

        return new CliCommandAction( ServerMigrator.class, createVirtualServerScript(server), builder.getCommand());
    }

    /**
     * Creates CliCommandAction for adding a Socket-Binding
     *
     * @param socket Socket-Binding
     * @return created CliCommandAction for adding the Socket-Binding
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Security-Domain
     *                            are missing or are empty (security-domain-name)
     */
    public static CliCommandAction createSocketBindingCliAction(SocketBindingBean socket) throws CliScriptException {
        String errMsg = " in socket-binding must be set.";
        Utils.throwIfBlank(socket.getSocketPort(), errMsg, "Port");
        Utils.throwIfBlank(socket.getSocketName(), errMsg, "Name");

        ModelNode serverCmd = new ModelNode();
        serverCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        serverCmd.get(ClientConstants.OP_ADDR).add("socket-binding-group", "standard-sockets");
        serverCmd.get(ClientConstants.OP_ADDR).add("socket-binding", socket.getSocketName());

        serverCmd.get("port").set(socket.getSocketPort());

        CliApiCommandBuilder builder = new CliApiCommandBuilder(serverCmd);
        builder.addProperty("interface", socket.getSocketInterface());

        return new CliCommandAction( ServerMigrator.class, createSocketBindingScript(socket), builder.getCommand());
    }

    /**
     * Creates a CLI script for adding a Connector
     *
     * @param connAS7 object of migrated connector
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     * 
     * @deprecated  Generate this from the ModelNode.
     */
    private static String createConnectorScript(ConnectorAS7Bean connAS7) throws CliScriptException {
        String errMsg = " in connector must be set.";
        Utils.throwIfBlank(connAS7.getScheme(), errMsg, "Scheme");
        Utils.throwIfBlank(connAS7.getSocketBinding(), errMsg, "Socket-binding");
        Utils.throwIfBlank(connAS7.getConnectorName(), errMsg, "Connector name");
        Utils.throwIfBlank(connAS7.getProtocol(), errMsg, "Protocol");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=web/connector=");

        resultScript.append(connAS7.getConnectorName()).append(":add(");

        builder.addProperty("socket-binding", connAS7.getSocketBinding());
        builder.addProperty("enable-lookups", connAS7.getEnableLookups());
        builder.addProperty("max-post-size", connAS7.getMaxPostSize());
        builder.addProperty("max-save-post-size", connAS7.getMaxSavePostSize());
        builder.addProperty("max-connections", connAS7.getMaxConnections());
        builder.addProperty("protocol", connAS7.getProtocol());
        builder.addProperty("proxy-name", connAS7.getProxyName());
        builder.addProperty("proxy-port", connAS7.getProxyPort());
        builder.addProperty("redirect-port", connAS7.getRedirectPort());
        builder.addProperty("scheme", connAS7.getScheme());
        builder.addProperty("secure", connAS7.getSecure());
        builder.addProperty("enabled", connAS7.getEnabled());

        resultScript.append(builder.asString()).append(")");

        return resultScript.toString();
    }

    /**
     * Creates a CLI script for adding a SSL configuration of the Connector
     *
     * @param connAS7 Connector containing SSL configuration
     * @return created string containing the CLI script for adding the SSL configuration
     * 
     * @deprecated  Generate this from the ModelNode.
     */
    private static String createSSLConfScript(ConnectorAS7Bean connAS7) {
        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=web/connector=" + connAS7.getConnectorName());

        resultScript.append("/ssl=configuration:add(");

        builder.addProperty("name", connAS7.getSslName());
        builder.addProperty("verify-client", connAS7.getVerifyClient());
        builder.addProperty("verify-depth", connAS7.getVerifyDepth());
        builder.addProperty("certificate-key-file", connAS7.getCertifKeyFile());
        builder.addProperty("password", connAS7.getPassword());
        builder.addProperty("protocol", connAS7.getProtocol());
        builder.addProperty("ciphers", connAS7.getCiphers());
        builder.addProperty("key-alias", connAS7.getKeyAlias());
        builder.addProperty("ca-certificate-file", connAS7.getCaCertifFile());
        builder.addProperty("session-cache-size", connAS7.getSessionCacheSize());
        builder.addProperty("session-timeout", connAS7.getSessionTimeout());

        resultScript.append(builder.asString()).append(")");

        return resultScript.toString();
    }

    /**
     * Creates a CLI script for adding virtual-server to AS7
     *
     * @param virtualServer object representing migrated virtual-server
     * @return string containing created CLI script
     * 
     * @deprecated  Generate this from the ModelNode.
     */
    private static String createVirtualServerScript(VirtualServerBean virtualServer) throws CliScriptException {
        String errMsg = "in virtual-server (engine in AS5) must be set";
        Utils.throwIfBlank(virtualServer.getVirtualServerName(), errMsg, "Server name");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=web/virtual-server=");
        resultScript.append(virtualServer.getVirtualServerName()).append(":add(");

        builder.addProperty("enable-welcome-root", virtualServer.getEnableWelcomeRoot());
        builder.addProperty("default-web-module", virtualServer.getDefaultWebModule());

        String aliases = "";
        if (virtualServer.getAliasName() != null) {
            StringBuilder aliasBuilder = new StringBuilder();
            for (String alias : virtualServer.getAliasName()) {
                aliasBuilder.append(", \"").append(alias).append("\"");
            }

            aliases = aliasBuilder.toString();
            aliases = aliases.replaceFirst(", ", "");

            if (!aliases.isEmpty()) {
                aliases = ", alias=[" + aliases + "]";
            }
        }

        resultScript.append(builder.asString()).append(aliases).append(")");

        return resultScript.toString();
    }

    /**
     * Creates a CLI script for adding socket-binding to AS7
     *
     * @param socketBinding object representing socket-binding
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     * 
     * @deprecated  Generate this from the ModelNode.
     */
    private static String createSocketBindingScript(SocketBindingBean socketBinding)
            throws CliScriptException {
        String errMsg = " in socket-binding must be set.";
        Utils.throwIfBlank(socketBinding.getSocketPort(), errMsg, "Port");
        Utils.throwIfBlank(socketBinding.getSocketName(), errMsg, "Name");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("/socket-binding-group=standard-sockets/socket-binding=");

        resultScript.append(socketBinding.getSocketName()).append(":add(");
        resultScript.append("port=").append(socketBinding.getSocketPort());

        builder.addProperty("interface", socketBinding.getSocketInterface());

        resultScript.append(builder.asString()).append(")");

        return resultScript.toString();
    }
    
}// class
