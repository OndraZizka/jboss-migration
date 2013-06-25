/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.actions;

import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.spi.ann.ActionDescriptor;

/**
 *  Migrators create this action to inform the user that a manual intervention is needed,
 *  for example when migration of certain configuration is not supported or possible.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@ActionDescriptor(
    header = "User intervention necessary"
)
public class ManualAction extends AbstractStatefulAction implements IMigrationAction {


    @Override
    public String toDescription() {
        return "Manual action:\n  "; // + StringUtils.join( getWarnings(), "\n  ");
    }

    @Override public void preValidate() throws MigrationException { }

    @Override public void backup() throws MigrationException { }

    @Override public void perform() throws MigrationException { }

    @Override public void postValidate() throws MigrationException { }

    @Override public void cleanBackup() { }

    @Override public void rollback() throws MigrationException { }
    
}
