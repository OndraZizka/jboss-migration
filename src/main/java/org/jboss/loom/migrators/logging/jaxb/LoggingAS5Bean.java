/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.logging.jaxb;


import org.jboss.loom.spi.IConfigFragment;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jboss.loom.migrators.OriginWiseJaxbBase;

/**
 * JAXB bean for logging configuration (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "configuration", namespace = "http://jakarta.apache.org/log4j/")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "configuration")

public class LoggingAS5Bean extends OriginWiseJaxbBase<LoggingAS5Bean> implements IConfigFragment {

    @XmlElements(@XmlElement(name = "appender", type = AppenderBean.class))
    Set<AppenderBean> appenders;

    @XmlElements(@XmlElement(name = "category", type = CategoryBean.class))
    private Set<CategoryBean> categories;

    @XmlElements(@XmlElement(name = "logger", type = CategoryBean.class))
    private Set<CategoryBean> loggers;

    @XmlElement(name = "root")
    private RootLoggerAS5Bean rootLoggerAS5;

    public Set<AppenderBean> getAppenders() {
        return appenders;
    }

    public void setAppenders(Collection<AppenderBean> appenders) {
        Set<AppenderBean> temp = new HashSet();
        temp.addAll(appenders);
        this.appenders = temp;
    }

    public Set<CategoryBean> getCategories() {
        return categories;
    }

    public void setCategories(Collection<CategoryBean> categories) {
        Set<CategoryBean> temp = new HashSet();
        temp.addAll(categories);
        this.categories = temp;
    }

    public RootLoggerAS5Bean getRootLoggerAS5() {
        return rootLoggerAS5;
    }

    public void setRootLoggerAS5(RootLoggerAS5Bean rootLoggerAS5) {
        this.rootLoggerAS5 = rootLoggerAS5;
    }

    public Set<CategoryBean> getLoggers() {
        return loggers;
    }

    public void setLoggers(Set<CategoryBean> loggers) {
        this.loggers = loggers;
    }
}
