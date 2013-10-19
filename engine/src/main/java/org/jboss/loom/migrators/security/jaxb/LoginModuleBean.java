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
public class LoginModuleBean {
    
    @XmlAttribute(name = "code")
    private String code;

    @XmlAttribute(name = "flag")
    private String flag;

    @XmlElements(@XmlElement(name = "module-option", type = LoginModuleOptionBean.class))
    private Set<LoginModuleOptionBean> options;
    
    

    // Get/set
    public String getCode() { return code; }
    public void setCode( String code ) { this.code = code; }
    public String getFlag() { return flag; }
    public void setFlag( String flag ) { this.flag = flag; }
    
    public Set<LoginModuleOptionBean> getOptions() { return options; }
    public void setOptions(Collection<LoginModuleOptionBean> options) {
        Set<LoginModuleOptionBean> temp = new HashSet();
        temp.addAll(options);
        this.options = temp;
    }
    
}// class
