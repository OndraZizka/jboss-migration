/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.actions;

import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.jboss.loom.ex.ActionException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.spi.ann.ActionDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@ActionDescriptor(
    header = "Copy file"
)
public class CopyFileAction extends FileAbstractAction {
    private static final Logger log = LoggerFactory.getLogger(CopyFileAction.class);

    private boolean overwrite;
   
    public enum IfExists {
        OVERWRITE, SKIP, WARN, FAIL
    }
    
    private IfExists ifExists = IfExists.FAIL;


    public CopyFileAction(Class<? extends IMigrator> fromMigrator, File src, File dest, IfExists ifExists) {
        super( fromMigrator, src, dest);
        this.ifExists = ifExists;
    }


    public CopyFileAction(Class<? extends IMigrator> fromMigrator, File src, File dest, IfExists ifExists, boolean failIfNotExist) {
        super( fromMigrator, src, dest, failIfNotExist );
        this.ifExists = ifExists;
    }

    public CopyFileAction(Class<? extends IMigrator> fromMigrator, String pathMask, File baseDir, File dest, IfExists ifExists, boolean failIfNotExist) {
        super( fromMigrator, pathMask, baseDir, dest, failIfNotExist );
        this.ifExists = ifExists;
    }

    
    @Override protected String verb() {
        return "Copy";
    }
    
    @Override
    public String addToDescription() {
        return "if exists - " + this.ifExists.name().toLowerCase() + ", ";
    }
    
    
    
    @Override
    public void preValidate() throws MigrationException {
        if( src != null && ! src.exists() && failIfNotExist )
            throw new ActionException(this, "File to "+verb().toLowerCase()+" doesn't exist: " + src.getPath());
        if( baseDir != null && ! baseDir.exists() && failIfNotExist )
            throw new ActionException(this, "Base dir to "+verb().toLowerCase()+" from doesn't exist: " + baseDir.getPath());
        if( ! dest.exists() )
            return;
        switch( this.ifExists ){
            case OVERWRITE: return;
            case FAIL: throw new ActionException(this, ""+verb()+" destination exists, overwrite not allowed: " + dest.getAbsolutePath());
            case WARN: log.warn(""+verb()+" destination exists, skipping: " + dest.getAbsolutePath()); return;
            case SKIP: return;
        }
    }


    @Override
    public void perform() throws MigrationException {
        if( dest.exists() && this.ifExists == IfExists.SKIP )
            return;
        try {
            doPerform();
            setState(State.DONE);
        } catch (Exception ex) {
            throw new ActionException(this, "Copying failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Overridable - does the actual file operation.
     * Handles both alternatives: a single File, or pathMask + baseDir.
     */
    protected void doPerform() throws Exception {
        
        final List<File> files = this.getFiles();
        
        if( files.isEmpty() )
            this.getWarnings().add("No file found for pattern '"+this.pathMask+"' in " + this.baseDir);
        else
            for( File f : files ){
                if( src.isDirectory() )
                    FileUtils.copyDirectory( src, dest );
                else //if( src.isFile() )
                    FileUtils.copyFile( src, dest );
            }
    }
    
}// class
