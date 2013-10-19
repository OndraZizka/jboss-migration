/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.actions;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.jboss.loom.ex.ActionException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.spi.IMigrator;

/**
 * Creates the given dir.
 * Rollback deletes it if it did not exist previously.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class CreateDirectoryAction extends AbstractStatefulAction {

    private File dir;
    private boolean existed = false;


    public CreateDirectoryAction( File dir, Class<? extends IMigrator> fromMigrator ) {
        super( fromMigrator );
        this.dir = dir;
    }
    
    

    @Override
    public String toDescription() {
        return "Create directory " + dir.getPath();
    }


    @Override
    public void preValidate() throws MigrationException {
        
        // If it already exists, it must be a directory.
        if( dir.exists()  ){
            if( ! dir.isDirectory() )
                throw new ActionException(this, "Already exists, but is not a directory: " + dir.getPath());
            existed = true;
            return;
        }
        
        // Check if the first existing parent is writable.
        File parentFile = dir.getParentFile();
        do {
            if( null == parentFile )
                return;
                //throw new ActionException(this, "Can't create directory - no existing parent dir: " + dir.getPath());
            
            if( parentFile.exists() )
                break;
            
            parentFile = parentFile.getParentFile();
        } while( true );
        
        if( ! parentFile.canWrite() )
            throw new ActionException(this, "Can't write to the directory: " + parentFile.getPath());
    }


    @Override
    public void backup() throws MigrationException {
    }


    @Override
    public void perform() throws MigrationException {
        if( existed )
            return;
        try {
            FileUtils.forceMkdir( dir );
        } catch( IOException ex ) {
            throw new ActionException(this, "Can't create directory " + dir.getPath() + ": " + ex.getMessage(), ex);
        }
    }


    @Override
    public void postValidate() throws MigrationException {
    }


    @Override
    public void cleanBackup() {
    }


    @Override
    public void rollback() throws MigrationException {
        if( existed ) return;
        try {
            FileUtils.deleteDirectory( dir );
        } catch( IOException ex ) {
            throw new ActionException( this, "Can't rollback dir creation: " + ex.getMessage(), ex);
        }
    }
    
}// class
