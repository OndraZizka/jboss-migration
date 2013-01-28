package cz.muni.fi.jboss.migration;

import java.io.File;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:48 AM
 */
public class GlobalConfiguration {

    private String dirAS5;

    private String dirAS7;

    private String profileAS5 = "default";

    private String profileAS7;

    private String standaloneFilePath;

    public String getProfileAS7() {
        return profileAS7;
    }

    public void setProfileAS7(String profileAS7) {
        this.profileAS7 = profileAS7;
    }

    public String getDirAS7() {
        return dirAS7;
    }

    public void setDirAS7(String dirAS7) {
        this.dirAS7 = dirAS7;
    }

    public String getProfileAS5() {
        return profileAS5;
    }

    public void setProfileAS5(String profileAS5) {
        this.profileAS5 = profileAS5;
    }

    public String getDirAS5() {
        return dirAS5;
    }

    public void setDirAS5(String dirAS5) {
        this.dirAS5 = dirAS5;
    }

    public String getStandaloneFilePath() {
        return standaloneFilePath;
    }

    public void setStandalonePath() {
        if(getProfileAS5() == null){
            standaloneFilePath = getDirAS7() + File.separator + "standalone" +
                    File.separator + "configuration" + File.separator + "standalone.xml";
        } else {
            standaloneFilePath = getDirAS7() + File.separator + "standalone"
                    + File.separator + "configuration" + File.separator + getProfileAS7();
        }

    }
}
