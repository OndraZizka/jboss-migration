package cz.muni.fi.jboss.migration.conf;

import cz.muni.fi.jboss.migration.utils.Utils;
import java.io.File;

/**
 * Class for storing global information needed for migration. Like dir of AS7, AS5, and profiles
 *
 * @author Roman Jakubco
 */
public class GlobalConfiguration {
    
    // TODO: With regard of possibility to migrate from other vendors' AS,
    //       split to AS 7 config class, and then 1 class per server (AS 5, WebLogic, ...)

    // AS 7 stuff
    private String as7dir;
    private String as7configPath = "standalone/configuration/standalone.xml";

    // AS 5 stuff
    private As5Config as5config;

   
    // Non-server stuff
    private String appPath;

    private boolean skipValidation;
    
    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getAS7Dir() { return as7dir; }
    public void setAS7Dir(String dirAS7) { this.as7dir = dirAS7; }
    public String getAS7ConfigPath() { return as7configPath; }
    public void setAS7ConfigPath(String confPathAS7) { this.as7configPath = confPathAS7; }
    public String getAs7ConfigFilePath() {
        return new File(getAS7Dir(), getAS7ConfigPath()).getPath();  // TODO: Return File and use that.
    }

    public As5Config getAS5Config() { return as5config; }
    public void setAS5Config(As5Config as5config) { this.as5config = as5config; }

    public String getAppPath() { return appPath; }
    public void setAppPath(String appPath) { this.appPath = appPath; }
    
    public boolean isSkipValidation() { return skipValidation; }
    public void setSkipValidation(boolean skipValidation) { this.skipValidation = skipValidation; }
    //</editor-fold>

    
}// class
