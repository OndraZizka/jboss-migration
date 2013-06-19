/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.logging.jaxb;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * JAXB bean for custom-handler (AS7)
 *
 * @author Roman Jakubco
 */
@XmlRootElement(name = "custom-handler")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "custom-handler")
public class CustomHandlerBean extends HandlerBeanBase {

    @XmlPath("encoding/@value")
    private String encoding;

    @XmlPath("level/@name")
    private String level;

    @XmlPath("filter/@value")
    private String filter;

    @XmlPath("formatter/pattern-formatter/@pattern")
    private String formatter;

    @XmlAttribute(name = "class")
    private String classValue;

    @XmlAttribute(name = "module")
    private String module;

    @XmlElementWrapper(name = "properties")
    @XmlElements(@XmlElement(name = "property", type = PropertyBean.class))
    private Set<PropertyBean> properties;


    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getFilter() { return filter; }
    public void setFilter(String filter) { this.filter = filter; }
    public String getFormatter() { return formatter; }
    public void setFormatter(String formatter) { this.formatter = formatter; }
    public String getClassValue() { return classValue; }
    public void setClassValue(String classValue) { this.classValue = classValue; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public Set<PropertyBean> getProperties() { return properties; }
    public void setProperties(Collection<PropertyBean> properties) {
        Set<PropertyBean> temp = new HashSet();
        temp.addAll(properties);
        this.properties = temp;
    }
    public String getEncoding() { return encoding; }
    public void setEncoding(String encoding) { this.encoding = encoding; }
    
}// class