package org.jboss.loom.recog;

/**
 *  Some versions represent products.
 *  Products have own versioning scheme, but are mappable to project versions.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Version {

    public String verProject;
    public String verProduct;

    public Version( String version ) {
        this.verProject = version;
    }
    
    public Version( String projectVer, String productVer ) {
        this.verProject = projectVer;
        this.verProduct = productVer;
    }
    
    /**
     *  Auto-fills the product version by looking it up through given mapper.
     */
    public Version( String version, IProjectAndProductVersionBidiMapper mapper ) {
        this.verProject = version;
        this.verProduct = mapper.getProjectToProductVersion( version );
    }
    
    /**
     *  Compares using project version.
     */
    public int compare( Version other ) {
        return VersionComparer.compareVersions( this.verProject, other.verProject );
    }

}// class
