package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.ex.ActionException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import cz.muni.fi.jboss.migration.utils.AS7ModuleUtils;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.io.FileUtils;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ModuleCreationAction extends AbstractStatefulAction {
    
    private static final String MODULE_XML_FNAME = "module.xml";

    // Action data
    File jarFile;
    String moduleName;
    String[] deps;
    boolean overwrite;
    
    // Backup
    private File moduleDir;


    public ModuleCreationAction( Class<? extends IMigrator> fromMigrator, 
            String moduleName, String[] deps, File jar, boolean overwrite)
    {
        super(fromMigrator);
        this.jarFile = jar;
        this.moduleName = moduleName;
        this.deps = deps;
        this.overwrite = overwrite;
    }


    @Override
    public String toDescription() {
        return "Create an AS 7 module '"+moduleName+"' from .jar " + this.jarFile.getPath() + ", deps: " + StringUtils.join(deps, " ");
    }
    

    @Override
    public void preValidate() throws MigrationException {
        if( ! jarFile.exists() )
            throw new ActionException(this, "Module source jar doesn't exist: " + jarFile.getPath());
        
        File dir = getModuleDir();
        if( dir.exists() && ! overwrite )
            throw new ActionException(this, "Module dir already exists in AS 7, overwrite not allowed: " + dir.getAbsolutePath());
    }


    @Override
    public void perform() throws MigrationException {
        // Create a module.
        try {
            // Copy jar file
            File dir = getModuleDir();
            FileUtils.copyFileToDirectory(this.jarFile, dir);
            //File dest = new File(dir, this.jarFile.getName());
            //FileUtils.copyFile( jarFile, dest );
            
            // XML doc
            File moduleXmlFile = new File(dir, MODULE_XML_FNAME);
            if( moduleXmlFile.exists() && ! this.overwrite )
                throw new ActionException(this, MODULE_XML_FNAME + " already exists: " + moduleXmlFile.getPath() );
            
            Document doc = AS7ModuleUtils.createModuleXML( moduleName, jarFile.getName(), deps );
            Utils.transformDocToFile( doc, moduleXmlFile );

            // Backup
            this.moduleDir = dir;
        }
        catch( IOException ex ) {
            throw new ActionException(this, "Copying failed: " + ex.getMessage(), ex);
        }
        catch( TransformerException ex ) {
            throw new ActionException(this, "Creation of " + MODULE_XML_FNAME + " failed: " + ex.getMessage(), ex);
        }
        catch( ParserConfigurationException ex ) {
            throw new ActionException(this, "Creation of " + MODULE_XML_FNAME + " failed: " + ex.getMessage(), ex);
        }

        setState(State.DONE);
    }


    @Override
    public void rollback() throws MigrationException {
        if( this.isAfterPerform() ) {
            FileUtils.deleteQuietly( this.moduleDir );
        }
        setState(State.ROLLED_BACK);
    }


    @Override
    public void postValidate() throws MigrationException {
    }


    @Override
    public void backup() throws MigrationException {
        setState(State.BACKED_UP);
    }


    @Override
    public void cleanBackup() {
        setState(State.FINISHED);
    }


    private File getModuleDir() {
        return new File( getMigrationContext().getAs7Config().getModulesDir(), this.moduleName + "/main" );
    }

}// class
