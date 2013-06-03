package org.jboss.loom.recog;

/**
 *  What did we recognize about the server; Currently just type and version.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ServerInfo {

    private IServerType type;
    
    private VersionRange versionRange;


    public String format() {
        return type.format( versionRange );
    }

    

    public IServerType getType() { return type; }
    public ServerInfo setType( IServerType type ) { this.type = type; return this; }
    public VersionRange getVersionRange() { return versionRange; }
    public ServerInfo setVersionRange( VersionRange versionRange ) { this.versionRange = versionRange; return this; }
    

}// class
