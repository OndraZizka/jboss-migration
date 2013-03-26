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
 */
public class MigrationContext {

    /** Instances of IMigrator; In a form of map Class -> instance.  */
    private Map<Class<? extends IMigrator>, IMigrator> migrators = new HashMap();

    
    private Map<Class<? extends IMigrator>, MigrationData> migrationData = new HashMap();

    private Set<RollbackData> rollbackDatas = new HashSet();

    private Document as7XmlDoc;

    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public Map<Class<? extends IMigrator>, IMigrator> getMigrators() { return migrators; }
    public void setMigrators(Map<Class<? extends IMigrator>, IMigrator> migrators) { this.migrators = migrators; }
    public Map<Class<? extends IMigrator>, MigrationData> getMigrationData() { return migrationData; }
    public void setMigrationData(Map<Class<? extends IMigrator>, MigrationData> migrationData) { this.migrationData = migrationData; }
    public Set<RollbackData> getRollbackData() { return rollbackDatas; }
    public void setRollbackDatas(Set<RollbackData> rollbackDatas) { this.rollbackDatas = rollbackDatas; }
    public Document getAS7XmlDoc() { return as7XmlDoc; }
    public void setAS7XmlDoc(Document standaloneDoc) { this.as7XmlDoc = standaloneDoc; }
    //</editor-fold>
    
}// class
