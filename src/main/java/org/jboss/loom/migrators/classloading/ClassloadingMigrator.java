/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.classloading;

import java.io.File;
import org.jboss.loom.actions.ManualAction;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ctx.DeploymentInfo;
import org.jboss.loom.ex.LoadMigrationException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.migrators.classloading.beans.DeploymentClassloadingConfig;
import org.jboss.loom.migrators.classloading.beans.JBossClassloadingXml;
import org.jboss.loom.migrators.classloading.beans.JBossWebXml;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;

/**
 *  For starters, we will only check for classloading files and WARN if found.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 *  @jira MIGR-56
 */
@ConfigPartDescriptor(
    name = "Classloading configuration"
)
public class ClassloadingMigrator extends AbstractMigrator implements IMigrator {


    @Override protected String getConfigPropertyModuleName() { return "classloading"; }


    public ClassloadingMigrator( GlobalConfiguration globalConfig ) {
        super( globalConfig );
    }
    


    @Override
    public void loadSourceServerConfig( MigrationContext ctx ) throws LoadMigrationException {
        
        ClassloadingConfData classloadingConfData = new ClassloadingConfData();
        
        // For each app, extract classloading conf data.
        /*for( String appPath : getGlobalConfig().getAppPaths() ){
            File app = new File(appPath);
            AppModuleClassloadingConfig modCLConf = extractClassloadingConfData( app );
            classloadingConfData.getAppClassloadingConfigs().put(app, modCLConf );
        }*/
        
        /* Instead - for now - let's just check for the presence of the files:
             EAR, myapp.ear/META-INF/jboss-classloading.xml
             WAR, myapp.war/WEB-INF/jboss-classloading.xml
             JAR, mylib.jar/META-INF/jboss-classloading.xml
           See AppConfigUtils.AppType
         */
        for( DeploymentInfo depl : ctx.getDeployments() ){
            
            DeploymentClassloadingConfig cfg = extractClassloadingConfData( depl );

            if( cfg != null ){
                classloadingConfData.getDeploymentsClassloadingConfigs().put( depl.getAsCanonicalFile(), cfg );
                classloadingConfData.setFoundSomething( true );
            }
        }
        
        if( classloadingConfData.isFoundSomething() )
            ctx.getMigrationData().put( ClassloadingMigrator.class, classloadingConfData );
    }


    
    
    @Override
    public void createActions( MigrationContext ctx ) throws MigrationException {
        
        // Cast!
        ClassloadingConfData migData = (ClassloadingConfData) ctx.getMigrationData().get( ClassloadingMigrator.class );
        if( null == migData || ! migData.isFoundSomething() ) return;
        
        // TODO: Report exactly what was found in which deployment.
        ManualAction manualAction = new ManualAction();
        manualAction.addWarning(
                "jboss-classloading.xml or/and jboss-web.xml was found in your deployments.\n"
                + "Migration of classloading configuration is not supported (yet).");
        ctx.getActions().add( manualAction );
        
    }


    /**
     *  Extracts classloading config from given Java EE archive.
     */
    private DeploymentClassloadingConfig extractClassloadingConfData( DeploymentInfo deplInfo ) {
        
        JBossClassloadingXml clXml = null;
        JBossWebXml          webXml = null;
        
        DeploymentClassloadingConfig deplCLConf = new DeploymentClassloadingConfig();
        boolean somethingFound = false;
        
        
        File infDir = deplInfo.getInfDir();

        // jboss-classloading.xml
        if( new File(infDir, "jboss-classloading.xml").exists() ){
            deplCLConf.setJbossClassloadingConf( new JBossClassloadingXml() );  // TODO: Parse.
            somethingFound = true;
        }

        // jboss-web.xml
        if( new File(infDir, "jboss-web.xml").exists() ){
            deplCLConf.setJbossWebConf( new JBossWebXml() ); // TODO: Parse.
            // TODO: jboss-web.xml may not contain classloading config. Ignore in that case.
            somethingFound = true;
        }
        
        return somethingFound ? deplCLConf : null;
    }
    
    
}// class
