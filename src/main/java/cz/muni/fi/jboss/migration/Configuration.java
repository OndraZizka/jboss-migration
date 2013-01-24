package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.spi.IMigrator;
import javafx.util.Pair;

import java.util.List;
import java.util.Map;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:36 AM
 */


//// Keeps the data parsed from user input (arguments, eventual .properties etc.)
//class Configuration {
//
//    // Has a class member per option.
//    private GlobalConfiguration options;
//
//    // Per-module options
//    private Map<Class<T extends IMigrator>, List<Pair<String,String>>>
//
//}

public class Configuration {

    private GlobalConfiguration options;

    private Map<Class<? extends IMigrator>, List<Pair<String,String>>> moduleOtions;

    public GlobalConfiguration getGlobal(){
         return options;
    }

    public void setOptions(GlobalConfiguration options) {
        this.options = options;
    }

    public Map<Class<? extends IMigrator>, List<Pair<String, String>>> getModuleOtions() {
        return moduleOtions;
    }

    public void setModuleOtions(Map<Class<? extends IMigrator>, List<Pair<String, String>>> moduleOtions) {
        this.moduleOtions = moduleOtions;
    }

    public List<Pair<String,String>> getForMigrator(Class<? extends IMigrator> migrator){
         return moduleOtions.get(migrator);

    }
}
