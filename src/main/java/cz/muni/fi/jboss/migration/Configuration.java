package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.spi.IMigrator;
import org.apache.commons.collections.map.MultiValueMap;

import java.util.Map;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:36 AM
 */

public class Configuration {

    private GlobalConfiguration options;

    private Map<Class<? extends IMigrator>, MultiValueMap> moduleOtions;

    public GlobalConfiguration getGlobal() {
        return options;
    }

    public void setOptions(GlobalConfiguration options) {
        this.options = options;
    }

    public Map<Class<? extends IMigrator>, MultiValueMap> getModuleOtions() {
        return moduleOtions;
    }

    public void setModuleOtions(Map<Class<? extends IMigrator>, MultiValueMap> moduleOtions) {
        this.moduleOtions = moduleOtions;
    }


    public MultiValueMap getForMigrator(Class<? extends IMigrator> migrator) {
        return moduleOtions.get(migrator);

    }
}
