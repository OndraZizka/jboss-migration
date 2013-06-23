package org.jboss.loom.migrators._ext;

import groovy.lang.GroovyClassLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.ex.MigrationExceptions;
import org.jboss.loom.migrators._ext.MigratorDefinition.JaxbClassDef;
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
    
    private Map<String, Class<? extends IConfigFragment>> fragmentJaxbClasses = new HashMap();

    /**
     *  Reads migrator descriptors from *.mig.xml in the given dir and returns them.
     */
    public List<IMigrator> loadMigrators( File dir, GlobalConfiguration gc ) throws MigrationException {
        
        List<IMigrator> migrators = new LinkedList();
        List<Exception> problems  = new LinkedList();
        
        // For each *.mig.xml file...
        for( File xml : FileUtils.listFiles( dir, new String[]{"mig.xml"}, true ) ){
            try{
                List<MigratorDefinition> descriptors = 
                    XmlUtils.unmarshallBeans( xml, "migration/migrator", MigratorDefinition.class );
                
                // For each <migrator ...> definition...
                for( MigratorDefinition desc : descriptors ) {
                    
                    desc.fileOfOrigin = xml;
                    
                    final DefinitionBasedMigrator mig = DefinitionBasedMigrator.from( desc, this, gc );

                    // For each JAXB class...
                    for( JaxbClassDef jaxbClsBean : desc.jaxbBeansClasses ) {
                        
                        // Look up in the map:  "TestJaxbBean" -> class
                        String className = StringUtils.substringAfter( jaxbClsBean.file.getName(), "." );
                        Class cls = this.fragmentJaxbClasses.get( className );
                        if( cls == null ){
                            cls = loadGroovyClass( new File( dir, jaxbClsBean.file.getPath() ) );
                            this.fragmentJaxbClasses.put( className, cls );
                        }
                        mig.addJaxbClass( cls );
                    }
                    
                    migrators.add( mig );
                }
            }
            catch( Exception ex ){
                problems.add(ex);
            }
        }
        
        // Wrap all exceptions into one.
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
     *  @deprecated  Using loadGroovyClass directly.
     */
    private static <T> List<Class<? extends T>> loadGroovyClasses( List<File> groovyFiles, Class<? extends T> expectedSuperType ) throws MigrationException {
        
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


    private static List<File> extractGrovyFilePaths( File baseDir, List<JaxbClassDef> jaxbBeansClasses ) {
        List<File> ret = new ArrayList(jaxbBeansClasses.size());
        for( JaxbClassDef bean : jaxbBeansClasses) {
            ret.add( new File( baseDir, bean.file.getPath() ) );
        }
        return ret;
    }

}// class
