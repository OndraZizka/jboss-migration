/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.logging.jaxb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.xml.bind.annotation.*;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.jboss.loom.migrators.OriginWiseJaxbBase;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;

/**
 * JAXB bean for category (AS5)
 *
 * @author Roman Jakubco
 */
@ConfigPartDescriptor(
    name = "Logging category ${categoryName}"
)
@XmlRootElement(name = "category")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "category")
public class CategoryBean extends OriginWiseJaxbBase<CategoryBean> implements IConfigFragment {

    @XmlAttribute(name = "name")
    private String categoryName;

    @XmlPath("priority/@value")
    private String categoryValue;

    @XmlPath("appender-ref/@ref") // MIGR-108 "priority/@appender-ref"
    private List<String> appenderRefs;

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getCategoryValue() { return categoryValue; }
    public void setCategoryValue(String categoryValue) { this.categoryValue = categoryValue; }
    public List<String> getAppenderRefs() { return appenderRefs == null ? this.appenderRefs = new ArrayList() : this.appenderRefs; }
    public void setAppenderRefs(Collection<String> appenderRefs) {
        Set<String> temp = new HashSet();
        if( null != appenderRefs )
            temp.addAll(appenderRefs);
        this.appenderRefs = new LinkedList(temp);
    }


    @Override public String toString() {
        return "CategoryBean{ name:" + categoryName + " level:" + categoryValue + " appenderRef:" + appenderRefs + " }";
    }
    
    
    @Override public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode( this.categoryName );
        return hash;
    }


    @Override public boolean equals( Object obj ) {
        if( obj == null )  return false;
        if( getClass() != obj.getClass() )  return false;
        final CategoryBean other = (CategoryBean) obj;
        if( !Objects.equals( this.categoryName, other.categoryName ) )  return false;
        return true;
    }
    
}// class
