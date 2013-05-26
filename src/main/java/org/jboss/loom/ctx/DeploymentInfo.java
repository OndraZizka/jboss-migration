package org.jboss.loom.ctx;

import java.io.File;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.DeploymentConfigUtils;

/**
 * Info about deployment provided by user as input.
 * The deployment may contain app-scoped configuration like -ds.xml, classloading etc.
 * This class only holds info for deployment extraction. Further analysis is up to IMigrators.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public final class DeploymentInfo {
    
    // What user provided as parameter.
    private String userProvidedPath;
    // The same as a canonical file. Used e.g. for map keys.
    private final File canonicalFile;
    
    // EAR, WAR, JAR?
    private DeploymentConfigUtils.DeploymentType type;
    
    // Where did we extract to?
    private File unzippedToTmpDirectory = null;
    
    


    public DeploymentInfo( String userProvidedPath ) throws MigrationException {
        
        // Validation
        try {
            this.canonicalFile = new File(userProvidedPath).getCanonicalFile();
        } catch( IOException | NullPointerException ex ) {
            throw new MigrationException( "Failed resolving canonical path for the deployment " 
                    + userProvidedPath + ": " + ex.getMessage(), ex );
        }
        
        this.userProvidedPath = userProvidedPath;
        this.type = guessTypeFromName();
    }
    
    public DeploymentConfigUtils.DeploymentType guessTypeFromName(){
        String suffix = StringUtils.substringAfterLast(userProvidedPath, ".");
        return DeploymentConfigUtils.DeploymentType.from( suffix );
    }
    

    public File unzipToTmpDir() throws MigrationException {
        this.unzippedToTmpDirectory = DeploymentConfigUtils.unzipDeployment( new File( userProvidedPath ) );
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
    
    public final File getAsCanonicalFile(){
        return canonicalFile;
    }
    
    
    /**
        EAR => myapp.ear/META-INF
        WAR => myapp.war/WEB-INF
        JAR => mylib.jar/META-INF
     */
    public File getInfDir(){
        return new File( getDirToScan(), this.type.getInfDir() );
    }


    // hashCode / equals delegated to userProvidedPath.
    public boolean equals( Object anObject ) {
        return userProvidedPath.equals( anObject );
    }

    public int hashCode() {
        return userProvidedPath.hashCode();
    }
    
    
    
    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getUserProvidedPath() { return userProvidedPath; }
    public DeploymentConfigUtils.DeploymentType getType() { return type; }
    public File getUnzippedToTmpDirectory() { return unzippedToTmpDirectory; }
    //</editor-fold>
    
}// class
