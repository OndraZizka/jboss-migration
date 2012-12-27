package cz.muni.fi.jboss.migration.server;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * 
 * @author  Roman Jakubco
 * Date: 10/2/12
 * Time: 9:12 PM
 */
@XmlRootElement(name = "Service")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Service")
public class Service{
    @XmlAttribute(name = "name")
    private String serviceName;
    @XmlElements(@XmlElement(name = "Connector",type = ConnectorAS5.class))
    private Collection<ConnectorAS5> connectorAS5s;
    @XmlPath("/Engine/@name")
    private String engineName;
    @XmlPath("/Engine/@defaultHost")
    private String defaultHost;
    @XmlPath("Engine/@className")
    private String engineClassName;
    @XmlPath("Engine/@backgroundProcessorDelay")
    private Integer backgroundProcessorDelay;
    @XmlPath("Engine/@jvmRoute")
    private String jvmRoute;
    @XmlPath("Engine/Realm/@className")
    private  String realmClassName;
    @XmlPath("Engine/Realm/@certificatePrincipal")
    private String certificatePrincipal;
    @XmlPath("Engine/Realm/@allRolesMode")
    private String allRolesMode;

    @XmlPath("Engine/Host/@name")
    private Collection<String > hostNames;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Collection<ConnectorAS5> getConnectorAS5s() {
        return connectorAS5s;
    }

    public void setConnectorAS5s(Collection<ConnectorAS5> connectorAS5s) {
        this.connectorAS5s = connectorAS5s;
    }

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

    public Collection<String> getHostNames() {
        return hostNames;
    }

    public void setHostNames(Collection<String> hostNames) {
        this.hostNames = hostNames;
    }

    @Override
    public String toString() {
        return "Service{" +
                "serviceName='" + serviceName + '\'' +
                ", connectorAS5s=" + connectorAS5s +
                ", engineName='" + engineName + '\'' +
                ", defaultHost='" + defaultHost + '\'' +
                ", engineClassName='" + engineClassName + '\'' +
                ", backgroundProcessorDelay=" + backgroundProcessorDelay +
                ", jvmRoute='" + jvmRoute + '\'' +
                ", realmClassName='" + realmClassName + '\'' +
                ", certificatePrincipal='" + certificatePrincipal + '\'' +
                ", allRolesMode='" + allRolesMode + '\'' +
                ", hostNames=" + hostNames +
                '}';
    }
}