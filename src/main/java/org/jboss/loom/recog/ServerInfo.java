package org.jboss.loom.recog;

import java.io.File;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.utils.compar.ComparisonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  What did we recognize about the server; Currently just type and version.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ServerInfo {
    private static final Logger log = LoggerFactory.getLogger( ServerInfo.class );

    
    private final File serverRootDir;
    private IServerType type = null;
    private VersionRange versionRange = null;
    private ComparisonResult comparisonResult = null;

    public ServerInfo( File serverRootDir ) {
        this.serverRootDir = serverRootDir;
    }
    
    
    public String format() {
        return type.format( versionRange );
    }

    public void compareHashes() throws MigrationException {
        if( ! ( this.type instanceof HasHashes ) )
            throw new MigrationException("Comparison of file hashes is not supported for server type '" + this.type.getDescription() + "'.");
            
        if( ! versionRange.isExactVersion() )
            log.warn("Comparing hashes without knowing exact server version. May produce a lot of mismatches.");
        
        this.comparisonResult = ((HasHashes)this.type).compareHashes( versionRange.from, serverRootDir );
    }

    

    public IServerType getType() { return type; }
    public ServerInfo setType( IServerType type ) { this.type = type; return this; }
    public VersionRange getVersionRange() { return versionRange; }
    public ServerInfo setVersionRange( VersionRange versionRange ) { this.versionRange = versionRange; return this; }

    public ComparisonResult getComparisonResult() { return comparisonResult; }

}// class
