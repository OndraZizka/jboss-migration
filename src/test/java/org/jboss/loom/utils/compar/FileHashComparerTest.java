package org.jboss.loom.utils.compar;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.filefilter.FileFilterUtils;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class FileHashComparerTest {
    
    public FileHashComparerTest() {
    }

    private static final String STR_ORACLE_DS    =  "oracle-ds.xml";
    private static final String STR_DATASOURCES  =  "datasources.xml";
    private static final String STR_NON_EXISTENT =  "non-existent.foo";
    private static final Path PATH_ORACLE_DS    = Paths.get("testdata/singleFiles/oracle-ds.xml");
    private static final Path PATH_DATASOURCES  = Paths.get("testdata/singleFiles/datasources.xml");
    private static final Path PATH_NON_EXISTENT = Paths.get("testdata/singleFiles/non-existent.foo");

    /**
     * Test of compareHashesAndDir method, of class FileHashComparer.
     */
    @Test
    public void testCompareHashesAndDir() throws Exception {
        System.out.println( "compareHashesAndDir" );
        
        File hashes = new File("testdata/utils/crc32hashes.txt");
        File dir = new File("./testdata/singleFiles/");
        
        Map<File, FileHashComparer.MatchResult> expResult = new HashMap();
        expResult.put(hashes, FileHashComparer.MatchResult.MATCH);
        
        //long crc32 = FileHashComparer.computeCrc32(new File("testdata/singleFiles/oracle-ds.xml"));
        
        ComparisonResult results = FileHashComparer.compareHashesAndDir( hashes, dir, FileFilterUtils.trueFileFilter());
        
        assertEquals( FileHashComparer.MatchResult.MATCH,    results.getMatches().get(PATH_ORACLE_DS) );
        assertEquals( FileHashComparer.MatchResult.MISMATCH, results.getMatches().get(PATH_DATASOURCES) );
        assertEquals( FileHashComparer.MatchResult.MISSING,  results.getMatches().get(PATH_NON_EXISTENT) );
    }


    /**
     * Test of readHashFile method, of class FileHashComparer.
     */
    @Test
    public void testReadHashFile() throws Exception {
        System.out.println( "readHashFile" );
        
        File hashes = new File("testdata/utils/crc32hashes.txt");
        Map<String, Long> result = FileHashComparer.readHashes( hashes );
        
        assertEquals( new Long(0xf735fe81L), result.get(STR_ORACLE_DS) );
        assertEquals( new Long(0x10000000L), result.get(STR_DATASOURCES) );
        assertEquals( new Long(0x20000000L), result.get(STR_NON_EXISTENT) );
    }
    
}