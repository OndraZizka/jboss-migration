/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.actions;

import org.jboss.loom.conf.Configuration;
import org.jboss.loom.ex.ActionException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.utils.as7.AS7ModuleUtils;
import org.jboss.loom.utils.Utils;
import org.apache.commons.io.FileUtils;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.jboss.loom.spi.ann.ActionDescriptor;
import org.jboss.loom.spi.ann.Property;
import org.jboss.loom.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@ActionDescriptor( header = "EAP 6 module creation")
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
            XmlUtils.transformDocToFile( doc, moduleXmlFile );

            // Backup
            this.moduleDir = dir;
        }
        catch( IOException ex ) {
            throw new ActionException(this, "Copying failed: " + ex.getMessage(), ex);
        }
        catch( TransformerException | ParserConfigurationException ex ) {
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
            } catch( Exception ex ) {
                throw new ActionException( this, "Can't move " + backupDir + " to " + moduleDir + ": " + ex );
            }
        }
        setState(State.ROLLED_BACK);
    }


    @Override
    public void postValidate() throws MigrationException {
    }


    @Override
    public void backup() throws MigrationException {
        if( getModuleDir().exists() ){
            Path tmpDir;
            this.moduleDir = getModuleDir();
            try {
                tmpDir = Files.createTempDirectory( "JBossAS-migr-backup-"+moduleName );
                this.backupDir = tmpDir.toFile();
            } catch( IOException ex ) {
                throw new ActionException( this, "Failed creating a backup dir. " + ex.getMessage(), ex);
            }
            
            try {
                FileUtils.copyDirectory(getModuleDir(), tmpDir.toFile() ); // Writes into.
            } catch( IOException ex ) {
                throw new ActionException( this, "Failed copying to the backup dir " + tmpDir + " : " + ex.getMessage(), ex);
            }
        }
        setState(State.BACKED_UP);
    }


    @Override
    public void cleanBackup() {
        if( this.getMigrationContext().getConf().getGlobal().isDryRun() )
                checkState( State.BACKED_UP );
        else 
                checkState( State.DONE, State.ROLLED_BACK );
        
        if( this.backupDir != null ){
            try {
                FileUtils.deleteDirectory( this.backupDir );
            } catch( IOException ex ) {
                //throw new ActionException( this, "Failed deleting the backup dir " + backupDir + " : " + ex.getMessage(), ex);
                String msg = "Failed deleting the backup dir " + backupDir + " : " + ex.getMessage();
                log.error( msg );
                this.addWarning( msg );
            }
        }
        setState(State.FINISHED);
    }


    private File getModuleDir() {
        return new File( getMigrationContext().getAs7Config().getModulesDir(), this.moduleName.replace('.', '/') + "/main" );
    }


    @Override
    public String toString() {
        return "ModuleCreationAction{ " + moduleName + " ifEx=" + ifExists + ", jar=" + jarFile + ", modDir=" + moduleDir + ", backup=" + backupDir + '}';
    }


    @Property(name = "jarFile", label = "JAR file to copy", style = "code")
    public File getJarFile() {
        return jarFile;
    }


    @Property(name = "moduleName", label = "Module name", style = "code")
    public String getModuleName() {
        return moduleName;
    }
    
}// class
