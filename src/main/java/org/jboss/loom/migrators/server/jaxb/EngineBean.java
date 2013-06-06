/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.server.jaxb;

import org.jboss.loom.spi.IConfigFragment;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jboss.loom.migrators.OriginWiseJaxbBase;

/**
 * @author Roman Jakubco
 */

@XmlRootElement(name = "Engine")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Engine")

public class EngineBean extends OriginWiseJaxbBase<EngineBean> implements IConfigFragment {

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

    @XmlPath("Host/Alias/@name")
    private Set<String> aliases;

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

    public void setHostNames(Collection<String> hostNames) {
        Set<String> temp = new HashSet();
        temp.addAll(hostNames);
        this.hostNames = temp;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public void setAliases(Collection<String> aliases) {
        Set<String> temp = new HashSet();
        temp.addAll(aliases);
        this.aliases = temp;
    }
}
