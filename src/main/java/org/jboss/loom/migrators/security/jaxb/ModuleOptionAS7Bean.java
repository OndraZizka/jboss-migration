/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.security.jaxb;

import javax.xml.bind.annotation.*;

/**
 * Class for marshalling and representing module-option (AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "module-option")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "module-option")

public class ModuleOptionAS7Bean {

    @XmlAttribute(name = "name")
    private String moduleOptionName;

    @XmlAttribute(name = "value")
    private String moduleOptionValue;


    public ModuleOptionAS7Bean() {
    }


    public ModuleOptionAS7Bean( String moduleOptionName, String moduleOptionValue ) {
        this.moduleOptionName = moduleOptionName;
        this.moduleOptionValue = moduleOptionValue;
    }

    
    
    
    public String getModuleOptionName() {
        return moduleOptionName;
    }

    public void setModuleOptionName(String moduleOptionName) {
        this.moduleOptionName = moduleOptionName;
    }

    public String getModuleOptionValue() {
        return moduleOptionValue;
    }

    public void setModuleOptionValue(String moduleOptionValue) {
        this.moduleOptionValue = moduleOptionValue;
    }
}