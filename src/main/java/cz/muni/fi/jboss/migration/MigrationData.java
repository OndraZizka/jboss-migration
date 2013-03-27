package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.spi.IConfigFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for storing data from AS5 which should be migrated.
 *
 * @author Roman Jakubco
 */
public class MigrationData {

    List<IConfigFragment> configFragment = new ArrayList();

    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public List<IConfigFragment> getConfigFragments() { return configFragment; }
    public void setConfigFragment(List<IConfigFragment> configFragment) { this.configFragment = configFragment; }
    //</editor-fold>
    
}// class
