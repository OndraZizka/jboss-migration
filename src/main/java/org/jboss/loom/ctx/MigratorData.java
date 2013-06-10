/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.ctx;

import org.jboss.loom.spi.IConfigFragment;

import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.utils.Utils;

/**
 * Source server config data to be migrated.
 *
 * @author Roman Jakubco
 * 
 * JAXB removed in favor of MigrationDataReportBean
 */
//@XmlRootElement
//@XmlAccessorType( XmlAccessType.NONE )
public class MigratorData {

    private List<? extends IConfigFragment> configFragments;
    
    //@XmlAttribute(name = "fromMigrator")
    private Class<? extends IMigrator> fromMigrator;
    

    public MigratorData() {
        this(new LinkedList());
        
    }

    public MigratorData( List<? extends IConfigFragment> configFragments ) {
        this.configFragments = configFragments;
        this.fromMigrator = Utils.findSubclassInStackTrace( IMigrator.class );
    }
    

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " with " + (configFragments == null ? "no" : configFragments.size()) + " config fragments";
    }

    
    // for JAXB
    //@XmlAttribute(name = "desc")
    //public String getDescription(){ return toString(); }


    public Class<? extends IMigrator> getFromMigrator() { return fromMigrator; }
    
    //@XmlElementWrapper(name = "configFragments")
    //@XmlElement(name = "configFragment")
    public <T extends IConfigFragment> List<T> getConfigFragments() {
        return (List<T>) configFragments;
    }

}// class
