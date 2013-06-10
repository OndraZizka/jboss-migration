package org.jboss.loom.migrators.classloading;

import org.jboss.loom.migrators.classloading.beans.DeploymentClassloadingConfig;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.jboss.loom.ctx.MigratorData;

/**
 * Holds config data for each app referenced by --app.path=...
 * 
 * Currently, it only stores whether classloadingInfo exists.
 * Non-null value means it does. Values are irrelevant.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ClassloadingConfData extends MigratorData {
    
    private Map<File, DeploymentClassloadingConfig> deplsClassloadingConfigs = new HashMap();

    private boolean foundSomething = false; // Whether to create a ManualAction.

    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public Map<File, DeploymentClassloadingConfig> getDeploymentsClassloadingConfigs() { return deplsClassloadingConfigs; }
    
    public boolean isFoundSomething() { return foundSomething; }
    public void setFoundSomething( boolean foundSomething ) { this.foundSomething = foundSomething; }
    //</editor-fold>
    
}// class
