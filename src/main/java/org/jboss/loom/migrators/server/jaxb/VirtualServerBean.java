package org.jboss.loom.migrators.server.jaxb;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for marshalling and representing virtual-server (AS)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "virtual-server")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "virtual-server")

public class VirtualServerBean {

    // Basic
    @XmlAttribute(name = "name")
    private String virtualServerName;

    @XmlAttribute(name = "enable-welcome-root")
    private String enableWelcomeRoot;

    @XmlAttribute(name = "default-web-module")
    private String defaultWebModule;

    @XmlPath("alias/@name")
    private Set<String> aliasName;

    public String getVirtualServerName() {
        return virtualServerName;
    }

    public void setVirtualServerName(String virtualServerName) {
        this.virtualServerName = virtualServerName;
    }

    public String getEnableWelcomeRoot() {
        return enableWelcomeRoot;
    }

    public void setEnableWelcomeRoot(String enableWelcomeRoot) {
        this.enableWelcomeRoot = enableWelcomeRoot;
    }

    public String getDefaultWebModule() {
        return defaultWebModule;
    }

    public void setDefaultWebModule(String defaultWebModule) {
        this.defaultWebModule = defaultWebModule;
    }

    public Set<String> getAliasName() {
        return aliasName;
    }

    public void setAliasName(Collection<String> aliasName) {
        Set<String> temp = new HashSet();
        temp.addAll(aliasName);
        this.aliasName = temp;
    }

}
