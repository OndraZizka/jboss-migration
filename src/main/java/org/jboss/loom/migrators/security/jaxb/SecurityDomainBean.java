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
 * JAXB bean for security-domain (AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "security-domain")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "security-domain")

public class SecurityDomainBean {

    @XmlAttribute(name = "name")
    private String securityDomainName;

    @XmlAttribute(name = "cache-type")
    private String cacheType;

    @XmlElementWrapper(name = "authentication")
    @XmlElements(@XmlElement(name = "login-module", type = LoginModuleAS7Bean.class))
    private Set<LoginModuleAS7Bean> loginModules;

    public String getSecurityDomainName() {
        return securityDomainName;
    }

    public void setSecurityDomainName(String securityDomainName) {
        this.securityDomainName = securityDomainName;
    }

    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    public Set<LoginModuleAS7Bean> getLoginModules() {
        return loginModules;
    }

    public void setLoginModules(Collection<LoginModuleAS7Bean> loginModules) {
        Set<LoginModuleAS7Bean> temp = new HashSet();
        temp.addAll(loginModules);
        this.loginModules = temp;
    }
}