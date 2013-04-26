package org.jboss.loom.actions;

import org.jboss.loom.conf.Configuration;
import org.jboss.loom.ex.ActionException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.utils.AS7ModuleUtils;
import org.jboss.loom.utils.Utils;
import org.apache.commons.io.FileUtils;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ModuleCreationAction extends AbstractStatefulAction {
    private static final Logger log = LoggerFactory.getLogger(ModuleCreationAction.class);
    
    private static final String MODULE_XML_FNAME = "module.xml";

    // Action data
    File jarFile;
    String moduleName;
    String[] deps;
    Configuration.IfExists ifExists;
    
    // Backup
    private File moduleDir;
    private File backupDir;


    public ModuleCreationAction( Class<? extends IMigrator> fromMigrator, 
            String moduleName, String[] deps, File jar, Configuration.IfExists ifExists)
    {
        super(fromMigrator);
        this.jarFile = jar;
        this.moduleName = moduleName;
        this.deps = deps;
        this.ifExists = ifExists;
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
        if( dir.exists() ){
            switch( this.ifExists ){
                case FAIL:
                    throw new ActionException(this, "Module dir already exists in AS 7, overwrite not allowed: " + dir.getAbsolutePath());
                case ASK:
                case MERGE:
                    throw new UnsupportedOperationException("ASK and MERGE are not supported for " + getClass().getSimpleName());
                case WARN:
                    log.warn("Module directory for "+this.moduleName+" already exists: " + this.moduleDir);
                    break;
                case SKIP:
                    log.debug("Module directory for "+this.moduleName+" already exists, skipping: " + this.moduleDir);
                    break;
            }
        }
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
            if( moduleXmlFile.exists() && this.ifExists != Configuration.IfExists.OVERWRITE )
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
        if( this.backupDir != null ){
            try {
                FileUtils.moveDirectory( this.backupDir, this.moduleDir );
            } catch( IOException ex ) {
                throw new ActionException( this, "Can't move " + backupDir + " to " + moduleDir );
            }
        }
        setState(State.ROLLED_BACK);
    }


    @Override
    public void postValidate() throws MigrationException {
    }


    @Override
    public void backup() throws MigrationException {
        Path tmpDir;
        try {
            tmpDir = Files.createTempDirectory( "JBossAS-migr-backup-"+moduleName );
        } catch( IOException ex ) {
            throw new ActionException( this, "Failed creating a backup dir. " + ex.getMessage(), ex);
        }
        if( getModuleDir().exists() ){
            try {
                FileUtils.copyDirectory( getModuleDir(), tmpDir.toFile() );
            } catch( IOException ex ) {
                throw new ActionException( this, "Failed copying to the backup dir " + tmpDir + " : " + ex.getMessage(), ex);
            }
        }
        this.backupDir = tmpDir.toFile();
        setState(State.BACKED_UP);
    }


    @Override
    public void cleanBackup() {
        setState(State.FINISHED);
    }


    private File getModuleDir() {
        return new File( getMigrationContext().getAs7Config().getModulesDir(), this.moduleName.replace('.', '/') + "/main" );
    }

}// class
