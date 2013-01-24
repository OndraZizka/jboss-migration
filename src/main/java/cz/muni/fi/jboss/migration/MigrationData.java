package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.spi.ILoadedData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:37 AM
 */

//class MigrationData {
//
//    List<ILoadedData> loadedData - keeps the domain objects, filled with data parsed from AS 5.
//
//    //List<DomElement> domElements - DOM elements will be created on-the-fly.
//
//    //List<String> cliCommands - CLI commands will be created on-the-fly.
//}


public class MigrationData {

    List<ILoadedData> loadedData = new ArrayList();

    public List<ILoadedData> getLoadedData() {
        return loadedData;
    }

    public void setLoadedData(List<ILoadedData> loadedData) {
        this.loadedData = loadedData;
    }

}
