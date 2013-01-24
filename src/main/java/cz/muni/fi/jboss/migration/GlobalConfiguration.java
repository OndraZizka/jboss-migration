package cz.muni.fi.jboss.migration;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:48 AM
 */
public class GlobalConfiguration {

    private String dirAS5;

    private String dirAS7;

    private String profile;

    private String confAS7;

    public String getConfAS7() {
        return confAS7;
    }

    public void setConfAS7(String confAS7) {
        this.confAS7 = confAS7;
    }

    public String getDirAS7() {
        return dirAS7;
    }

    public void setDirAS7(String dirAS7) {
        this.dirAS7 = dirAS7;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getDirAS5() {
        return dirAS5;
    }

    public void setDirAS5(String dirAS5) {
        this.dirAS5 = dirAS5;
    }
}
