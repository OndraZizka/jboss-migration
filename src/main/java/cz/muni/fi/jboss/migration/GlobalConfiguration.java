package cz.muni.fi.jboss.migration;

import java.io.File;

/**
 * Class for storing global information needed for migration. Like dir of AS7, AS5, and profiles
 *
 * @author Roman Jakubco
 */
public class GlobalConfiguration {

    private String dirAS5;

    private String dirAS7;

    private String profileAS5 = "default";

    private String confPathAS7 = "standalone/configuration/standalone.xml";

    private String appPath;

    private boolean skipValidation;

    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getDirAS7() { return dirAS7; }
    public void setDirAS7(String dirAS7) { this.dirAS7 = dirAS7; }
    public String getConfPathAS7() { return confPathAS7; }
    public void setConfPathAS7(String confPathAS7) { this.confPathAS7 = confPathAS7; }
    public String getStandaloneFilePath() {
        return new File(getDirAS7(), getConfPathAS7()).getPath();  // TODO: Return File and use that.
    }
    //public void setStandaloneFilePath() { standaloneFilePath = getDirAS7() + File.separator + getConfPathAS7(); }

    public String getDirAS5() { return dirAS5; }
    public void setDirAS5(String dirAS5) { this.dirAS5 = dirAS5; }
    public String getProfileAS5() { return profileAS5; }
    public void setProfileAS5(String profileAS5) { this.profileAS5 = profileAS5; }

    public String getAppPath() { return appPath; }
    public void setAppPath(String appPath) { this.appPath = appPath; }
    
    public boolean isSkipValidation() { return skipValidation; }
    public void setSkipValidation(boolean skipValidation) { this.skipValidation = skipValidation; }
    //</editor-fold>
    
}// class
