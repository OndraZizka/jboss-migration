package cz.muni.fi.jboss.migration.utils;

import cz.muni.fi.jboss.migration.RollbackData;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.CopyException;

import java.io.File;
import java.util.List;

/**
 * Utils class containing helping classes
 *
 * @author Roman Jakubco
 *         Date: 2/2/13
 *         Time: 1:05 PM
 */
public class Utils {
    /**
     * Method for testing if given string is null or empty and if it is then CliScriptException is thrown with given message
     *
     * @param string string for testing
     * @param errMsg message for exception
     * @param name name of property of tested value
     * @throws CliScriptException if tested string is empty or null
     */
    public static void throwIfBlank(String string, String errMsg, String name) throws CliScriptException{
        if((string == null) || (string.isEmpty())){
            throw new CliScriptException(name + errMsg) ;
        }
    }

    /**
     * Helping method for copying files from AS5 to AS7. It checks if list is empty and if not then set HomePath and
     * targetPath of object of RollbackData. Plus for driver it creates special path from module of the driver.
     *
     * @param rollData object representing files which should be copied
     * @param list List of files found for this object of RollbackData
     * @param targetPath path to AS7 home dir
     * @throws cz.muni.fi.jboss.migration.ex.CopyException if no file was found and rolldata is not representing driver and if it is then if module of
     *                       driver is null
     */
     public static void setRollbackData(RollbackData rollData, List<File> list, String targetPath)
            throws CopyException {
        if( (list.isEmpty()) && !(rollData.getType().equals("driver")) ){
            throw  new CopyException("Cannot locate log file: " + rollData.getName() + "!");
        } else{
            rollData.setHomePath(list.get(0).getAbsolutePath());

            if(rollData.getType().equals("driver")){
                rollData.setName(list.get(0).getName());
                String module;

                if(rollData.getModule() != null){
                    String[] parts = rollData.getModule().split("\\.");
                    module = "";
                    for(String s : parts){
                        module = module + s + File.separator;
                    }
                    rollData.setTargetPath(targetPath + File.separator + "modules" + File.separator +
                            module  + "main");
                } else{
                    throw new CopyException("Error: Module for driver is null!");
                }
            } else{
                rollData.setTargetPath(targetPath + File.separator + "standalone" + File.separator + "log");
            }
        }
    }
}
