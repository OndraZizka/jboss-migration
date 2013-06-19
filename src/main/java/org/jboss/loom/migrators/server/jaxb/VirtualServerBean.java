/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.server.jaxb;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * JAXB bean for virtual-server (AS)
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
