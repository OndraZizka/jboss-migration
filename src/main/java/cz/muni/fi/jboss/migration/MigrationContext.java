package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.spi.IMigrator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

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

    private Map<Class<? extends IMigrator>, IMigrator> migrators = new HashMap();

    private Map<Class<? extends IMigrator>, MigrationData> migrationData = new HashMap();

    private Set<CopyMemory> copyMemories = new HashSet();

    private DocumentBuilder docBuilder;

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

    public Set<CopyMemory> getCopyMemories() {
        return copyMemories;
    }

    public void setCopyMemories(Set<CopyMemory> copyMemories) {
        this.copyMemories = copyMemories;
    }

    public void createBuilder() throws ParserConfigurationException{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
       docBuilder = dbf.newDocumentBuilder();
    }

    public DocumentBuilder getDocBuilder() {
        return docBuilder;
    }

    public String checkingMethod(String script, String name, String setter){
        if(setter != null){
            if (!setter.isEmpty()) {
                script = script.concat(name + "=" + setter);
            }
        }
        return script;
    }
}
