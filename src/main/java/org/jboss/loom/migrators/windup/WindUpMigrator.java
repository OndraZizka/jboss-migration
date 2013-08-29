package org.jboss.loom.migrators.windup;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jboss.loom.conf.AS5Config;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ctx.MigratorData;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.utils.compar.FileHashComparer;
import org.jboss.windup.WindupEngine;
import org.jboss.windup.WindupEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Looks for deployments in the source server,
 *  and processes them (which are not standard EAP 5 parts)  
 *  using WindUp and appends the resulting reports to WindRide's report.
 * 
 * API to use:
    * Set a WindupEnvironment
    * packageSignature string - filter of packages to scan; delimiter ‘:’
    * and call the ReportEngine 
 * 
 *  @see  https://issues.jboss.org/browse/MIGR-154
 *  @author  Ondrej Zizka, ozizka at redhat.com
 */
public class WindUpMigrator extends AbstractMigrator implements IMigrator {
    private static final Logger log = LoggerFactory.getLogger( WindUpMigrator.class );
    
    @Override protected String getConfigPropertyModuleName() { return "deployments"; }

    
    public WindUpMigrator( GlobalConfiguration globalConfig ) {
        super( globalConfig );
    }
    

    /**
     *  Looks for deployments in the source server, and processes them with WindUp.
     *  The resulting 
     */
    @Override public void loadSourceServerConfig( MigrationContext ctx ) throws MigrationException {
        
        // Structure to put info to.
        Data data = new Data();
        ctx.getMigrationData().put( WindUpMigrator.class, data );
        
        // Deployments directories.
        List<File> dirs = WindUpMigrator.readAS5DeploymentScannersDirectoriesInfo( ctx.getConf().getGlobal().getSourceServerConf() );
        
        // Prepare the matches against known files in known distributions.
        final Map<Path, FileHashComparer.MatchResult> matches = ctx.getSourceServer().getHashesComparisonResult().getMatches();
        
        // Read deployments info.
        for( File dir : dirs ){
            try {
                if( ! dir.exists() )      throw new MigrationException("Dir not found: " + dir);
                if( ! dir.isDirectory())  throw new MigrationException("Not a directory: " + dir);
                
                Collection<File> list = FileUtils.listFilesAndDirs( dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE );
                for( File file : list ){
                    // Most of the files found are expected to be skipped by this.
                    if( matches.containsKey( file ) )
                        continue;
                    
                    log.info("Detected custom deployment: " + file.getPath());
                    data.deployments.put( file, null );
                }
            }
            catch( Exception ex ) {
                throw new MigrationException("Failed scanning for deployments in "+dir+"\n    " + ex.getMessage(), ex);
            }
        }
        
    }// loadSourceServerConfig()


    
    /**
     *  Calls WindUp to process the deployments found in loadSourceServerConfig().
     */
    @Override public void createActions( MigrationContext ctx ) throws MigrationException {

        try {
            // WindUp
            WindupEnvironment windupEnv = new WindupEnvironment();
            WindupEngine windupEng = new WindupEngine( windupEnv );

            // Create a temp dir for the report dirs.
            File reportsTmpDir = Files.createTempDirectory("JBossMigration-WindUpReports-").toFile();
            reportsTmpDir.deleteOnExit();

            // For each deployment from source server...
            Data data = (Data) ctx.getMigrationData().get( WindUpMigrator.class );
            for( Map.Entry<File, File> item : data.deployments.entrySet() ) {
                File depl = item.getKey();

                File reportDir = new File(reportsTmpDir, depl.getName() );
                windupEng.processArchive( depl, reportDir );
                data.deployments.put( depl, reportDir );
            }
        }
        catch( Exception ex ){
            throw new MigrationException("Failed processing the source server deployments with WindUp: " + ex.getLocalizedMessage(), ex);
        }
    }// createActions()

    
    
    
    /**
     *  Should return all dirs in which the deployers search for deployments.
     *  Currently only returns the deployments/ directory (hard-coded). Fake as hell.
     */
    private static List<File> readAS5DeploymentScannersDirectoriesInfo( AS5Config as5 ) {
        return Collections.singletonList( as5.getDeployDir() );
    }

    
    
    /**
     *  Returns the dirs which AS scans for the deployments. Not needed, not finished.
     * 
    private static List<File> readDeploymentScannersDirectoriesInfo( ModelControllerClient aS7Client ) {
        ModelNode res = AS7CliUtils.executeRequest("/subsystem=deployment-scanner/scanner=default/:read-resource", ctx.getAS7Client() );
        return null;
    }
     */
    
    /**
     * Reads the deployments in given AS. Not needed, not finished.
     * 
     * {
        "outcome" => "success",
        "result" => [{
            "address" => [("deployment" => "jboss-as-wicket-ear-ear.ear")],
            "outcome" => "success",
            "result" => {
                "content" => [{"hash" => bytes { ... }}],
                "enabled" => true,
                "name" => "jboss-as-wicket-ear-ear.ear",
                "persistent" => true,
                "runtime-name" => "jboss-as-wicket-ear-ear.ear",
                "subdeployment" => {
                    "jboss-as-wicket-ear-ejb.jar" => undefined,
                    "jboss-as-wicket-ear-war.war" => undefined
                },
                "subsystem" => {"datasources" => undefined}
            }
        }]
    }
    private static List<File> readDeploymentScannersDirectoriesInfo( ModelControllerClient aS7Client ) {
            ModelNode res = AS7CliUtils.executeRequest("/deployment=* /:read-resource", ctx.getAS7Client() );
            
            final List<ModelNode> depls = res.hasDefined(ClientConstants.RESULT) 
                    ? res.get(ClientConstants.RESULT).asList() 
                    : Collections.<ModelNode>emptyList();
            
            for( ModelNode depl : depls ) {
                String file = depl.get("address").get("deployment").asString();
                data.deployments.add( file );
                
            }

        ModelNode res = AS7CliUtils.executeRequest("/subsystem=deployment-scanner/scanner=default/:read-resource", ctx.getAS7Client() );
        return null;
    }
    */

    
    
    
    /**
     *  Contains deployment file -> report file map.
     */
    protected class Data extends MigratorData {
        public final Map<File, File> deployments = new HashMap();
    }
    
}// class
