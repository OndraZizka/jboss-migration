package org.jboss.loom.migrators.classloading;

import org.jboss.loom.migrators.classloading.beans.AppModuleClassloadingConfig;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.jboss.loom.MigrationData;

/**
 * Holds config data for each app referenced by --app.path=...
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ClassloadingConfData extends MigrationData {
    
    private Map<File, AppModuleClassloadingConfig> appClassloadingConfigs = new HashMap();

    
    
    public Map<File, AppModuleClassloadingConfig> getAppClassloadingConfigs() { return appClassloadingConfigs; }
    
    
}// class
