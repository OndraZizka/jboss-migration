package cz.muni.fi.jboss.migration.migrators.trash;

import cz.muni.fi.jboss.migration.migrators.server.jaxb.ConnectorAS7Bean;
import cz.muni.fi.jboss.migration.migrators.server.jaxb.VirtualServerBean;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for marshalling and representing web subsystem (AS7)
 *
 * @author Roman Jakubco
 * Date: 8/30/12
 * Time: 5:03 PM
 */

@XmlRootElement(name = "server")
@XmlAccessorType(XmlAccessType.NONE)
// TODO: Problem with ordering.
/*
 * when it is in this order {"virtualServers", "connectors" } => App.java generates connector and then virtual server
 * which is the format we want. But if with main.java(which is for testing) it generates virtual before connector!
 *
 * and when it is in this order {"connectors", "virtualServers"} it is exact opposite....
 */
@XmlType(name = "", propOrder = {"virtualServers", "connectors" })

public class ServerSub {

    @XmlAttribute(name = "default-virtual-server")
    private String defVirtualServer;

    @XmlAttribute(name = "native")
    private Boolean nativeAttr;

    // TODO: Elements jsp-configuration and static-resources not implemented yet

    @XmlElements(@XmlElement(name = "connector", type = ConnectorAS7Bean.class))
    private Set<ConnectorAS7Bean> connectors;

    @XmlElements(@XmlElement(name = "virtual-server", type = VirtualServerBean.class ))
    private Set<VirtualServerBean> virtualServers;

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

    public Set<ConnectorAS7Bean> getConnectors() {
        return connectors;
    }

    public void setConnectors(Collection<ConnectorAS7Bean> connectors) {
        Set<ConnectorAS7Bean> temp = new HashSet();
        temp.addAll(connectors);
        this.connectors = temp;
    }

    public Set<VirtualServerBean> getVirtualServers() {
        return virtualServers;
    }

    public void setVirtualServers(Collection<VirtualServerBean> virtualServers) {
        Set<VirtualServerBean> temp = new HashSet();
        temp.addAll(virtualServers);
        this.virtualServers = temp;
    }



}
