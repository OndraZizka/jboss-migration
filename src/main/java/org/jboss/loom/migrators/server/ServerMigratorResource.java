package org.jboss.loom.migrators.server;

import org.jboss.loom.migrators.server.jaxb.SocketBindingBean;

import java.util.HashSet;
import java.util.Set;

/**
 * User: Roman Jakubco
 * Date: 4/29/13
 */
public class ServerMigratorResource {
    private Set<SocketBindingBean> socketTemp = new HashSet();

    private Set<SocketBindingBean> socketBindings = new HashSet();

    private Set<String> keystores = new HashSet();

    private Integer randomSocket = 1;

    private Integer randomConnector = 1;

    public Integer getRandomConnector() {
        return randomConnector++;
    }

    public Integer getRandomSocket() {
        return randomSocket++;
    }

    public Set<SocketBindingBean> getSocketBindings() {
        return socketBindings;
    }

    public void setSocketBindings(Set<SocketBindingBean> socketBindings) {
        this.socketBindings = socketBindings;
    }

    public Set<SocketBindingBean> getSocketTemp() {
        return socketTemp;
    }

    public void setSocketTemp(Set<SocketBindingBean> socketTemp) {
        this.socketTemp = socketTemp;
    }

    public Set<String> getKeystores() {
        return keystores;
    }
}
