package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.spi.IMigrator;

import java.util.Map;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:37 AM
 */

// Keeps everything else
//class MigrationContext {
//
//    Map<Class<T extends IMigrator>, IMigrator> migrators;
//
//    Map<Class<T extends IMigrator>, MigrationData> migrationData;
//}

public class MigrationContext {

    Map<Class<? extends IMigrator>, IMigrator> migrators;

    Map<Class<? extends IMigrator>, MigrationData> migrationData;

    Map<Class<? extends IMigrator>, MigratedData> migratedData;

    public Map<Class<? extends IMigrator>, IMigrator> getMigrators() {
        return migrators;
    }

    public void setMigrators(Map<Class<? extends IMigrator>, IMigrator> migrators) {
        this.migrators = migrators;
    }

    public Map<Class<? extends IMigrator>, MigrationData> getMigrationData() {
        return migrationData;
    }

    public void setMigrationData(Map<Class<? extends IMigrator>, MigrationData> migrationData) {
        this.migrationData = migrationData;
    }

    public Map<Class<? extends IMigrator>, MigratedData> getMigratedData() {
        return migratedData;
    }

    public void setMigratedData(Map<Class<? extends IMigrator>, MigratedData> migratedData) {
        this.migratedData = migratedData;
    }
}
