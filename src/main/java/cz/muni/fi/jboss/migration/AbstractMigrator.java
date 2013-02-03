package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.spi.IMigrator;
import org.apache.commons.collections.map.MultiValueMap;

/**
 * Abstract class for Migrators containing configurations of Migrators.
 *
 * @author Roman Jakubco
 *         Date: 2/2/13
 *         Time: 11:07 AM
 */
public abstract class AbstractMigrator implements IMigrator {

    private GlobalConfiguration globalConfig;

    private MultiValueMap config;

    public AbstractMigrator(GlobalConfiguration globalConfig, MultiValueMap config) {
        this.globalConfig = globalConfig;
        this.config = config;
    }

    public GlobalConfiguration getGlobalConfig() {
        return globalConfig;
    }

    public void setGlobalConfig(GlobalConfiguration globalConfig) {
        this.globalConfig = globalConfig;
    }

    public MultiValueMap getConfig() {
        return config;
    }

    public void setConfig(MultiValueMap config) {
        this.config = config;
    }
}
