package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.spi.IConfigFragment;

import java.util.ArrayList;
import java.util.List;

/**
 *  Source server config data to be migrated.
 *
 *  @author Roman Jakubco
 */
public class MigrationData {

    private List<IConfigFragment> configFragments = new ArrayList();


    @Override
    public String toString() {
        return "MigrationData{" + "configFragment=" + configFragments + '}';
    }
    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public List<IConfigFragment> getConfigFragments() { return configFragments; }
    //public void setConfigFragments(List<IConfigFragment> configFragment) { this.configFragments = configFragment; }
    //</editor-fold>
    
}// class
