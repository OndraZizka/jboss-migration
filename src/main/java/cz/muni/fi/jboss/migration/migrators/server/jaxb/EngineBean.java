package cz.muni.fi.jboss.migration.migrators.server.jaxb;

import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Set;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 6:21 PM
 */

@XmlRootElement(name = "Engine")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Engine")

public class EngineBean implements IConfigFragment {

    @XmlAttribute(name = "name")
    private String engineName;

    @XmlAttribute(name = "defaultHost")
    private String defaultHost;

    @XmlAttribute(name = "className")
    private String engineClassName;

    @XmlAttribute(name = "backgroundProcessorDelay")
    private Integer backgroundProcessorDelay;

    @XmlAttribute(name = "jvmRoute")
    private String jvmRoute;

    @XmlPath("Realm/@className")
    private String realmClassName;

    @XmlPath("Realm/@certificatePrincipal")
    private String certificatePrincipal;

    @XmlPath("Realm/@allRolesMode")
    private String allRolesMode;

    @XmlPath("Host/@name")
    private Set<String> hostNames;

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public String getDefaultHost() {
        return defaultHost;
    }

    public void setDefaultHost(String defaultHost) {
        this.defaultHost = defaultHost;
    }

    public String getEngineClassName() {
        return engineClassName;
    }

    public void setEngineClassName(String engineClassName) {
        this.engineClassName = engineClassName;
    }

    public Integer getBackgroundProcessorDelay() {
        return backgroundProcessorDelay;
    }

    public void setBackgroundProcessorDelay(Integer backgroundProcessorDelay) {
        this.backgroundProcessorDelay = backgroundProcessorDelay;
    }

    public String getJvmRoute() {
        return jvmRoute;
    }

    public void setJvmRoute(String jvmRoute) {
        this.jvmRoute = jvmRoute;
    }

    public String getRealmClassName() {
        return realmClassName;
    }

    public void setRealmClassName(String realmClassName) {
        this.realmClassName = realmClassName;
    }

    public String getCertificatePrincipal() {
        return certificatePrincipal;
    }

    public void setCertificatePrincipal(String certificatePrincipal) {
        this.certificatePrincipal = certificatePrincipal;
    }

    public String getAllRolesMode() {
        return allRolesMode;
    }

    public void setAllRolesMode(String allRolesMode) {
        this.allRolesMode = allRolesMode;
    }

    public Set<String> getHostNames() {
        return hostNames;
    }

    public void setHostNames(Set<String> hostNames) {
        this.hostNames = hostNames;
    }
}
