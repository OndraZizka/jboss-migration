package cz.muni.fi.jboss.migration.spi;

import cz.muni.fi.jboss.migration.MigrationContext;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:35 AM
 */
public interface IMigrator {

    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException, FileNotFoundException;

    public void apply(MigrationContext ctx);

    public List<Node> generateDomElements(MigrationContext ctx) throws MigrationException;

    public List<String> generateCliScripts(MigrationContext ctx) throws CliScriptException;
}
