/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.transactions;

import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.LoadMigrationException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class TransactionsMigrator extends AbstractMigrator {


    @Override  protected String getConfigPropertyModuleName() { return "transactions"; }


    public TransactionsMigrator( GlobalConfiguration globalConfig ) {
        super( globalConfig );
    }
    
    
    


    @Override
    public void loadSourceServerConfig( MigrationContext ctx ) throws LoadMigrationException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void createActions( MigrationContext ctx ) throws MigrationException {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
}// class
