package cz.muni.fi.jboss.migration.utils;

import cz.muni.fi.jboss.migration.conf.Configuration;
import cz.muni.fi.jboss.migration.FileTransferInfo;
import cz.muni.fi.jboss.migration.ex.CopyException;
import cz.muni.fi.jboss.migration.ex.RollbackMigrationException;
import java.io.File;
import java.util.Collection;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;


/**
 *  A temporary class until we create some RollbackManager.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class RollbackUtils {
    private static final Logger log = LoggerFactory.getLogger(RollbackUtils.class);
    
    
    /**
     * Reverts the standalone file to its original state before migration if the app fails.
     *
     * @param doc    object of Document representing original standalone file. This file is saved in Main before migration.
     * @param config configuration of app
     * 
     * TODO: MIGR-23: Rollback needs to be done on the file level, not by writing back unchanged DOM.
     */
    public static void rollbackAS7ConfigFile(Document doc, Configuration config) throws Exception {
        log.debug("rollbackAS7ConfigFile() " + config);
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult result = new StreamResult(new File(config.getGlobal().getAS7Config().getConfigFilePath()));
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            throw new RollbackMigrationException("Failed rolling the target server config back: " + ex.toString(), ex);
        }
    }

    
    /**
     *  Removes the copied data for migration from AS5.
     *  All rollbackData have set path to copied files in AS7.
     *  So this method iterates over the given collection of these objects and try to delete them.
     *
     * @param rollbackData  The files which where copied to the AS7 folder.
     */
    public static void removeData(Collection<FileTransferInfo> rollbackData) {
        log.debug("removeData() " + rollbackData);
        for (FileTransferInfo rolldata : rollbackData) {
            if (!(rolldata.getType().equals(FileTransferInfo.Type.DRIVER))) {
                FileUtils.deleteQuietly(new File(rolldata.getTargetPath(), rolldata.getName()));
            }
        }
    }

    
    /**
     * Helping method for copying files from AS5 to AS7. It checks if list is empty and if not then set HomePath and
     * targetPath of object of RollbackData. Plus for driver it creates special path from module of the driver.
     *
     * @param rollData   Object representing files which should be copied
     * @param files      List of files found for this object of RollbackData
     * @param targetPath path to AS7 home dir
     * @throws cz.muni.fi.jboss.migration.ex.CopyException
     *          if no file was found and roll data is not representing driver and if it is then if module of
     *          driver is null
     *
     * TODO: This needs to be moved to some RollbackManager.
     */
    public static void setRollbackData( FileTransferInfo rollData, Collection<File> files, String targetPath ) throws CopyException {
        log.debug("setRollbackData() " + rollData);
        
        // TODO: Most likely should be in the caller method.
        if (files.isEmpty()) {
            throw new CopyException("Cannot locate file: " + rollData.getName());
        }
        
        File firstFile = files.iterator().next();
        
        rollData.setHomePath(firstFile.getAbsolutePath()); // Why absolute?
        
        // TODO:     This pulls IMigrator implementations details into generic class.
        // MIGR-23   Must be either generalized or moved to those implementations.
        switch (rollData.getType()) {
            /* We really don't want to migrate logs.
            case LOG:
                rollData.setTargetPath(Utils.createPath(targetPath, "standalone", "log").getPath());
                break; */
            case RESOURCE:
                rollData.setTargetPath(Utils.createPath(targetPath, "standalone", "deployments").getPath());
                break;
            case SECURITY:
                rollData.setTargetPath(Utils.createPath(targetPath, "standalone", "configuration").getPath());
                break;
            case DRIVER:
            case LOGMODULE:
                {
                    rollData.setName(firstFile.getName());
                    if (rollData.getModuleName() == null)
                        throw new CopyException("Module in a rollback record is null!");
                    String moduleSubPath = rollData.getModuleName().replace('.', '/');
                    rollData.setTargetPath(Utils.createPath(targetPath, "modules", moduleSubPath, "main").getPath());
                }
                break;
        }
    }// setRollbackData()
    
}// class
