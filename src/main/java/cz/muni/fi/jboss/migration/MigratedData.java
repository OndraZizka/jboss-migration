package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.spi.IMigratedData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 5:14 PM
 */
public class MigratedData {

    List<IMigratedData> migratedData =  new ArrayList();

    public List<IMigratedData> getMigratedData() {
        return migratedData;
    }

    public void setMigratedData(List<IMigratedData> migratedData) {
        this.migratedData = migratedData;
    }
}
