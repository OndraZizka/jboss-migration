package org.jboss.loom.migrators._groovy;

import groovy.lang.GroovyClassLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.ex.MigrationExceptions;
import org.jboss.loom.migrators._groovy.MigratorDescriptorBean.JaxbClass;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.utils.Utils;
import org.jboss.loom.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Loads migrators externalized to *.mig.xml descriptors and *.groovy JAXB beans.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class GroovyMigratorsLoader {
    private static final Logger log = LoggerFactory.getLogger( GroovyMigratorsLoader.class );
    
    private Map<String, Class<? extends IConfigFragment>> fragmentJaxbClasses;

    /**
     *  Reads migrator descriptors from *.mig.xml in the given dir and returns them.
     */
    public List<IMigrator> loadMigrators( File dir, GlobalConfiguration gc ) throws MigrationException {
        
        List<IMigrator> migrators = new LinkedList();
        
        List<Exception> problems = new LinkedList();
        
        // For each *.mig.xml file...
        for( File xml : FileUtils.listFiles( dir, new String[]{"mig.xml"}, true ) ){
            try{
                List<MigratorDescriptorBean> descriptors = 
                    XmlUtils.unmarshallBeans( xml, "migration/migrator", MigratorDescriptorBean.class );
                
                // For each migrator definition...
                for( MigratorDescriptorBean desc : descriptors ) {
                    
                    final DescriptorBasedMigrator mig = DescriptorBasedMigrator.from( desc, this, gc );

                    // For each JAXB class...
                    for( JaxbClass jaxbClsBean : desc.jaxbBeansClasses ) {
                        Class cls = loadGroovyClass( jaxbClsBean.file );
                        
                        // Put to a map:  "TestJaxbBean" -> class
                        String className = StringUtils.substringAfter( jaxbClsBean.file.getName(), "." );
                        this.fragmentJaxbClasses.put( className, cls );
                    }
                    
                    migrators.add( mig );
                }
            }
            catch( Exception ex ){
                problems.add(ex);
            }
        }
        if( ! problems.isEmpty() ){
            String msg = "Errors occured when reading migrator descriptors from " + dir + ": ";
            if( problems.size() == 1 )
                throw new MigrationException(msg, problems.get(0));
            else
                throw new MigrationExceptions(msg, problems);
        }
        
        return migrators;
        
    }// loadMigrators()


    /**
     *  TODO: Create a cache not to load the same classes multiple times.
     */
    private static <T> List<Class<? extends T>> loadClasses( List<File> groovyFiles, Class<? extends T> expectedSuperType ) throws MigrationException {
        
        List<Class<? extends T>> ret = new ArrayList(groovyFiles);
        for( File file : groovyFiles ) {
            Class cls = loadGroovyClass( file );
            if( ! expectedSuperType.isAssignableFrom( cls ) )
                // throw
                continue;
            ret.add( cls );
        }
        return ret;
    }


    private static Class loadGroovyClass( File file ) throws MigrationException {
        
        try {
            InputStream groovyClassIS = new FileInputStream( file );
            //FileReader fr = new FileReader( file );

            GroovyClassLoader gcl = new GroovyClassLoader();
            Class clazz = gcl.parseClass( groovyClassIS, StringUtils.substringBefore(file.getName(),"."));
            
            return clazz;
        }
        catch( IOException ex ){
            throw new MigrationException("Failed creating class from " + file.getPath() + ":\n    " + ex.getMessage(), ex);
        }
    }
    
    
    // Test
    public static void main( String[] args ) throws Exception {
        
        File workDir = new File("target/extMigrators/");
        FileUtils.forceMkdir( workDir );
        Utils.copyResourceToDir( GroovyMigratorsLoader.class, "TestMigrator.mig.xml", workDir );
        Utils.copyResourceToDir( GroovyMigratorsLoader.class, "TestJaxbBean.groovy",  workDir );
        
        new GroovyMigratorsLoader().loadMigrators( workDir, new GlobalConfiguration() );
    }


    private static List<File> extractGrovyFilePaths( File baseDir, List<JaxbClass> jaxbBeansClasses ) {
        List<File> ret = new ArrayList(jaxbBeansClasses.size());
        for( JaxbClass bean : jaxbBeansClasses) {
            ret.add( new File( baseDir, bean.file.getPath() ) );
        }
        return ret;
    }

}// class
