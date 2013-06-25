/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.logging.jaxb;

import javax.xml.bind.annotation.*;
import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * JAXB bean for periodic-rotating-file-handler (AS7)
 *
 * @author Roman Jakubco
 */
@XmlRootElement(name = "periodic-rotating-file-handler")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "periodic-rotating-file-handler")
public class PerRotFileHandlerBean extends HandlerBeanBase{

    @XmlPath("encoding/@value")
    private String encoding;

    @XmlPath("level/@name")
    private String level;

    @XmlPath("filter/@value")
    private String filter;

    @XmlPath("formatter/pattern-formatter/@pattern")
    private String formatter;

    @XmlAttribute(name = "autoflush")
    private String autoflush;

    @XmlPath("append/@value")
    private String append;

    @XmlPath("file/@relative-to")
    private String relativeTo;

    @XmlPath("file/@path")
    private String path;

    @XmlPath("suffix/@value")
    private String suffix;

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getFilter() { return filter; }
    public void setFilter(String filter) { this.filter = filter; }
    public String getFormatter() { return formatter; }
    public void setFormatter(String formatter) { this.formatter = formatter; }
    public String getAutoflush() { return autoflush; }
    public void setAutoflush(String autoflush) { this.autoflush = autoflush; }
    public String getAppend() { return append; }
    public void setAppend(String append) { this.append = append; }
    public String getRelativeTo() { return relativeTo; }
    public void setRelativeTo(String relativeTo) { this.relativeTo = relativeTo; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public String getEncoding() { return encoding; }
    public void setEncoding(String encoding) { this.encoding = encoding; }

}// class
