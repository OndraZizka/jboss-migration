package cz.muni.fi.jboss.Migration.Server;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: Roman Jakubco
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
    private String defVirtualServer;
    @XmlAttribute(name = "native")
    private Boolean nativeAttr;
    // TODO:elements jsp-configuration and static-resources not implemented yet

    @XmlElements(@XmlElement(name = "connector", type =ConnectorAS7.class))
    private Set<ConnectorAS7> connectors;
    @XmlElements(@XmlElement(name = "virtual-server", type =VirtualServer.class ))
    private Set<VirtualServer> virtualServers;

    public String getXmlns() {
        return xmlns;
    }

    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    public String getDefVirtualServer() {
        return defVirtualServer;
    }

    public void setDefVirtualServer(String defVirtualServer) {
        this.defVirtualServer = defVirtualServer;
    }

    public Boolean getNativeAttr() {
        return nativeAttr;
    }

    public void setNativeAttr(Boolean nativeAttr) {
        this.nativeAttr = nativeAttr;
    }

    public Set<ConnectorAS7> getConnectors() {
        return connectors;
    }

    public void setConnectors(Collection<ConnectorAS7> connectors) {
        Set<ConnectorAS7> temp = new HashSet();
        temp.addAll(connectors);
        this.connectors = temp;
    }

    public Set<VirtualServer> getVirtualServers() {
        return virtualServers;
    }

    public void setVirtualServers(Collection<VirtualServer> virtualServers) {
        Set<VirtualServer> temp = new HashSet();
        temp.addAll(virtualServers);
        this.virtualServers = temp;
    }



}
