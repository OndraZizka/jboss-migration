/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.actions;

import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.spi.IMigrator;
import java.io.File;

/**
 *  Merges two properties files - overwrites properties in dest with those from src.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class MergePropertiesFilesAction extends FileAbstractAction {
    
    
    @Override protected String verb() {
        return "Merge";
    }
    

    public MergePropertiesFilesAction( Class<? extends IMigrator> fromMigrator, File src, File dest ) {
        super( fromMigrator, src, dest );
    }


    public MergePropertiesFilesAction( Class<? extends IMigrator> fromMigrator, File src, File dest, boolean failIfNotExist ) {
        super( fromMigrator, src, dest, failIfNotExist );
    }
    
    

    @Override
    public void perform() throws MigrationException {
        throw new UnsupportedOperationException("Not yet implemented.");
        /*try {
            // TODO
            setState(State.DONE);
        } catch (IOException ex) {
            throw new ActionException(this, "Copying failed: " + ex.getMessage(), ex);
        }*/
    }
    
}// class
