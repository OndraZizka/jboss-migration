package cz.muni.fi.jboss.migration.conf;

import cz.muni.fi.jboss.migration.spi.IMigrator;
import java.util.LinkedList;
import org.apache.commons.collections.map.MultiValueMap;

import java.util.List;

/**
 *  Holds global configuration and plugin-specific configuration.
 * 
 *  @author Roman Jakubco
 */
public class Configuration {

    private GlobalConfiguration globalConfig = new GlobalConfiguration();

    private List<ModuleSpecificProperty> moduleConfigs = new LinkedList();


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
