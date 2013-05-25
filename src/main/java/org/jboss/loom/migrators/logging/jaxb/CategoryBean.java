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

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class for unmarshalling and representing category (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "category")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "category")

public class CategoryBean implements IConfigFragment {

    @XmlAttribute(name = "name")
    private String categoryName;

    @XmlPath("priority/@value")
    private String categoryValue;

    @XmlPath("priority/@appender-ref")
    private Set<String> appenderRef;

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getCategoryValue() { return categoryValue; }
    public void setCategoryValue(String categoryValue) { this.categoryValue = categoryValue; }
    public Set<String> getAppenderRef() { return appenderRef; }
    public void setAppenderRef(Collection<String> appenderRef) {
        Set<String> temp = new HashSet();
        temp.addAll(appenderRef);
        this.appenderRef = temp;
    }


    @Override public String toString() {
        return "CategoryBean{ name:" + categoryName + " level:" + categoryValue + " appenderRef:" + appenderRef + " }";
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
