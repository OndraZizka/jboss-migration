package cz.muni.fi.jboss.migration.utils;

import cz.muni.fi.jboss.migration.Configuration;
import cz.muni.fi.jboss.migration.RollbackData;
import cz.muni.fi.jboss.migration.ex.CopyException;
import java.io.File;
import java.util.Collection;
import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FileUtils;

import org.w3c.dom.Document;


/**
 *  A temporary class until we create some RollbackManager.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class RollbackUtils {

    
    /**
     * Reverts the standalone file to its original state before migration if the app fails.
     *
     * @param doc    object of Document representing original standalone file. This file is saved in Main before migration.
     * @param config configuration of app
     */
    public static void cleanStandalone(Document doc, Configuration config) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult result = new StreamResult(new File(config.getGlobal().getAs7ConfigFilePath()));
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    
    /**
     *  Removes the copied data for migration from AS5.
     *  All rollbackData have set path to copied files in AS7.
     *  So this method iterates over the given collection of these objects and try to delete them.
     *
     * @param rollbackData  The files which where copied to the AS7 folder.
     */
    public static void removeData(Collection<RollbackData> rollbackDatas) {
        for (RollbackData rolldata : rollbackDatas) {
            if (!(rolldata.getType().equals(RollbackData.Type.DRIVER))) {
                FileUtils.deleteQuietly(new File(rolldata.getTargetPath(), rolldata.getName()));
            }
        }
    }

    
    /**
     * Helping method for copying files from AS5 to AS7. It checks if list is empty and if not then set HomePath and
     * targetPath of object of RollbackData. Plus for driver it creates special path from module of the driver.
     *
     * @param rollData   object representing files which should be copied
     * @param list       List of files found for this object of RollbackData
     * @param targetPath path to AS7 home dir
     * @throws cz.muni.fi.jboss.migration.ex.CopyException
     *          if no file was found and roll data is not representing driver and if it is then if module of
     *          driver is null
     *
     * TODO: This needs to be moved to some RollbackManager.
     */
    public static void setRollbackData(RollbackData rollData, List<File> list, String targetPath) throws CopyException {
        
        // TODO: Most likely should be in the caller method.
        if (list.isEmpty()) {
            throw new CopyException("Cannot locate file: " + rollData.getName());
        }
        
        rollData.setHomePath(list.get(0).getAbsolutePath());
        switch (rollData.getType()) {
            case LOG:
                rollData.setTargetPath(Utils.createPath(targetPath, "standalone", "log").getPath());
                break;
            case RESOURCE:
                rollData.setTargetPath(Utils.createPath(targetPath, "standalone", "deployments").getPath());
                break;
            case SECURITY:
                rollData.setTargetPath(Utils.createPath(targetPath, "standalone", "configuration").getPath());
                break;
            case DRIVER:
            case LOGMODULE:
                {
                    rollData.setName(list.get(0).getName());
                    if (rollData.getModule() == null)
                        throw new CopyException("Module in a rollback record is null!");
                    String moduleSubPath = rollData.getModule().replace('.', '/');
                    rollData.setTargetPath(Utils.createPath(targetPath, "modules", moduleSubPath, "main").getPath());
                }
                break;
        }
    }// setRollbackData()
    
}// class
