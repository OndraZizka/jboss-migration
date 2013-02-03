package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.spi.IMigrator;
import javafx.util.Pair;

import java.util.List;

/**
 * Abstract class for Migrators containing configurations of Migrators.
 *
 * @author Roman Jakubco
 *         Date: 2/2/13
 *         Time: 11:07 AM
 */
 public abstract class AbstractMigrator implements IMigrator {

    private GlobalConfiguration globalConfig;

    private List<Pair<String,String>> config;

    public AbstractMigrator(GlobalConfiguration globalConfig, List<Pair<String,String>> config){
        this.globalConfig = globalConfig;
        this.config =  config;
    }

    public GlobalConfiguration getGlobalConfig() {
        return globalConfig;
    }

    public void setGlobalConfig(GlobalConfiguration globalConfig) {
        this.globalConfig = globalConfig;
    }

    public List<Pair<String, String>> getConfig() {
        return config;
    }

    public void setConfig(List<Pair<String, String>> config) {
        this.config = config;
    }

}
