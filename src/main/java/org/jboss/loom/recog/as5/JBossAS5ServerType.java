package org.jboss.loom.recog.as5;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.jboss.loom.recog.IServerType;
import org.jboss.loom.recog.VersionRange;
import org.jboss.loom.utils.compar.FileHashComparer;
import org.jboss.loom.utils.compar.ComparisonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class JBossAS5ServerType implements IServerType {
    private static final Logger log = LoggerFactory.getLogger( JBossAS5ServerType.class );
    
    @Override public String getDescription() { return "JBoss AS 5.x or 6.x, or JBoss EAP 5.x"; }

    
    private static final String HASH_FILES_PATH = "/fileHashes/as5/";
    
    

    @Override
    public VersionRange recognizeVersion( File homeDir ) {
        if( ! isPresentInDir( homeDir ) )
            return new VersionRange();
        
        IOFileFilter filter = FileFilterUtils.suffixFileFilter(".jar");
        
        int minMismatches = Integer.MAX_VALUE;
        HashFile minMisHF = null;
        
        // Compare the directory against each hash file.
        for( HashFile hashFile : getHashFiles()) {
            try {
                InputStream is = this.getClass().getResourceAsStream( HASH_FILES_PATH + hashFile.fName );
                
                ComparisonResult result = FileHashComparer.compareHashesAndDir( is, homeDir, filter );
                log.debug("   Comparison of .jar's in %s against %s: %d of %d match.", homeDir.getPath(), hashFile.fName,
                        result.getCountMatches(), result.getCountTotal() );
                int curMismatches = result.getCountMismatches();
                if( curMismatches < minMismatches){
                     minMisHF = hashFile;
                     minMismatches = curMismatches;
                }
            }
            catch( IOException ex ) {
                throw new RuntimeException("Failed comparing dir " + homeDir.getPath() + " against hashfile " + hashFile.fName + ": " + ex.getMessage(), ex);
            }
        }
        
        // If there's some almost certain match, return that as recognized version.
        if( minMisHF != null )
            return new VersionRange( minMisHF.version, minMisHF.version );
        
        return new VersionRange( "5.0.0", "6" );
    }


    @Override
    public boolean isPresentInDir( File homeDir ) {
        if( ! new File(homeDir, "jboss-modules.jar").exists() )
            return false;
        if( ! new File(homeDir, "standalone/configuration").exists() )
            return false;
        if( ! new File(homeDir, "bin/standalone.sh").exists() )
            return false;
        
        return true;
    }

    
    private static List<HashFile> getHashFiles(){
        return Arrays.asList(
            new HashFile( "jboss-eap-5.0.1-crc32.txt", "5.0.1"),
            new HashFile( "jboss-eap-5.0.1-crc32.txt", "5.1.2"),
            new HashFile( "jboss-eap-5.0.1-crc32.txt", "5.2.0")
        );
    }
    
    // --- Structs ---
    
    static class HashFile {
        public String fName;
        public String version;
        public HashFile( String fName, String version ) {
            this.fName = fName;
            this.version = version;
        }
    }
    static class HashFileMatch{
        public HashFile hashFile;
        public Map<Path, FileHashComparer.MatchResult> matches;
    }

}// class
