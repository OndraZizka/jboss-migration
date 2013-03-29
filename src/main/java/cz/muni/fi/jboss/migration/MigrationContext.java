package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.actions.IMigrationAction;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

    
    private final Map<Class<? extends IMigrator>, MigrationData> migrationData = new HashMap();

    private final Set<FileTransferInfo> rollbackData = new HashSet();
    // TODO: Replace with:
    
    private final List<IMigrationAction> actions = new LinkedList();
    // TBC: Roman said there are cases when the same file is suggested for copying by multiple migrators?
    

    private Document as7ConfigXmlDoc;
    private Document as7ConfigXmlDocOriginal;
    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public Map<Class<? extends IMigrator>, IMigrator> getMigrators() { return migrators; }
    
    public Map<Class<? extends IMigrator>, MigrationData> getMigrationData() { return migrationData; }
    
    public Set<FileTransferInfo> getFileTransfers() { return rollbackData; }

    public List<IMigrationAction> getActions() { return actions; }
    
    
    
    public Document getAS7ConfigXmlDoc() { return as7ConfigXmlDoc; }
    public void setAS7ConfigXmlDoc(Document standaloneDoc) { this.as7ConfigXmlDoc = standaloneDoc; }
    
    public Document getAs7ConfigXmlDocOriginal() { return as7ConfigXmlDocOriginal; }
    public void setAs7ConfigXmlDocOriginal(Document as7XmlDocOriginal) { this.as7ConfigXmlDocOriginal = as7XmlDocOriginal; }
    //</editor-fold>

    
}// class
