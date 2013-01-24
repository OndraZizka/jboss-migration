package cz.muni.fi.jboss.migration.spi;

import cz.muni.fi.jboss.migration.MigrationContext;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:35 AM
 */
public interface IMigrator {

    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException, FileNotFoundException;

    public void apply(MigrationContext ctx);

    public void migrate(MigrationContext ctx);
}
