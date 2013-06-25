/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.security.jaxb;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.*;

/**
 * JAXB bean for login-module (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "login-module")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "login-module")

public class LoginModuleAS5Bean {
    @XmlAttribute(name = "code")
    private String loginModule;

    @XmlAttribute(name = "flag")
    private String loginModuleFlag;

    @XmlElements(@XmlElement(name = "module-option", type = ModuleOptionAS5Bean.class))
    private Set<ModuleOptionAS5Bean> moduleOptions;

    public String getLoginModule() {
        return loginModule;
    }

    public void setLoginModule(String loginModule) {
        this.loginModule = loginModule;
    }

    public Set<ModuleOptionAS5Bean> getModuleOptions() {
        return moduleOptions;
    }

    public void setModuleOptions(Collection<ModuleOptionAS5Bean> moduleOptions) {
        Set<ModuleOptionAS5Bean> temp = new HashSet();
        temp.addAll(moduleOptions);
        this.moduleOptions = temp;
    }

    public String getLoginModuleFlag() {
        return loginModuleFlag;
    }

    public void setLoginModuleFlag(String loginModuleFlag) {
        this.loginModuleFlag = loginModuleFlag;
    }
}
