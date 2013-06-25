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
import org.jboss.loom.migrators.OriginWiseJaxbBase;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;

/**
 * JAXB bean for application-policy (AS5)
 *
 * @author Roman Jakubco
 */
@ConfigPartDescriptor(
    name = "Application policy ${applicationPolicyName}"
)
@XmlRootElement(name = "application-policy")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "application-policy")

public class ApplicationPolicyBean extends OriginWiseJaxbBase<ApplicationPolicyBean> implements IConfigFragment {

    @XmlAttribute(name = "name")
    private String applicationPolicyName;

    @XmlElementWrapper(name = "authentication")
    @XmlElements(@XmlElement(name = "login-module", type = LoginModuleAS5Bean.class))
    private Set<LoginModuleAS5Bean> loginModules;

    public String getApplicationPolicyName() {
        return applicationPolicyName;
    }

    public void setApplicationPolicyName(String applicationPolicyName) {
        this.applicationPolicyName = applicationPolicyName;
    }

    public Set<LoginModuleAS5Bean> getLoginModules() {
        return loginModules;
    }

    public void setLoginModules(Collection<LoginModuleAS5Bean> loginModules) {
        Set<LoginModuleAS5Bean> temp = new HashSet();
        temp.addAll(loginModules);
        this.loginModules = temp;
    }
}

