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

/**
 * Class for marshalling and representing file-handler (AS7)
 *
 * @author Roman Jakubco
 */
@XmlRootElement(name = "file-handler")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "file-handler")
public class FileHandlerBean extends HandlerBeanBase {

    @XmlPath("level/@name")
    private String level;

    @XmlPath("filter/@value")
    private String filter;

    @XmlPath("formatter/pattern-formatter/@pattern")
    private String formatter;

    @XmlAttribute(name = "autoflush")
    private Boolean autoflush;

    @XmlPath("append/@value")
    private Boolean append;

    @XmlPath("file/@relative-to")
    private String file;

    @XmlPath("file/@path")
    private String path;

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getFilter() { return filter; }
    public void setFilter(String filter) { this.filter = filter; }
    public String getFormatter() { return formatter; }
    public void setFormatter(String formatter) { this.formatter = formatter; }
    public Boolean getAutoflush() { return autoflush; }
    public void setAutoflush(Boolean autoflush) { this.autoflush = autoflush; }
    public Boolean getAppend() { return append; }
    public void setAppend(Boolean append) { this.append = append; }
    public String getFile() { return file; }
    public void setFile(String file) { this.file = file; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

}// class
