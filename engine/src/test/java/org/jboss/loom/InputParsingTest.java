package org.jboss.loom;

import java.util.List;
import junit.framework.TestCase;
import static junit.framework.TestCase.fail;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.ConfigurationValidator;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class InputParsingTest extends TestCase {
    
    public void testParseBasic(){
        String[] args = new String[]{
            "src.dir=testdata/as5configs/01_510all",
            "dest.dir=testdata/as7mock",
        };
        doTest( args );
    }
    
    public void testParseWithProfile(){
        String[] args = new String[]{
            "src.dir=testdata/as5configs/01_510all",
            "src.profile=all",
            "dest.dir=testdata/as7mock",
        };
        doTest( args );
    }

    public void testParseWithProfileAndConfFile(){
        String[] args = new String[]{
            "src.dir=testdata/as5configs/01_510all",
            "src.profile=all",
            "dest.dir=testdata/as7mock",
            "dest.conf.file=standalone/configuration/standalone-foo.xml",
        };
        doTest( args );
    }

    public void testParseWithDryRun(){
        String[] args = new String[]{
            "src.dir=testdata/as5configs/01_510all",
            "src.profile=all",
            "dest.dir=testdata/as7mock",
            "dest.conf.file=standalone/configuration/standalone-foo.xml",
            "dryRun",
        };
        Configuration conf = doTest( args );
        assertTrue( conf.getGlobal().isDryRun() );
    }

    public void testParseNegative(){
        String[] args = new String[]{
            "src.dir=BAAAAAAH!",
            "dest.dir=testdata/as7mock",
        };
        doNegativeTest( args );
        
        args = new String[]{
            "src.dir=testdata/as5configs/01_510all",
            "dest.dir=WOOF!",
        };
        doNegativeTest( args );
    }
    

    private Configuration doTest( String[] args ) {
        return parseAndValidate( args );
    }
    
    private void doNegativeTest( String[] args ) {
        try {
            parseAndValidate( args );
            fail("Should have failed due to invalid arguments.");
        }
        catch( IllegalArgumentException ex ){
            // OK
        }
    }

    private Configuration parseAndValidate( String[] args ) {
        
        Configuration conf = MigratorApp.parseArguments( args );
        List<String> problems = ConfigurationValidator.validate( conf );
        
        if( !problems.isEmpty() ){
            StringBuilder sb = new StringBuilder("Parsing the arguments resulted in following errors:\n");
            for( String problem : problems )
                sb.append( problem ).append("\n");
            throw new IllegalArgumentException( sb.toString() );
        }
        
        return conf;
    }


    
}// class
