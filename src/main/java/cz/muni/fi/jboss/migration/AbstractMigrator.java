package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.conf.Configuration;
import cz.muni.fi.jboss.migration.conf.GlobalConfiguration;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import org.apache.commons.collections.map.MultiValueMap;


/**
 * Abstract class for Migrators containing configurations of Migrators.
 *
 * @author Roman Jakubco
 */
public abstract class AbstractMigrator implements IMigrator {

    private GlobalConfiguration globalConfig;

    private MultiValueMap config;
    

    public AbstractMigrator(GlobalConfiguration globalConfig, MultiValueMap config) {
        this.globalConfig = globalConfig;
        this.config = config;
    }

    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    @Override public GlobalConfiguration getGlobalConfig() { return globalConfig; }
    @Override public void setGlobalConfig(GlobalConfiguration globalConfig) { this.globalConfig = globalConfig; }
    
    public MultiValueMap getConfig() { return config; }
    public void setConfig(MultiValueMap config) { this.config = config; }
    //</editor-fold>
    
    
    
    /**
     *  Default implementation of examineConfigProperty();
     *  Simply puts it in a MultiValueMap if the module prefix belongs to the implementation.
     */
    @Override
    //public int examineConfigProperty(String moduleName, String propName, String value) {
    public int examineConfigProperty(Configuration.ModuleSpecificProperty prop) {
        if( ! this.getConfigPropertyModuleName().equals(prop.getModuleId()) )  return 0;
        if( this.config == null )  this.config = new MultiValueMap();
        this.config.put(prop.getPropName(), prop.getValue());
        return 1;
    }
    
    
    /**
     *  "ID" of this IMigrator implementation, e.g "logging";
     *  Used  by #examineConfigProperty() to decide whether to store the property.
     */
    abstract protected String getConfigPropertyModuleName();
    
}// class
