/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.logging.jaxb;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * @author Roman Jakubco
 */

@XmlRootElement(name = "root-logger")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "root-logger")

public class RootLoggerAS7Bean {

    @XmlPath("level/@name")
    private String rootLoggerLevel;

    @XmlPath("handlers/handler/@name")
    private Set<String> rootLoggerHandlers;

    @XmlPath("filter/@value")
    private String rootLogFilValue;

    public String getRootLoggerLevel() {
        return rootLoggerLevel;
    }

    public void setRootLoggerLevel(String rootLoggerLevel) {
        this.rootLoggerLevel = rootLoggerLevel;
    }

    public Set<String> getRootLoggerHandlers() {
        return rootLoggerHandlers;
    }

    public void setRootLoggerHandlers(Set<String> rootLoggerHandlers) {
        this.rootLoggerHandlers = rootLoggerHandlers;
    }

    public String getRootLogFilValue() {
        return rootLogFilValue;
    }

    public void setRootLogFilValue(String rootLogFilValue) {
        this.rootLogFilValue = rootLogFilValue;
    }
}
