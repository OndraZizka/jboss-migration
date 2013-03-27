package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.spi.IMigrator;
import org.w3c.dom.Document;

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

    private Set<FileTransferInfo> rollbackData = new HashSet();

    private Document as7ConfigXmlDoc;
    private Document as7ConfigXmlDocOriginal;
    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public Map<Class<? extends IMigrator>, IMigrator> getMigrators() { return migrators; }
    public void setMigrators(Map<Class<? extends IMigrator>, IMigrator> migrators) { this.migrators = migrators; }
    
    public Map<Class<? extends IMigrator>, MigrationData> getMigrationData() { return migrationData; }
    public void setMigrationData(Map<Class<? extends IMigrator>, MigrationData> migrationData) { this.migrationData = migrationData; }
    
    public Set<FileTransferInfo> getRollbackData() { return rollbackData; }
    public void setRollbackData(Set<FileTransferInfo> rollbackData) { this.rollbackData = rollbackData; }
    
    public Document getAS7ConfigXmlDoc() { return as7ConfigXmlDoc; }
    public void setAS7ConfigXmlDoc(Document standaloneDoc) { this.as7ConfigXmlDoc = standaloneDoc; }
    
    public Document getAs7ConfigXmlDocOriginal() { return as7ConfigXmlDocOriginal; }
    public void setAs7ConfigXmlDocOriginal(Document as7XmlDocOriginal) { this.as7ConfigXmlDocOriginal = as7XmlDocOriginal; }
    //</editor-fold>

    
}// class
