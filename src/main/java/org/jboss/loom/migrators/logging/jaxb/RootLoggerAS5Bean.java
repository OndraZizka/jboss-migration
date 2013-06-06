/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.logging.jaxb;

import org.jboss.loom.spi.IConfigFragment;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Set;
import org.jboss.loom.migrators.OriginWiseJaxbBase;

/**
 * @author Roman Jakubco
 */

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "root")

public class RootLoggerAS5Bean extends OriginWiseJaxbBase<RootLoggerAS5Bean> implements IConfigFragment {

    @XmlPath("priority/@value")
    private String rootPriorityValue;

    @XmlPath("appender-ref/@ref")
    private Set<String> rootAppenderRefs;

    public String getRootPriorityValue() {
        return rootPriorityValue;
    }

    public void setRootPriorityValue(String rootPriorityValue) {
        this.rootPriorityValue = rootPriorityValue;
    }

    public Set<String> getRootAppenderRefs() {
        return rootAppenderRefs;
    }

    public void setRootAppenderRefs(Set<String> rootAppenderRefs) {
        this.rootAppenderRefs = rootAppenderRefs;
    }
}
