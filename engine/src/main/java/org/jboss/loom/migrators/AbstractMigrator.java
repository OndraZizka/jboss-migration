/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators;

import org.apache.commons.collections.map.MultiValueMap;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.spi.IMigrator;


/**
 * Abstract class for Migrators containing configurations of Migrators.
 *
 * @author Roman Jakubco
 */
public abstract class AbstractMigrator implements IMigrator {

    private GlobalConfiguration globalConfig;

    // Configurables
    private Configuration.IfExists ifExists = Configuration.IfExists.WARN;
    private MultiValueMap config; // Catch-all map.


    public AbstractMigrator(GlobalConfiguration globalConfig) {
        this.globalConfig = globalConfig;
        this.config = new MultiValueMap();
    }


    //<editor-fold defaultstate="collapsed" desc="get/set">
    @Override public GlobalConfiguration getGlobalConfig() { return globalConfig; }
    @Override public void setGlobalConfig(GlobalConfiguration globalConfig) { this.globalConfig = globalConfig; }
    
    /** Returns a map of migrator-specific config values, e.g. conf.logging.logger.ifExists. */
    public MultiValueMap getConfig() { return config; }
    public void setConfig(MultiValueMap config) { this.config = config; }
    
    public Configuration.IfExists getIfExists() { return ifExists; }
    //</editor-fold>


    /**
     * Default implementation of examineConfigProperty();
     * Simply puts it in a MultiValueMap if the module prefix belongs to the implementation.
     */
    @Override
    //public int examineConfigProperty(String moduleName, String propName, String value) {
    public int examineConfigProperty(Configuration.ModuleSpecificProperty prop) {
        if( ! this.getConfigPropertyModuleName().equals( prop.getModuleId() ) ) return 0;

        switch( prop.getPropName() ){
            case Configuration.IfExists.PARAM_NAME:
                this.ifExists = Configuration.IfExists.valueOf_Custom(prop.getValue());
                break;
            default:
                if( this.config == null) this.config = new MultiValueMap();
                this.config.put(prop.getPropName(), prop.getValue());
                break;
        }
        return 1;
    }


    /**
     * "ID" of this IMigrator implementation, e.g "logging";
     * Used  by #examineConfigProperty() to decide whether to store the property.
     */
    abstract protected String getConfigPropertyModuleName();

    
    /**
     *  Parses the IfExists param of given name set for this migrator.
     *  For example, in a "logging" migrator, querying for "logger.ifExists" 
     *  would give what user set using "conf.logging.logger.ifExists".
     */
    protected Configuration.IfExists parseIfExistsParam( String paramName, Configuration.IfExists default_ ){
        String ifExistsParam = (String) this.getConfig().get( paramName );
        if( null == ifExistsParam )
            return default_;
        Configuration.IfExists ifExists_ = Configuration.IfExists.valueOf_Custom( ifExistsParam );
        return ifExists_;
    }

}// class
