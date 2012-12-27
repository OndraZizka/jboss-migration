package cz.fi.muni.jboss.migration.server;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * 
 * @author  Roman Jakubco
 * Date: 8/30/12
 * Time: 5:03 PM
 */


@XmlRootElement(name = "subsystem")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "subsystem")
public class ServerSub {
    @XmlAttribute(name = "xmlns")
    private String xmlns;
    @XmlAttribute(name = "default-virtual-server")
    private String defaultVirtualServer;
    @XmlAttribute(name = "native")
    private Boolean nativeAttr;
    //TODO:elements jsp-configuration and static-resources not implemented yet

    @XmlElements(@XmlElement(name = "connector", type =ConnectorAS7.class))
    private Collection<ConnectorAS7> connectors;
    @XmlElements(@XmlElement(name = "virtual-server", type =VirtualServer.class ))
    private Collection<VirtualServer> virtualServers;

    public String getXmlns() {
        return xmlns;
    }

    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    public String getDefaultVirtualServer() {
        return defaultVirtualServer;
    }

    public void setDefaultVirtualServer(String defaultVirtualServer) {
        this.defaultVirtualServer = defaultVirtualServer;
    }

    public Boolean getNativeAttr() {
        return nativeAttr;
    }

    public void setNativeAttr(Boolean nativeAttr) {
        this.nativeAttr = nativeAttr;
    }

    public Collection<ConnectorAS7> getConnectors() {
        return connectors;
    }

    public void setConnectors(Collection<ConnectorAS7> connectors) {
        this.connectors = connectors;
    }

    public Collection<VirtualServer> getVirtualServers() {
        return virtualServers;
    }

    public void setVirtualServers(Collection<VirtualServer> virtualServers) {
        this.virtualServers = virtualServers;
    }



}
