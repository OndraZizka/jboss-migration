package org.jboss.loom.conf;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.jboss.as.controller.client.ModelControllerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ConfigurationValidator {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationValidator.class);
    

    /**
     *  Validates the config - checks if the paths exist, contain the expected files etc.
     * 
     *  @returns  True if everything is OK.
     */
    public static List<String> validate(Configuration config) {
        LinkedList<String> problems = new LinkedList<>();
        
        // AS 5
        String path = config.getGlobal().getAS5Config().getDir();
        if( null == path )
            problems.add("src.dir was not set.");
        else if( ! new File(path).isDirectory() )
            problems.add("src.dir is not a directory: " + path);
        else if( ! new File(path, "server").isDirectory() )
            problems.add("src.dir doesn't appear to be JBoss AS 5 directory - doesn't contain server/ subdir: " + path);
        else {
            String profileName = config.getGlobal().getAS5Config().getProfileName();
            if( null == profileName )
                ;
            else {
                File profileDir = config.getGlobal().getAS5Config().getProfileDir();
                if( ! profileDir.exists() )
                    problems.add("src.profile is not a subdirectory in AS 5 dir: " + profileDir.getPath());
            }
        }
        // AS 7
        AS7Config as7Config = config.getGlobal().getAS7Config();
        path = as7Config.getDir();
        if( null == path )
            problems.add("dest.dir was not set.");
        else if( ! new File(path).isDirectory() )
            problems.add("dest.dir is not a directory: " + path);
        else if( ! new File(path, "jboss-modules.jar").isFile())
            problems.add("dest.dir doesn't appear to be JBoss AS 7 directory - doesn't contain jboss-modules.jar: " + path);
        else {
            String configPath = as7Config.getConfigFilePath();
            if( null == configPath )
                ; //problems.add("dest.confPath was not set."); // TODO: Put defaults to the config.
            else{
                File configFile = new File(configPath);
                if( ! configFile.exists() )
                //    problems.add(
                    log.warn("dest.conf.file is not not found in AS 7 dir: " + configFile.getPath() );
            }
        }
        
        // Management host and port
        mgmt: {
            if( as7Config.getManagementPort() == -1 ){
                problems.add("dest.mgmt doesn't contain valid port after ':'.");
                break mgmt;
            }
        
            ModelControllerClient client = null;
            try {
                client = ModelControllerClient.Factory.create(as7Config.getHost(), as7Config.getManagementPort());
                client.close();
            }
            catch( UnknownHostException ex ){
                problems.add("Can't connect to AS 7 management: " + as7Config.getHost() + ":" + as7Config.getManagementPort());
            }
            catch( IOException ex ){ } // Happens on close().
        }
        
        
        // App (deployment)
        Set<String> paths = config.getGlobal().getDeploymentsPaths();
        for( String string : paths ) {
            if( null != path && ! new File(path).exists())
                problems.add("App path was set but does not exist: " + path);
        }
        
        return problems;
    }

}// class
