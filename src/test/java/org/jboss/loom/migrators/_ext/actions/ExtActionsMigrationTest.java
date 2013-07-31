package org.jboss.loom.migrators._ext.actions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;
import org.jboss.loom.migrators._ext.*;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.loom.TestUtils;
import org.jboss.loom.utils.ClassUtils;
import org.jboss.loom.utils.as7.AS7CliUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Test the actions defined in external files.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith( Arquillian.class )
public class ExtActionsMigrationTest extends ExtMigrationTestBase {
    private static final Logger log = LoggerFactory.getLogger( ExtActionsMigrationTest.class );
    
    //@ArquillianResource private ManagementClient mc; // ARQ-1443


    @Test @RunAsClient
    public void testManualAction() throws Exception {
        TestUtils.printTestBanner();
        doTest( "ManualActionTest", null, DirPreparation.NOOP );
    }
    
    
    /**
        <action type="copy" src="src.file" dest="destCopy.file"/>
        <action type="copy" src="srcExistingDir" dest="destExistingDir"/>
        <action type="copy" src="srcExistingDir" dest="nonExistentDir"/>
     */
    @Test @RunAsClient
    public void testCopyAction() throws Exception {
        TestUtils.printTestBanner();
        
        File dir = Files.createTempDirectory("ExtMigr-copy-").toFile();
        
        
        doTest( "CopyActionTest", dir, new DirPreparation() {
            @Override public void prepareDir( File dir ) throws IOException {
                // 1
                FileUtils.touch( new File( dir, "src.file"));
                // 2
                Files.createDirectory( new File(dir, "srcExistingDir").toPath() );
                FileUtils.touch( new File( dir, "srcExistingDir/src.file"));
                // 3 - ^^^ is reused
            }
        } );
        
        // Check
        // 1
        File file = new File(dir, "destCopy.file");
        Assert.assertTrue("src.file was copied to destCopy.file ", file.exists() );
        // 2
        //file = new File(dir, "destExistingDir/srcExistingDir/src.file");
        file = new File(dir, "destExistingDir/src.file");
        Assert.assertTrue("srcExistingDir was copied under destExistingDir", file.exists() );
        // 3
        file = new File(dir, "nonExistentDir/src.file");
        Assert.assertTrue("srcExistingDir  was copied as nonExistentDir", file.exists() );
    }
    
    /**
     *   <action type="xslt" src="src.xml" dest="dest.xml" xslt="XsltStyleSheet.xsl"/>
     */
    @Test @RunAsClient
    public void testXsltAction() throws Exception {
        TestUtils.printTestBanner();
        File dir = Files.createTempDirectory("ExtMigr-xslt-").toFile();
        
        doTest( "XsltActionTest", dir, new DirPreparation() {
            @Override public void prepareDir( File dir_ ) throws IOException {
                try( FileWriter fw = new FileWriter( new File( dir_, "src.xml") ) ){
                    fw.write("<?xml version='1.0' ?><a/>");
                }
                ClassUtils.copyResourceToDir( getClass(), "XsltStyleSheet.xsl", dir_ );
            }
        } );
        
        // Check the result
        for( String fName : new String[]{"destBasic.xml", "destIfExists.xml"} ){
            final File res = new File( dir, fName);
            Assert.assertTrue("XSLT result file exists.", res.exists() );
            Assert.assertTrue("Result contains '<b/>'.", FileUtils.readFileToString(res).contains("<b/>") );
        }
    }
    
    
    
    @Test
    public void testCliAction() throws Exception {
        TestUtils.printTestBanner();
        doTest( "CliActionTest", null, DirPreparation.NOOP );
        
        // Check the system property - /system-property=foo:read-resource
        final ModelControllerClient mcc = ModelControllerClient.Factory.create("localhost", 9999);
        // { "outcome" => "success", "result" => {"value" => "bar"} }
        ModelNode res = AS7CliUtils.executeRequest("/system-property=foo:read-resource", mcc);
        log.info("/system-property=foo:read-resource: " + res.toString());
        Assert.assertTrue("/system-property=foo:read-resource went OK", AS7CliUtils.wasSuccess(res) );
        //AS7CliUtils.exists("/system-property=foo", mcc);
    }
    
    /**
     *     <action type="module" name="org.jboss.windride.test" jar="test.jar"/>
     */
    @Test
    public void testModuleAction() throws Exception {
        TestUtils.printTestBanner();
        doTest( "ModuleCreationActionTest", null, new DirPreparation() {
            @Override public void prepareDir( File dir ) throws IOException {
                // 1
                FileUtils.touch( new File( dir, "test.jar"));
            }
        } );
        // TODO: Check that module was created and loaded.
    }
    
}// class
