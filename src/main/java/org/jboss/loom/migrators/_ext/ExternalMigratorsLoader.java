package org.jboss.loom.migrators._ext;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import java.io.File;
import java.io.IOException;
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
import org.jboss.loom.utils.Utils;
import org.jboss.loom.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Loads migrators externalized to *.mig.xml descriptors and *.groovy JAXB beans.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ExternalMigratorsLoader {
    private static final Logger log = LoggerFactory.getLogger( ExternalMigratorsLoader.class );

    // Migrator descriptors.
    private List<MigratorDefinition> descriptors;
    
    // Map of JAXB class name -> class - not to lead them multiple times.
    private Map<String, Class<? extends IConfigFragment>> fragmentJaxbClasses = new HashMap();
    
    // Map of migragor class name -> class
    private Map<String, Class<? extends DefinitionBasedMigrator>> migratorsClasses = new HashMap();
    

    
    /**
     *  Reads migrator descriptors from *.mig.xml in the given dir and returns them.
     * 
     *  1) Reads the definitions from the XML files.
     *  2) Loads the Groovy classes referenced in these definitions.
     *  3) Creates the classes of the Migrators.
     *  4) Instantiates the classes and returns the list of instances.
     */
    public Map<Class<? extends DefinitionBasedMigrator>, DefinitionBasedMigrator> loadMigrators( File dir, GlobalConfiguration globConf ) throws MigrationException {
        // Read the definitions from the XML files.
        this.descriptors = loadMigratorDefinitions( dir, globConf );
        
        // Load the Groovy classes referenced in these definitions.
        //this.fragmentJaxbClasses = loadJaxbClasses( this.descriptors );
        
        // Create the classes of the Migrators.
        //this.migratorsClasses = createMigratorsClasses();
        
        
        // Instantiates the classes and returns the list of instances.
        Map<Class<? extends DefinitionBasedMigrator>, DefinitionBasedMigrator> migs = instantiateMigratorsFromDefinitions( this.descriptors, this.fragmentJaxbClasses, globConf );
        return migs;
    }
    

    
    
    /**
     *  Reads migrator descriptors from *.mig.xml in the given dir and returns them.
     */
    public static List<MigratorDefinition> loadMigratorDefinitions( File dir, GlobalConfiguration gc ) throws MigrationException {
        
        List<MigratorDefinition> retDefs = new LinkedList();
        List<Exception> problems  = new LinkedList();
        
        // For each *.mig.xml file...
        for( File xmlFile : FileUtils.listFiles( dir, new String[]{"mig.xml"}, true ) ){
            try{
                List<MigratorDefinition> defs = XmlUtils.unmarshallBeans( xmlFile, "/migration/migrator", MigratorDefinition.class );
                retDefs.addAll( defs );
                // Validate
                for( MigratorDefinition def : defs ) {
                    try { Utils.validate( def ); } 
                    catch( MigrationException ex ){ problems.add( ex ); }
                }
            }
            catch( Exception ex ){
                problems.add(ex);
            }
        }
        String msg = "Errors occured when reading migrator descriptors from " + dir + ": ";
        MigrationExceptions.wrapExceptions( problems, null );
        return retDefs;
    }
    

    /**
     *  Loads the Groovy classes referenced in these definitions.
     */
    private static Map<String, Class<? extends IConfigFragment>> loadJaxbClasses(
            MigratorDefinition desc ) throws MigrationException
    {
        Map<String, Class<? extends IConfigFragment>> jaxbClasses = new HashMap();
        List<Exception> problems  = new LinkedList();
        
        // JAXB class...
        for( JaxbClassDef jaxbClsBean : desc.jaxbBeansClasses ) {
            try {
                // Look up in the map:  "TestJaxbBean" -> class
                String className = StringUtils.substringAfter( jaxbClsBean.file.getName(), "." );
                Class cls = jaxbClasses.get( className );
                if( cls == null ){
                    // Use the directory where the definition XML file is from.
                    log.debug("    Loading JAXB class from dir " + desc.getOrigin() );
                    //File dir = new File( desc.getLocation().getSystemId() ).getParentFile();
                    File dir = desc.getOrigin().getFile().getParentFile();
                    final File groovyFile = new File( dir, jaxbClsBean.file.getPath() );
                    cls = loadGroovyClass( groovyFile );
                    if( ! IConfigFragment.class.isAssignableFrom( cls ) ){
                        problems.add( new MigrationException("Groovy class from '"+groovyFile.getPath()+
                            "' doesn't implement " + IConfigFragment.class.getSimpleName() + ": " + cls.getName()));
                        continue;
                    }
                    jaxbClasses.put( className, cls );
                }
                //mig.addJaxbClass( cls );
            }
            catch( Exception ex ){
                problems.add(ex);
            }
        }
        MigrationExceptions.wrapExceptions( problems, "Failed loading JAXB classes. ");
        return jaxbClasses;
    }// loadJaxbClasses()

    
    
    /**
     *  Processes the definitions - loads the used classes and creates the migrator objects.
     */
    public static Map<Class<? extends DefinitionBasedMigrator>, DefinitionBasedMigrator> instantiateMigratorsFromDefinitions (
            List<MigratorDefinition> defs, 
            Map<String, Class<? extends IConfigFragment>> fragClasses,
            GlobalConfiguration globConf
        ) throws MigrationException
    {
        //List<Class<? extends DefinitionBasedMigrator>> migClasses = new LinkedList();
        Map<Class<? extends DefinitionBasedMigrator>, DefinitionBasedMigrator> migrators = new HashMap();
        List<Exception> problems  = new LinkedList();
        
        // For each <migrator ...> definition...
        for( MigratorDefinition migDef : defs ) {
            log.debug("Instantiating " + migDef);
            try {
                // JAXB classes.
                Map<String, Class<? extends IConfigFragment>> jaxbClasses = loadJaxbClasses( migDef );
                
                // Migrator class.
                Class<? extends DefinitionBasedMigrator> migClass = MigratorSubclassMaker.createClass( migDef.name );
                //migClasses.add( migClass );

                // Instance.
                //final DefinitionBasedMigrator mig = DefinitionBasedMigrator.from( desc, gc );
                DefinitionBasedMigrator mig = MigratorSubclassMaker.instantiate( migClass, migDef, globConf );
                migrators.put( migClass, mig );
            } catch( Exception ex ) {
                problems.add( ex );
            }
        }
        MigrationExceptions.wrapExceptions( problems, "Failed processing migrator definitions. ");
        return migrators;
    }
    

    /**
     *  Loads a groovy class from given file.
     */
    private static Class loadGroovyClass( File file ) throws MigrationException {
        try {
            //InputStream groovyClassIS = new FileInputStream( file );
            //FileReader fr = new FileReader( file );
            GroovyCodeSource src = new GroovyCodeSource( file );
            GroovyClassLoader gcl = new GroovyClassLoader();
            //Class clazz = gcl.parseClass( groovyClassIS, StringUtils.substringBefore(file.getName(),"."));
            Class clazz = gcl.parseClass( src );
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
        Utils.copyResourceToDir( ExternalMigratorsLoader.class, "TestMigrator.mig.xml", workDir );
        Utils.copyResourceToDir( ExternalMigratorsLoader.class, "TestJaxbBean.groovy",  workDir );
        
        new ExternalMigratorsLoader().loadMigrators( workDir, new GlobalConfiguration() );
    }


    private static List<File> extractGrovyFilePaths( File baseDir, List<JaxbClassDef> jaxbBeansClasses ) {
        List<File> ret = new ArrayList(jaxbBeansClasses.size());
        for( JaxbClassDef bean : jaxbBeansClasses) {
            ret.add( new File( baseDir, bean.file.getPath() ) );
        }
        return ret;
    }

}// class
