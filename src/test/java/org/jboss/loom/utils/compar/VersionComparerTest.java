package org.jboss.loom.utils.compar;

import org.jboss.loom.recog.VersionComparer;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class VersionComparerTest {
    private static final Logger log = LoggerFactory.getLogger( VersionComparerTest.class.getName() );
    

    @Test
    public void testMain() {
        Assert.assertTrue( doCompareVersions("2.3.0.GA", "2.4.0-SNAPSHOT", -1) );
        Assert.assertTrue( doCompareVersions("2.4.0.GA", "2.4.0-SNAPSHOT", 1) );
        Assert.assertTrue( doCompareVersions("2.4.1.GA", "2.4.0-SNAPSHOT", 1) );
        Assert.assertTrue( doCompareVersions("2.4.1", "2.4.0-SNAPSHOT", 1) );
        Assert.assertTrue( doCompareVersions("2.4.0", "2.4.0-SNAPSHOT", 1) );
        Assert.assertTrue( doCompareVersions("2.4.0", "2.4.0.GA", 0) );
        Assert.assertTrue( doCompareVersions("2.4.0.CR1", "2.4.0.GA", -1) );
        Assert.assertTrue( doCompareVersions("2.4.CR1", "2.4.0.GA", -1) );
        Assert.assertTrue( doCompareVersions("2.4.CR1", "2.4.0", -1) );
        Assert.assertTrue( doCompareVersions("2.0.0.0", "2.0.0.SNAPSHOT", 1) );
        Assert.assertTrue( doCompareVersions("2.0.0.1", "2.0.0.SNAPSHOT", -1) );
        Assert.assertTrue( doCompareVersions("2.0.0.1", "2.0.0.SP2", 1) );
        Assert.assertTrue( doCompareVersions("2.0.0.1", "2.0.SP2", 1) );
        //System.out.println("Test " + (succ ? "PASSED" : "FAILED") ) );
    }
    
    
     private static boolean doCompareVersions( String v1, String v2, int expectedSignum ){
          int compared = VersionComparer.compareVersions(v1, v2);
          log.info( String.format("Comparing versions:  %s  and  %s  produces:  %d, expected: %d%n", v1, v2, compared, expectedSignum ) );
          return Integer.signum(expectedSignum) == Integer.signum( compared );
     }
    
}// class