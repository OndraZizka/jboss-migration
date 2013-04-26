package org.jboss.loom.conf;

import org.jboss.loom.utils.Utils;

import java.io.File;

/**
 * AS 5 specific configuration.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class AS5Config {

    private String as5dir;
    private String as5profileName = "default";
    public static final String AS5_PROFILES_DIR = "server";
    public static final String AS5_DEPLOY_DIR = "deploy";
    public static final String AS5_CONF_DIR = "conf";

    public File getProfileDir() {
        return Utils.createPath(as5dir, AS5_PROFILES_DIR, as5profileName);
    }

    public File getDeployDir() {
        return Utils.createPath(as5dir, AS5_PROFILES_DIR, as5profileName, AS5_DEPLOY_DIR);
    }

    public File getConfDir() {
        return Utils.createPath(as5dir, AS5_PROFILES_DIR, as5profileName, AS5_CONF_DIR);
    }


    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getDir() {
        return as5dir;
    }

    public void setDir(String as5dir) {
        this.as5dir = as5dir;
    }

    public String getProfileName() {
        return as5profileName;
    }

    public void setProfileName(String profileName) {
        this.as5profileName = profileName;
    }
    //</editor-fold>

}// class
