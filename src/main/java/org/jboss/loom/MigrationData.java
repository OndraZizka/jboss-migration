package org.jboss.loom;

import org.jboss.loom.spi.IConfigFragment;

import java.util.LinkedList;
import java.util.List;

/**
 * Source server config data to be migrated.
 *
 * @author Roman Jakubco
 */
public class MigrationData {

    private List<IConfigFragment> configFragments = new LinkedList();


    @Override
    public String toString() {
        return "MigrationData{" + "configFragment=" + configFragments + '}';
    }


    //<editor-fold defaultstate="collapsed" desc="get/set">
    public List<IConfigFragment> getConfigFragments() {
        return configFragments;
    }
    //public void setConfigFragments(List<IConfigFragment> configFragment) { this.configFragments = configFragment; }
    //</editor-fold>

}// class
