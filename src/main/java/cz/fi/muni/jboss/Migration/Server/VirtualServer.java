package cz.fi.muni.jboss.Migration.Server;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 10/2/12
 * Time: 9:14 PM
 */
@XmlRootElement(name = "virtual-server")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "virtual-server")
public class VirtualServer{
    //basic
    @XmlAttribute(name = "name")
    private String virtualServerName;
    @XmlAttribute(name = "enable-welcome-root")
    private Boolean enableWelcomeRoot;
    @XmlAttribute(name = "default-web-module")
    private String defaultWebModule;
    @XmlPath("alias/@name")
    private Collection<String> aliasName;

    public String getVirtualServerName() {
        return virtualServerName;
    }

    public void setVirtualServerName(String virtualServerName) {
        this.virtualServerName = virtualServerName;
    }

    public Boolean getEnableWelcomeRoot() {
        return enableWelcomeRoot;
    }

    public void setEnableWelcomeRoot(Boolean enableWelcomeRoot) {
        this.enableWelcomeRoot = enableWelcomeRoot;
    }

    public String getDefaultWebModule() {
        return defaultWebModule;
    }

    public void setDefaultWebModule(String defaultWebModule) {
        this.defaultWebModule = defaultWebModule;
    }

    public Collection<String> getAliasName() {
        return aliasName;
    }

    public void setAliasName(Collection<String> aliasName) {
        this.aliasName = aliasName;
    }

}
