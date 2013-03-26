package cz.muni.fi.jboss.migration.migrators.server;

import cz.muni.fi.jboss.migration.conf.GlobalConfiguration;
import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.ex.ApplyMigrationException;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.NodeGenerationException;
import cz.muni.fi.jboss.migration.migrators.server.jaxb.*;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.collections.map.MultiValueMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Migrator of server subsystem implementing IMigrator.
 *
 * @author Roman Jakubco
 */

public class ServerMigrator extends AbstractMigrator {
    
    @Override protected String getConfigPropertyModuleName() { return "server"; }
    

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
        
        if( ! file.canRead() )
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
        }
        catch (JAXBException e) {
            throw new LoadMigrationException("Failed parsing logging config file: " + file.getPath(), e);
        }
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
                if (((Element) subsystems.item(i)).getAttribute("xmlns").contains("web")) {
                    if (!((Element) subsystems.item(i)).getAttribute("xmlns").contains("web-services")) {
                        Node parent = subsystems.item(i);
                        Node lastNode = parent.getLastChild();

                        while (!(lastNode instanceof Element)) {
                            lastNode = lastNode.getPreviousSibling();
                        }

                        for (Node node : generateDomElements(ctx)) {
                            Node adopted = doc.adoptNode(node.cloneNode(true));
                            if (node.getNodeName().equals("virtual-server")) {
                                parent.appendChild(adopted);
                            } else {
                                parent.insertBefore(adopted, lastNode);
                            }
                        }
                        break;
                    }
                }
            }
            NodeList socketGroup = doc.getElementsByTagName("socket-binding-group");
            for (int i = 0; i < socketGroup.getLength(); i++) {
                if (!(socketGroup.item(i) instanceof Element)) {
                    continue;
                }
                Node parent = socketGroup.item(i);
                Node lastNode = parent.getLastChild();

                while (!(lastNode instanceof Element)) {
                    lastNode = lastNode.getPreviousSibling();
                }

                for (Node node : generateDomElements(ctx)) {
                    if (node.getNodeName().equals("socket-binding")) {
                        Node adopted = doc.adoptNode(node.cloneNode(true));
                        parent.insertBefore(adopted, lastNode);
                    }
                }
                break;
            }
        } catch (NodeGenerationException e) {
            throw new ApplyMigrationException(e);
        }
    }

    @Override
    public List<Node> generateDomElements(MigrationContext ctx) throws NodeGenerationException {
        try {
            JAXBContext connCtx = JAXBContext.newInstance(ConnectorAS7Bean.class);
            JAXBContext virSerCtx = JAXBContext.newInstance(VirtualServerBean.class);
            JAXBContext socketCtx = JAXBContext.newInstance(SocketBindingBean.class);

            List<Node> nodeList = new ArrayList();

            Marshaller connMarshaller = connCtx.createMarshaller();
            Marshaller virSerMarshaller = virSerCtx.createMarshaller();
            Marshaller socketMarshaller = socketCtx.createMarshaller();

            for (IConfigFragment fragment : ctx.getMigrationData().get(ServerMigrator.class).getConfigFragments()) {
                Document doc = Utils.createXmlDocumentBuilder().newDocument();
                if (fragment instanceof ConnectorAS5Bean) {
                    connMarshaller.marshal(connectorMigration((ConnectorAS5Bean) fragment, ctx), doc);
                    nodeList.add(doc.getDocumentElement());
                    continue;
                }
                if (fragment instanceof EngineBean) {
                    virSerMarshaller.marshal(engineMigration((EngineBean) fragment), doc);
                    nodeList.add(doc.getDocumentElement());
                    continue;
                }

                throw new NodeGenerationException("Object is not part of Server migration!");
            }

            for (SocketBindingBean sb : this.socketBindings) {
                Document doc = Utils.createXmlDocumentBuilder().newDocument();
                socketMarshaller.marshal(sb, doc);
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

            Unmarshaller connUnmarshaller = JAXBContext.newInstance(ConnectorAS7Bean.class).createUnmarshaller();
            Unmarshaller virtualUnmarshaller = JAXBContext.newInstance(VirtualServerBean.class).createUnmarshaller();
            Unmarshaller socketUnmarshaller = JAXBContext.newInstance(SocketBindingBean.class).createUnmarshaller();

            for (Node node : generateDomElements(ctx)) {
                if (node.getNodeName().equals("connector")) {
                    ConnectorAS7Bean conn = (ConnectorAS7Bean) connUnmarshaller.unmarshal(node);
                    list.add(createConnectorScript(conn));
                    continue;
                }
                if (node.getNodeName().equals("virtual-server")) {
                    VirtualServerBean virtual = (VirtualServerBean) virtualUnmarshaller.unmarshal(node);
                    list.add(createVirtualServerScript(virtual));
                    continue;
                }
                if (node.getNodeName().equals("socket-binding")) {
                    SocketBindingBean socketBinding = (SocketBindingBean) socketUnmarshaller.unmarshal(node);
                    list.add(createSocketBindingScript(socketBinding));
                }
            }

            return list;
        } catch (NodeGenerationException | JAXBException e) {
            throw new CliScriptException(e);
        }
    }

    /**
     * Method for migration of connector from AS5 to AS7
     *
     * @param connector object representing connector in AS5
     * @param ctx  migration context
     * @return  migrated AS7's connector
     * @throws NodeGenerationException
     *         if socket-binding cannot be created or set
     */
    public ConnectorAS7Bean connectorMigration(ConnectorAS5Bean connector, MigrationContext ctx)
            throws NodeGenerationException{
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

        connAS7.setConnectorName("connector" + this.randomConnector);
        this.randomConnector++;

        // Socket-binding.. first try
        if (connector.getProtocol().equals("HTTP/1.1")) {

            if (connector.getSslEnabled() == null) {
                connAS7.setSocketBinding(createSocketBinding(connector.getPort(), "http", ctx));
            } else {
                if (connector.getSslEnabled().equals("true")) {
                    connAS7.setSocketBinding(createSocketBinding(connector.getPort(), "https", ctx));
                } else {
                    connAS7.setSocketBinding(createSocketBinding(connector.getPort(), "http", ctx));
                }
            }
        } else {
            connAS7.setSocketBinding(createSocketBinding(connector.getPort(), "ajp", ctx));
        }

        if (connector.getSslEnabled() != null) {
            if (connector.getSslEnabled().equals("true")) {
                connAS7.setScheme("https");
                connAS7.setSecure(connector.getSecure());

                connAS7.setSslName("ssl");
                connAS7.setVerifyClient(connector.getClientAuth());
                // TODO: Problem with place of the file
                connAS7.setCertifKeyFile(connector.getKeystoreFile());

                // TODO: No sure which protocols can be in AS5. Hard to find..
                if ((connector.getSslProtocol().equals("TLS")) || (connector.getSslProtocol() == null)) {
                    connAS7.setSslProtocol("TLSv1");
                }
                connAS7.setSslProtocol(connector.getSslProtocol());

                connAS7.setCiphers(connector.getCiphers());
                connAS7.setKeyAlias(connAS7.getKeyAlias());

                // TODO: Problem with passwords. Password in AS7 stores keystorePass and truststorePass(there are same)
                connAS7.setPassword(connector.getKeystorePass());
            }
        }

        return connAS7;
    }

    /**
     * Method for migration of Engine rom AS5 to AS7
     *
     * @param engine object representing Engine
     * @return  created virtual-server
     */
    public static VirtualServerBean engineMigration(EngineBean engine){
        VirtualServerBean virtualServer = new VirtualServerBean();
        virtualServer.setVirtualServerName(engine.getEngineName());
        virtualServer.setEnableWelcomeRoot("true");
        virtualServer.setAliasName(engine.getHostNames());

        if(engine.getAliases() != null){
            virtualServer.getAliasName().addAll(engine.getAliases());
        }

        return virtualServer;
    }

    /**
     * Method for creating socket-bindings, which are already in fresh standalone files.
     *
     * @param ctx migration context
     * @throws LoadMigrationException if unmarshalling socket-bindings from standalone file fails
     */
    private void createDefaultSockets(MigrationContext ctx) throws LoadMigrationException {
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(SocketBindingBean.class).createUnmarshaller();

            // Or maybe use FileUtils and list all files with that name?
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
            throw new LoadMigrationException(e);
        }

    }

    /**
     * Method for creating socket-binding if it doesn't already exists.
     *
     * @param port port of the connector, which will be converted to socket-binding
     * @param name name of the protocol which is used by connector (ajp/http/https)
     * @return name of the socket-binding so it cant be referenced in connector
     * @throws NodeGenerationException if createDefaultSocket fails to unmarshall socket-bindings
     */
    private String createSocketBinding(String port, String name, MigrationContext ctx) throws NodeGenerationException {
        if (this.socketTemp.isEmpty()) {
            try {
                createDefaultSockets(ctx);
            } catch (LoadMigrationException e) {
                throw new NodeGenerationException(e);
            }
        }

        for (SocketBindingBean sb : this.socketTemp) {
            if (sb.getSocketPort().equals(port)) {
                return sb.getSocketName();
            }
        }
        SocketBindingBean socketBinding = new SocketBindingBean();
        if (this.socketBindings == null) {
            this.socketBindings = new HashSet();
            socketBinding.setSocketName(name);
            socketBinding.setSocketPort(port);
            this.socketBindings.add(socketBinding);
            return name;
        }

        for (SocketBindingBean sb : this.socketBindings) {
            if (sb.getSocketPort().equals(port)) {
                return sb.getSocketName();
            }
        }

        socketBinding.setSocketPort(port);

        for (SocketBindingBean sb : this.socketBindings) {
            if (sb.getSocketName().equals(name)) {
                name = name.concat(this.randomSocket.toString());
                this.randomSocket++;
            }
        }

        socketBinding.setSocketName(name);
        this.socketBindings.add(socketBinding);

        return name;
    }

    /**
     * Creating CLI script for adding connector to AS7 from migrated connector.
     *
     * @param connAS7 object of migrated connector
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createConnectorScript(ConnectorAS7Bean connAS7) throws CliScriptException {
        String errMsg = " in connector must be set.";
        Utils.throwIfBlank(connAS7.getScheme(), errMsg, "Scheme");
        Utils.throwIfBlank(connAS7.getSocketBinding(), errMsg, "Socket-binding");
        Utils.throwIfBlank(connAS7.getConnectorName(), errMsg, "Connector name");
        Utils.throwIfBlank(connAS7.getProtocol(), errMsg, "Protocol");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
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

        if (connAS7.getScheme().equals("https")) {
            resultScript.append("\n/subsystem=web/connector=").append(connAS7.getConnectorName());
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
        }

        return resultScript.toString();
    }

    /**
     * Creating CLI script for adding virtual-server to AS7
     *
     * @param virtualServer object representing migrated virtual-server
     * @return string containing created CLI script
     */
    public static String createVirtualServerScript(VirtualServerBean virtualServer) throws CliScriptException{
        String errMsg = "in virtual-server (engine in AS5) must be set";
        Utils.throwIfBlank(virtualServer.getVirtualServerName(), errMsg, "Server name");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
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
     * Creating CLI script for adding socket-binding to AS7
     *
     * @param socketBinding object representing socket-binding
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createSocketBindingScript(SocketBindingBean socketBinding)
            throws CliScriptException {
        String errMsg = " in socket-binding must be set.";
        Utils.throwIfBlank(socketBinding.getSocketPort(), errMsg, "Port");
        Utils.throwIfBlank(socketBinding.getSocketName(), errMsg, "Name");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        StringBuilder resultScript = new StringBuilder("/socket-binding-group=standard-sockets/socket-binding=");

        resultScript.append(socketBinding.getSocketName()).append(":add(");
        resultScript.append("port=").append(socketBinding.getSocketPort());

        builder.addProperty("interface", socketBinding.getSocketInterface());

        resultScript.append(builder.asString()).append(")");

        return resultScript.toString();
    }
}
