package org.jboss.loom.recog.as5;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.jboss.loom.recog.AsToEapMap;
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

    
    private static final String JAR_VERSIONS_XML = "jar-versions.xml";
    private static final String HASH_FILES_PATH = "/fileHashes/as5/";
    
    

    /**
     *  First checks jar-versions.xml. If that's not present, then compares checksums of all jars.
     * @param homeDir
     * @return 
     */
    @Override
    public VersionRange recognizeVersion( File homeDir ) {
        if( ! isPresentInDir( homeDir ) )
            return new VersionRange();
        
        // Check jar-versions.xml.
        File jvx = new File( homeDir, JAR_VERSIONS_XML );
        try {
            // Check if we know that file's CRC32; if so, use that version.
            long jarVerCrc = FileHashComparer.computeCrc32(jvx);
            String ver = getJarVersionsXmlCrcToVersionsMap().get( jarVerCrc );
            if( ver != null )
                return VersionRange.forProduct( ver, ver, new AsToEapMap() );
        } catch ( IOException ex ){
            log.error("Failed computing CRC32 of " + jvx.getPath() + ": " + ex.getMessage(), ex);
        }
        
        
        // No match - check .jars.
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
            return VersionRange.forProduct( minMisHF.version, minMisHF.version, new AsToEapMap() );
        
        // Default range - all we know - AS 5 to AS 6.
        return new VersionRange( "5.0.0", "6" );
    }


    @Override
    public boolean isPresentInDir( File homeDir ) {
        if( ! new File(homeDir, JAR_VERSIONS_XML).exists() )
            return false;
        if( ! new File(homeDir, "bin/run.sh").exists() )
            return false;
        if( ! new File(homeDir, "lib/jboss-main.jar").exists() )
            return false;
        
        return true;
    }

    
    private static List<HashFile> getHashFiles(){
        return Arrays.asList(
            new HashFile( "jboss-eap-5.0.1-crc32.txt", "5.0.1"),
            new HashFile( "jboss-eap-5.1.2-crc32.txt", "5.1.2"),
            new HashFile( "jboss-eap-5.2.0-crc32.txt", "5.2.0")
        );
    }
    
    private static Map<Long, String> getJarVersionsXmlCrcToVersionsMap(){
        Map<Long, String> map = new HashMap();
        map.put( 0x9e98373eL, "5.0.1");
        map.put( 0x10c95871L, "5.1.2");
        map.put( 0xb7414c39L, "5.2.0");
        return map;
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
