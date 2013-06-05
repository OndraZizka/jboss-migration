package org.jboss.loom.migrators.classloading;

import java.io.File;
import org.jboss.loom.*;
import org.jboss.loom.conf.AS7Config;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.utils.as7.AS7CliUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.loom.categories.EAP;
import org.jboss.loom.conf.ConfigurationValidator;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith(Arquillian.class)
public class ClassloadingTest {
    
    
    public static File createDeploymentJar() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "classloading.jar");
        jar.addAsManifestResource(new StringAsset("Manifest-Version: 1.0\n" +
                "Dependencies: org.hornetq\n"), "MANIFEST.MF");
        jar.addAsResource(ClassloadingTest.class.getPackage(), "jboss-classloading.xml", "META-INF/jboss-classloading.xml");
        File file = new File("target/classloading.jar");
        jar.as( ZipExporter.class ).exportTo( file, true );
        return file;
    }
        
    public static File createDeploymentWar() {
        WebArchive war = ShrinkWrap.create( WebArchive.class, "classloading.war");
        war.addAsResource(EmptyAsset.INSTANCE, "WEB-INF/web.xml");
        war.addAsResource(ClassloadingTest.class.getPackage(), "jboss-classloading.xml", "WEB-INF/jboss-classloading.xml");
        File file = new File("target/classloading.war");
        war.as( ZipExporter.class ).exportTo( file, true );
        return file;
    }
    
    
    /**
     *   With EAP 5.2.0. config.
     */
    @Test @Category( EAP.class )
    @RunAsClient
    public void testClassloadingMigrator( ) throws Exception {
                
        Configuration conf = TestAppConfig.createTestConfig_EAP_520("production");
        conf.getGlobal().addDeploymentPath( createDeploymentJar().getPath() );
        conf.getGlobal().addDeploymentPath( createDeploymentWar().getPath() );
        AS7Config as7Config = conf.getGlobal().getAS7Config();
        
        ModelControllerClient as7client = ModelControllerClient.Factory.create(as7Config.getHost(), as7Config.getManagementPort());
        
        // Query for the server path.
        String as7Dir = AS7CliUtils.queryServerHomeDir( as7client );
        if( as7Dir != null )  // AS 7.1.1 doesn't define it.
            conf.getGlobal().getAS7Config().setDir( as7Dir );
        
        TestAppConfig.announceMigration( conf );
        
        ConfigurationValidator.validate( conf );
        
        MigrationEngine migrator = new MigrationEngine(conf);
        migrator.doMigration();
    }
    
}// class
