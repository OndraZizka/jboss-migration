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
 * JAXB bean for security policy (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "policy")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "policy")

public class SecurityAS5Bean {

    @XmlElements(@XmlElement(name = "application-policy", type = ApplicationPolicyBean.class))
    private Set<ApplicationPolicyBean> applicationPolicies;

    public Set<ApplicationPolicyBean> getApplicationPolicies() {
        return applicationPolicies;
    }

    public void setApplicationPolicies(Collection<ApplicationPolicyBean> applicationPolicies) {
        Set<ApplicationPolicyBean> temp = new HashSet();
        temp.addAll(applicationPolicies);
        this.applicationPolicies = temp;
    }


}
