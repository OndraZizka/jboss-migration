/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.conf;

import org.jboss.loom.spi.IMigrator;
import java.util.LinkedList;
import org.apache.commons.collections.map.MultiValueMap;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *  Holds global configuration and plugin-specific configuration.
 * 
 *  @author Roman Jakubco
 */
@XmlRootElement(name="config")
@XmlAccessorType( XmlAccessType.NONE )
public class Configuration {

    @XmlElement
    private GlobalConfiguration globalConfig = new GlobalConfiguration();

    private List<ModuleSpecificProperty> moduleConfigs = new LinkedList();
    
    /**
     *  What to do if some resource already exists.
     *  MERGE (ModelNode into current model) and ASK (interactive) are not supported yet.
     */
    public enum IfExists {
        FAIL, WARN, SKIP, MERGE, OVERWRITE, ASK, GUI;
        
        /** The same as valueOf(), only case-insensitive. */
        public static IfExists valueOf_Custom(String str) throws IllegalArgumentException {
            try {
                return valueOf( str.toUpperCase() );
            }
            catch( IllegalArgumentException | NullPointerException ex ){
                throw new IllegalArgumentException("ifExists must be one of FAIL, WARN, SKIP, MERGE, OVERWRITE, ASK. Was: " + str);
            }
        }
        
        public static final String PARAM_NAME = "ifExists";
    }// enum


    /**
     *  Returns all config migrator-specific values for the given migrator.
     */
    private MultiValueMap getForMigrator(Class<? extends IMigrator> migratorClass) {
        //return moduleConfigs.get(migrator);
        
        MultiValueMap map = new MultiValueMap();
        /*IMigrator mig = ...  // or migratorClass.getMethod("", parameterTypes);
                
        for( ModuleSpecificProperty prop : moduleConfigs ){
            if( 0 != mig.examineConfigProperty(prop) )
        }*/
        throw new UnsupportedOperationException();
        // TODO: This method must be in a context - only the instances have the list of values they have taken.
    }
    
    
    /**
     *  Triplet for module specific property, e.g --conf.logging.merge=true .
     */
    public static class ModuleSpecificProperty{
        
        private String moduleId;
        private String propName;
        private String value;

        public ModuleSpecificProperty(String moduleId, String propName, String value) {
            this.moduleId = moduleId;
            this.propName = propName;
            this.value = value;
        }

        public String getModuleId() { return moduleId; }
        public void setModuleId(String moduleId) { this.moduleId = moduleId; }
        public String getPropName() { return propName; }
        public void setPropName(String propName) { this.propName = propName; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }        
    }
    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public GlobalConfiguration getGlobal() { return globalConfig; }
    public void setGlobalConfig(GlobalConfiguration options) { this.globalConfig = options; }
    public List<ModuleSpecificProperty> getModuleConfigs() { return moduleConfigs; }
    public void setModuleConfigs(List<ModuleSpecificProperty> moduleConfigs) { this.moduleConfigs = moduleConfigs; }
    //</editor-fold>    
    
}// class
