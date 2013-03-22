package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.spi.IMigrator;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Context of migration. Stores all necessary objects and information for all Migrators.
 *
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:37 AM
 */

public class MigrationContext {

    private Map<Class<? extends IMigrator>, IMigrator> migrators = new HashMap();

    private Map<Class<? extends IMigrator>, MigrationData> migrationData = new HashMap();

    private Set<RollbackData> rollbackDatas = new HashSet();

    private DocumentBuilder docBuilder;

    private Document standaloneDoc;

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

    public Set<RollbackData> getRollbackData() {
        return rollbackDatas;
    }

    public void setRollbackDatas(Set<RollbackData> rollbackDatas) {
        this.rollbackDatas = rollbackDatas;
    }

    public void createBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        docBuilder = dbf.newDocumentBuilder();
    }

    public DocumentBuilder getDocBuilder() {
        return docBuilder;
    }

    public Document getStandaloneDoc() {
        return standaloneDoc;
    }

    public void setStandaloneDoc(Document standaloneDoc) {
        this.standaloneDoc = standaloneDoc;
    }
}
