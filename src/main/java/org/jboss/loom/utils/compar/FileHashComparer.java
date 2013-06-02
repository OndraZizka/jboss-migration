package org.jboss.loom.utils.compar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.util.CRCUtil;
import org.jboss.loom.utils.compar.ComparisonResult;
import org.slf4j.LoggerFactory;

/**
 * Compares a list of hashes with actual files in a directory tree.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class FileHashComparer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger( FileHashComparer.class );


    public static enum MatchResult { MATCH, MISMATCH, MISSING };
    
    
    /**
     *  Reads the hashes from file and compares each entry to the respective file in given base $dir.
     *  Hashes file format is:
     *       92ae740a ./bin/twiddle.bat
     */
    public static ComparisonResult compareHashesAndDir( File hashes, File dir ) throws FileNotFoundException, IOException{
        Map<Path, MatchResult> results = compareHashesAndDir( readHashFile(hashes), dir );
        return new ComparisonResult( hashes, dir ).setMatches( results );
        
    }
    
    private static Map<Path, MatchResult> compareHashesAndDir( Map<String, Long> hashes, File dir ) throws IOException {
        Map<Path, MatchResult> matches = new HashMap();
        
        // Iterate through hashes and compare with files.
        for( Map.Entry<String, Long> entry : hashes.entrySet() ) {
            String path = entry.getKey();
            Long hash = entry.getValue();
            
            File file = new File(dir, path);
            Path pathNorm = file.toPath().normalize();
            
            if( ! file.exists() ){
                matches.put( pathNorm, MatchResult.MISSING );
                continue;
            }
            long hashReal = computeCrc32(file);
            matches.put( pathNorm, hash == hashReal ? MatchResult.MATCH : MatchResult.MISMATCH );
        }
        return matches;
    }

    
    /**
     *  Reads a file format 
     *    92ae740a ./bin/twiddle.bat
     *  and returns a map of paths -> hashes.
     *  The paths are normalized, while kept relative. I.e. ./foo/../bar/a results in bar/a .
     */
    static Map<String, Long> readHashFile( File file ) throws FileNotFoundException {
        //FileReader fr = new FileReader( file );
        //FileInputStream is = new FileInputStream( file );
        Scanner sc = new Scanner( file );
        
        Map<String, Long> hashes = new HashMap();
        
        //  92ae740a ./bin/twiddle.bat
        while( sc.hasNextLine() ){
            //String line = sc.nextLine();
            //String hash = line.substring(0,8);
            //String hash = line.substring(9);
            
            try {
                long hash = sc.nextLong(16);
                String path = sc.nextLine();
                path = path.trim();
                path = Paths.get(path).normalize().toString();
                hashes.put( path, hash );
            }
            catch( NoSuchElementException ex ){
                log.warn("Failed parsing line in " + file.getPath() + ": " + sc.nextLine(), ex);
                sc.nextLine();
            }
        }
        return hashes;
    }

    
    public static long computeCrc32( File file ) throws IOException {
        try {
            return CRCUtil.computeFileCRC(file.getPath());
        } catch( ZipException ex ) {
            throw new IOException("Can't compute CRC32 of " + file.getPath() + ": " + ex.getMessage(), ex);
        }
    }
        
}// class
