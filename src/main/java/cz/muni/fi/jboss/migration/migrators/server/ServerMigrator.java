package cz.muni.fi.jboss.migration.migrators.server;

import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.ex.*;
import cz.muni.fi.jboss.migration.migrators.server.jaxb.*;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.utils.Utils;
import javafx.util.Pair;
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
 *         Date: 1/24/13
 *         Time: 10:42 AM
 */

public class ServerMigrator extends AbstractMigrator{

    private Set<SocketBindingBean> socketTemp = new HashSet();

    private Set<SocketBindingBean> socketBindings = new HashSet();

    private Integer randomSocket = 1;

    private Integer randomConnector =1 ;

    public ServerMigrator(GlobalConfiguration globalConfig, List<Pair<String,String>> config){
        super(globalConfig, config);
    }

    public Set<SocketBindingBean> getSocketTemp() {
        return socketTemp;
    }

    public void setSocketTemp(Set<SocketBindingBean> socketTemp) {
        this.socketTemp = socketTemp;
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException{
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(ServerAS5Bean.class ).createUnmarshaller();

            // Or maybe use FileUtils and list all files with that name?
            File file = new File(super.getGlobalConfig().getDirAS5() + super.getGlobalConfig().getProfileAS5() +
                    File.separator + "deploy" + File.separator + "jbossweb.sar" + File.separator + "server.xml");

            if(file.canRead()){
                ServerAS5Bean serverAS5 = (ServerAS5Bean)unmarshaller.unmarshal(file);

                MigrationData mData = new MigrationData();
                for(ServiceBean s : serverAS5.getServices()){
                    mData.getConfigFragment().add(s.getEngine());
                    mData.getConfigFragment().addAll(s.getConnectorAS5s());
                }

                ctx.getMigrationData().put(ServerMigrator.class, mData);

            } else{
                 throw new LoadMigrationException("Cannot find/open file: " + file.getAbsolutePath(), new
                         FileNotFoundException());
            }
        } catch (JAXBException e) {
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
                if(((Element) subsystems.item(i)).getAttribute("xmlns").contains("web")){
                    if(!((Element) subsystems.item(i)).getAttribute("xmlns").contains("web-services")){
                        Node parent = subsystems.item(i);
                        Node lastNode = parent.getLastChild();

                        while(!(lastNode instanceof Element)){
                            lastNode = lastNode.getPreviousSibling();
                        }

                        for(Node node : generateDomElements(ctx)){
                            Node adopted = doc.adoptNode(node.cloneNode(true));
                            if(node.getNodeName().equals("virtual-server")){
                                parent.appendChild(adopted);
                            } else{
                                parent.insertBefore(adopted, lastNode);
                            }
                        }
                        break;
                    }
                }
            }
            NodeList socketGroup = doc.getElementsByTagName("socket-binding-group");
            for(int i = 0; i < socketGroup.getLength(); i++){
                if(!(socketGroup.item(i) instanceof Element)){
                    continue;
                }
                Node parent = socketGroup.item(i);
                Node lastNode = parent.getLastChild();

                while(!(lastNode instanceof Element)){
                    lastNode = lastNode.getPreviousSibling();
                }

                for(Node node : generateDomElements(ctx)){
                    if(node.getNodeName().equals("socket-binding")){
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
    public List<Node> generateDomElements(MigrationContext ctx) throws NodeGenerationException{
        try {
            JAXBContext connCtx = JAXBContext.newInstance(ConnectorAS7Bean.class);
            JAXBContext virSerCtx = JAXBContext.newInstance(VirtualServerBean.class);
            JAXBContext socketCtx = JAXBContext.newInstance(SocketBindingBean.class);

            List<Node> nodeList = new ArrayList();

            Marshaller connMarshaller = connCtx.createMarshaller();
            Marshaller virSerMarshaller = virSerCtx.createMarshaller();
            Marshaller socketMarshaller = socketCtx.createMarshaller();

            for(IConfigFragment data : ctx.getMigrationData().get(ServerMigrator.class).getConfigFragment()){
                if(data instanceof ConnectorAS5Bean){
                    ConnectorAS5Bean connector = (ConnectorAS5Bean) data;
                    ConnectorAS7Bean connAS7 = new ConnectorAS7Bean();
                    connAS7.setEnabled("true");
                    connAS7.setEnableLookups(connector.getEnableLookups());
                    connAS7.setMaxPostSize(connector.getMaxPostSize());
                    connAS7.setMaxSavePostSize(connector.getMaxSavePostSize());
                    connAS7.setProtocol(connector.getProtocol());
                    connAS7.setProxyName(connector.getProxyName());
                    connAS7.setProxyPort(connector.getProxyPort());
                    connAS7.setRedirectPort(connector.getRedirectPort());

                    // TODO: Getting error in AS7 when deploying ajp connector with empty scheme or without attribute.
                    // TODO: Only solution is http?
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

                    if(connector.getSslEnabled() != null){
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
                    Document doc = ctx.getDocBuilder().newDocument();
                    connMarshaller.marshal(connAS7,doc);
                    nodeList.add(doc.getDocumentElement());
                    continue;
                }
                if(data instanceof EngineBean){
                    EngineBean eng = (EngineBean) data;
                    VirtualServerBean virtualServer = new VirtualServerBean();
                    virtualServer.setVirtualServerName(eng.getEngineName());
                    virtualServer.setEnableWelcomeRoot("true");
                    virtualServer.setAliasName(eng.getHostNames());

                    Document doc = ctx.getDocBuilder().newDocument();
                    virSerMarshaller.marshal(virtualServer, doc);
                    nodeList.add(doc.getDocumentElement());
                    continue;
                }

                throw new NodeGenerationException("Object is not part of Server migration!");
            }

            for(SocketBindingBean sb : this.socketBindings){
                Document doc = ctx.getDocBuilder().newDocument();
                socketMarshaller.marshal(sb, doc);
                nodeList.add(doc.getDocumentElement());
                continue;
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

            Unmarshaller connUnmarshaller = JAXBContext.newInstance(ConnectorAS7Bean.class).createUnmarshaller();
            Unmarshaller virtualUnmarshaller = JAXBContext.newInstance(VirtualServerBean.class).createUnmarshaller();
            Unmarshaller socketUnmarshaller = JAXBContext.newInstance(SocketBindingBean.class).createUnmarshaller();

            for(Node node : generateDomElements(ctx)){
                if(node.getNodeName().equals("connector")){
                    ConnectorAS7Bean conn = (ConnectorAS7Bean) connUnmarshaller.unmarshal(node);
                    list.add(createConnectorScript(conn, ctx));
                    continue;
                }
                if(node.getNodeName().equals("virtual-server")){
                    VirtualServerBean virtual = (VirtualServerBean) virtualUnmarshaller.unmarshal(node);
                    list.add(createVirtualServerScript(virtual, ctx));
                    continue;
                }
                if(node.getNodeName().equals("socket-binding")){
                    SocketBindingBean socketBinding = (SocketBindingBean) socketUnmarshaller.unmarshal(node);
                    list.add(createSocketBindingScript(socketBinding, ctx));
                    continue;
                }
            }

            return list;
        } catch (NodeGenerationException | JAXBException e) {
            throw new CliScriptException(e);
        }
    }

    /**
     * Method for creating socket-bindings, which are already in fresh standalone files.
     *
     * @param ctx  migration context
     * @throws LoadMigrationException if unmarshalling socket-bindings from standalone file fails
     */
    private void createDefaultSockets(MigrationContext ctx)  throws LoadMigrationException{
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(SocketBindingBean.class ).createUnmarshaller();

            // Or maybe use FileUtils and list all files with that name?
            NodeList bindings = ctx.getStandaloneDoc().getElementsByTagName("socket-binding");
            for(int i = 0; i < bindings.getLength(); i++) {
                if(!(bindings.item(i) instanceof Element)){
                    continue;
                }
                SocketBindingBean socketBinding = (SocketBindingBean)unmarshaller.unmarshal(bindings.item(i));
                if((socketBinding.getSocketName() != null) || (socketBinding.getSocketPort() != null)){
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
     * @param name  name of the protocol which is used by connector (ajp/http/https)
     * @return name of the socket-binding so it cant be referenced in connector
     * @throws MigrationException if createDefaultSocket fails to unmarshall socket-bindings
     */
    private String createSocketBinding(String port, String name, MigrationContext ctx) throws NodeGenerationException{
        if(this.socketTemp.isEmpty()){
            try {
                createDefaultSockets(ctx);
            } catch (LoadMigrationException e) {
                throw new NodeGenerationException(e);
            }
        }

        Set<SocketBindingBean> temp = new HashSet();

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
     * @param connectorAS7 object of migrated connector
     * @param ctx  migration context
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createConnectorScript(ConnectorAS7Bean connectorAS7, MigrationContext ctx) throws CliScriptException{
        String errMsg = " in connector must be set.";
        Utils.throwIfBlank(connectorAS7.getScheme(), errMsg, "Scheme");
        Utils.throwIfBlank(connectorAS7.getSocketBinding(), errMsg, "Socket-binding");
        Utils.throwIfBlank(connectorAS7.getConnectorName(), errMsg, "Connector name");
        Utils.throwIfBlank(connectorAS7.getProtocol(), errMsg, "Protocol");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();

        String script = "/subsystem=web/connector=";
        script = script.concat(connectorAS7.getConnectorName() + ":add(");
        builder.addProperty("socket-binding", connectorAS7.getSocketBinding());
        builder.addProperty("enable-lookups", connectorAS7.getEnableLookups());
        builder.addProperty("max-post-size", connectorAS7.getMaxPostSize());
        builder.addProperty("max-save-post-size", connectorAS7.getMaxSavePostSize());
        builder.addProperty("max-connections", connectorAS7.getMaxConnections());
        builder.addProperty("protocol", connectorAS7.getProtocol());
        builder.addProperty("proxy-name", connectorAS7.getProxyName());
        builder.addProperty("proxy-port", connectorAS7.getProxyPort());
        builder.addProperty("redirect-port", connectorAS7.getRedirectPort());
        builder.addProperty("scheme", connectorAS7.getScheme());
        builder.addProperty("secure", connectorAS7.getSecure());
        builder.addProperty("enabled", connectorAS7.getEnabled());

        script = script.concat(builder.asString() + ")");

        if(connectorAS7.getScheme().equals("https"))  {
            builder.clear();

            script = script.concat("\n/subsystem=web/connector=" + connectorAS7.getConnectorName()
                    + "/ssl=configuration:add(");
            builder.addProperty("name", connectorAS7.getSslName());
            builder.addProperty("verify-client", connectorAS7.getVerifyClient());
            builder.addProperty("verify-depth", connectorAS7.getVerifyDepth());
            builder.addProperty("certificate-key-file", connectorAS7.getCertifKeyFile());
            builder.addProperty("password", connectorAS7.getPassword());
            builder.addProperty("protocol", connectorAS7.getProtocol());
            builder.addProperty("ciphers", connectorAS7.getCiphers());
            builder.addProperty("key-alias", connectorAS7.getKeyAlias());
            builder.addProperty("ca-certificate-file", connectorAS7.getCaCertifFile());
            builder.addProperty("session-cache-size", connectorAS7.getSessionCacheSize());
            builder.addProperty("session-timeout", connectorAS7.getSessionTimeout());

            script = script.concat(builder.asString() + ")");
        }
        return script;



//        String script = "/subsystem=web/connector=";
//        script = script.concat(connectorAS7.getConnectorName() + ":add(");
//        script = ctx.checkingMethod(script, "socket-binding", connectorAS7.getSocketBinding());
//        script = ctx.checkingMethod(script, ", enable-lookups", connectorAS7.getEnableLookups());
//        script = ctx.checkingMethod(script, ", max-post-size", connectorAS7.getMaxPostSize());
//        script = ctx.checkingMethod(script, ", max-save-post-size", connectorAS7.getMaxSavePostSize());
//        script = ctx.checkingMethod(script, ", max-connections", connectorAS7.getMaxConnections());
//        script = ctx.checkingMethod(script, ", protocol", connectorAS7.getProtocol());
//        script = ctx.checkingMethod(script, ", proxy-name", connectorAS7.getProxyName());
//        script = ctx.checkingMethod(script, ", proxy-port", connectorAS7.getProxyPort());
//        script = ctx.checkingMethod(script, ", redirect-port", connectorAS7.getRedirectPort());
//        script = ctx.checkingMethod(script, ", scheme", connectorAS7.getScheme());
//        script = ctx.checkingMethod(script, ", secure", connectorAS7.getSecure());
//        script = ctx.checkingMethod(script, ", enabled", connectorAS7.getEnabled());
//        script = script.concat(")");
//
//        if(connectorAS7.getScheme().equals("https"))  {
//            script = script.concat("\n/subsystem=web/connector=" + connectorAS7.getConnectorName()
//                    + "/ssl=configuration:add(");
//            script = ctx.checkingMethod(script, "name", connectorAS7.getSslName());
//            script = ctx.checkingMethod(script, ", verify-client", connectorAS7.getVerifyClient());
//            script = ctx.checkingMethod(script, ", verify-depth", connectorAS7.getVerifyDepth());
//            script = ctx.checkingMethod(script, ", certificate-key-file", connectorAS7.getCertifKeyFile());
//            script = ctx.checkingMethod(script, ", password", connectorAS7.getPassword());
//            script = ctx.checkingMethod(script, ", protocol", connectorAS7.getProtocol());
//            script = ctx.checkingMethod(script, ", ciphers", connectorAS7.getCiphers());
//            script = ctx.checkingMethod(script, ", key-alias", connectorAS7.getKeyAlias());
//            script = ctx.checkingMethod(script, ", ca-certificate-file", connectorAS7.getCaCertifFile());
//            script = ctx.checkingMethod(script, ", session-cache-size", connectorAS7.getSessionCacheSize());
//            script = ctx.checkingMethod(script, ", session-timeout", connectorAS7.getSessionTimeout());
//            script = script.concat(")");
//        }
//
//        return script;
    }

    /**
     * Creating CLI script for adding virtual-server to AS7
     *
     * @param virtualServer object representing migrated virtual-server
     * @param ctx migration context
     * @return string containing created CLI script
     */
    public static String createVirtualServerScript(VirtualServerBean virtualServer, MigrationContext ctx) {
        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        String script = "/subsystem=web/virtual-server=";
        script = script.concat(virtualServer.getVirtualServerName() + ":add(");
        builder.addProperty("enable-welcome-root", virtualServer.getEnableWelcomeRoot());
        builder.addProperty("default-web-module", virtualServer.getDefaultWebModule());

        String aliases = "";
        if(virtualServer.getAliasName() != null){
            StringBuilder aliasBuilder = new StringBuilder();
            for(String alias : virtualServer.getAliasName()){
                aliasBuilder.append(", \"" + alias + "\"");
            }

            aliases = aliasBuilder.toString();
            aliases = aliases.replaceFirst("\\, ", "");

            if(!aliases.isEmpty()){
                aliases = ", alias=[" + aliases + "]";
            }
        }


        return script + builder.asString() + aliases + ")";

//        String script = "/subsystem=web/virtual-server=";
//        script = script.concat(virtualServer.getVirtualServerName() + ":add(");
//        script = ctx.checkingMethod(script, "enable-welcome-root", virtualServer.getEnableWelcomeRoot());
//        script = ctx.checkingMethod(script, "default-web-module", virtualServer.getDefaultWebModule());
//
//        if(virtualServer.getAliasName() != null){
//            String aliases = "";
//            for(String alias : virtualServer.getAliasName()){
//                aliases = aliases.concat(", \"" + alias+"\"");
//            }
//
//            aliases = aliases.replaceFirst("\\, ", "");
//
//            if(!aliases.isEmpty()){
//                script = script.concat(", alias=[" + aliases + "]");
//            }
//        }
//
//        script = script.concat(")");
//
//        return script;
    }

    /**
     * Creating CLI script for adding socket-binding to AS7
     *
     * @param socketBinding object representing socket-binding
     * @param ctx migration context
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createSocketBindingScript(SocketBindingBean socketBinding, MigrationContext ctx)
            throws CliScriptException{
        String errMsg = " in socket-binding must be set.";
        Utils.throwIfBlank(socketBinding.getSocketPort(), errMsg, "Port");
        Utils.throwIfBlank(socketBinding.getSocketName(), errMsg, "Name");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        String script = "/socket-binding-group=standard-sockets/socket-binding=";
        script = script.concat(socketBinding.getSocketName() + ":add(");
        script = script.concat("port=" + socketBinding.getSocketPort());
        builder.addProperty("interface", socketBinding.getSocketInterface());


        return script + builder.asString() + ")";

//        String script = "/socket-binding-group=standard-sockets/socket-binding=";
//        script = script.concat(socketBinding.getSocketName() + ":add(");
//        script = script.concat("port=" + socketBinding.getSocketPort());
//        script = ctx.checkingMethod(script, ", interface", socketBinding.getSocketInterface());
//        script = script.concat(")");
//
//        return script;
    }
}
