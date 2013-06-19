/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.security.jaxb;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * JAXB bean for login-module (AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "login-module")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "login-module")

public class LoginModuleAS7Bean {

    @XmlAttribute(name = "code")
    private String loginModuleCode;

    @XmlAttribute(name = "flag")
    private String loginModuleFlag;

    @XmlAttribute(name = "module")
    private String module;

    @XmlElements(@XmlElement(name = "module-option", type = ModuleOptionAS7Bean.class))
    private Set<ModuleOptionAS7Bean> moduleOptions;

    public String getLoginModuleCode() {
        return loginModuleCode;
    }

    public void setLoginModuleCode(String loginModuleCode) {
        this.loginModuleCode = loginModuleCode;
    }

    public String getLoginModuleFlag() {
        return loginModuleFlag;
    }

    public void setLoginModuleFlag(String loginModuleFlag) {
        this.loginModuleFlag = loginModuleFlag;
    }

    public Set<ModuleOptionAS7Bean> getModuleOptions() {
        return moduleOptions;
    }

    public void setModuleOptions(Collection<ModuleOptionAS7Bean> moduleOptions) {
        Set<ModuleOptionAS7Bean> temp = new HashSet();
        temp.addAll(moduleOptions);
        this.moduleOptions = temp;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public void setModuleOptions(Set<ModuleOptionAS7Bean> moduleOptions) {
        this.moduleOptions = moduleOptions;
    }
}

