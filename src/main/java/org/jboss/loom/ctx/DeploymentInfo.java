package org.jboss.loom.ctx;

import java.io.File;
import org.apache.commons.lang.StringUtils;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AppConfigUtils;

/**
 * Info about deployment provided by user as input.
 * The deployment may contain app-scoped configuration like -ds.xml, classloading etc.
 * This class only holds info for deployment extraction. Further analysis is up to IMigrators.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class DeploymentInfo {
    
    // What user provided as parameter.
    String userProvidedPath;
    
    // EAR, WAR, JAR?
    AppConfigUtils.DeploymentType type;
    
    // Where did we extract to?
    File unzippedToTmpDirectory = null;


    public DeploymentInfo( String userProvidedPath ) {
        this.userProvidedPath = userProvidedPath;
        this.type = guessTypeFromName();
    }
    
    public AppConfigUtils.DeploymentType guessTypeFromName(){
        String suffix = StringUtils.substringAfterLast(userProvidedPath, ".");
        return AppConfigUtils.DeploymentType.from( suffix );
    }
    

    public File unzipToTmpDir() throws MigrationException {
        this.unzippedToTmpDirectory = AppConfigUtils.unzipDeployment( new File( userProvidedPath ) );
        return this.unzippedToTmpDirectory;
    }
    
    
    /**
     *  Return a directory with the extracted deployment, either original or unzipped.
     * @return 
     */
    public File getDirToScan(){
        if( this.unzippedToTmpDirectory != null )
            return this.unzippedToTmpDirectory;
        
        return new File( this.userProvidedPath );
    }
    
    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getUserProvidedPath() { return userProvidedPath; }
    public AppConfigUtils.DeploymentType getType() { return type; }
    public File getUnzippedToTmpDirectory() { return unzippedToTmpDirectory; }
    //</editor-fold>
    
}// class
