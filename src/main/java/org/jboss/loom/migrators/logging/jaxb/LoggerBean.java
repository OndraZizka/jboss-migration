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
 * Class for marshalling and representing logger (AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "logger")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "logger")

public class LoggerBean {
    @XmlAttribute(name = "category")
    private String loggerCategory;

    @XmlPath("level/@name")
    private String loggerLevelName;

    @XmlAttribute(name = "use-parent-handlers")
    private String useParentHandlers;

    @XmlPath("handlers/handler/@name")
    private Set<String> handlers;

    public String getLoggerCategory() {
        return loggerCategory;
    }

    public void setLoggerCategory(String loggerCategory) {
        this.loggerCategory = loggerCategory;
    }

    public String getLoggerLevelName() {
        return loggerLevelName;
    }

    public void setLoggerLevelName(String loggerLevelName) {
        this.loggerLevelName = loggerLevelName;
    }

    public String getUseParentHandlers() {
        return useParentHandlers;
    }

    public void setUseParentHandlers(String useParentHandlers) {
        this.useParentHandlers = useParentHandlers;
    }

    public Set<String> getHandlers() {
        return handlers;
    }

    public void setHandlers(Collection<String> handlers) {
        Set<String> temp = new HashSet();
        temp.addAll(handlers);
        this.handlers = temp;
    }
}
