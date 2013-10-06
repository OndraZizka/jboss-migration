package org.jboss.loom.migrators.windup;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jboss.loom.conf.AS5Config;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ctx.MigratorData;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.ex.MigrationExceptions;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.utils.PathUtils;
import org.jboss.loom.utils.ZipUtils;
import org.jboss.loom.utils.compar.FileHashComparer;
import org.jboss.windup.WindupEngine;
import org.jboss.windup.WindupEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Looks for deployments in the source server,
 *  and processes them (those which are not standard EAP 5 parts)  
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
        List<File> deployDirs = WindUpMigrator.readAS5DeploymentScannersDirectoriesInfo( ctx.getConf().getGlobal().getSourceServerConf() );
        
        String serverDir = ctx.getConf().getGlobal().getSourceServerConf().getDir();
        
        // Prepare the matches against known files in known distributions.
                // Match's key's path is currently e.g. "server/standard/deploy/iiop-service.xml => MATCH".
        final Map<Path, FileHashComparer.MatchResult> matches = ctx.getSourceServer().getHashesComparisonResult().getMatches();
        
        // For each deployments directory...
        for( File dir : deployDirs ){
            try {
                if( ! dir.exists() )      throw new MigrationException("Dir not found: " + dir);
                if( ! dir.isDirectory())  throw new MigrationException("Not a directory: " + dir);
                
                // If the deploy dir is under the server root dir, get the sub-root path.
                File deplDirSubPath = PathUtils.cutOffSuperDir( serverDir, dir );
                if( deplDirSubPath == null ){
                    log.debug("Deploy dir is outside server root: " + deplDirSubPath);
                }
                
                //Collection<File> list = FileUtils.listFilesAndDirs( dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE );
                //List<String> list = Arrays.asList( dir.list() );
                List<File> deployments = listDeploymentsInDir( dir );
                
                for( File deplPath : deployments ){
                                        
                    if( deplDirSubPath != null ){
                        // Subpath under the deployment dir.
                        final File deplFileSubSubPath = PathUtils.cutOffSuperDir( dir, deplPath.getPath() );
                        final File deplFileSubPath = new File( deplDirSubPath, deplFileSubSubPath.getPath() );
                        // Most of the files found are expected to be skipped by this.
                        if( matches.containsKey( deplFileSubPath.toPath() ) ){
                            log.info("Deployment is known part of the source server, skipping: " + deplFileSubSubPath.getPath() );
                            continue;
                        }
                    }
                    
                    log.info("Detected custom deployment: " + deplPath);
                    // Storing the whole path as key.
                    // The value will be a HTML report dir, added later.
                    data.deployments.put( new Data.DeplDataItem( deplPath.toString() ), null );
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

            List<Exception> problems = new LinkedList();

            // For each deployment from source server...
            Data data = (Data) ctx.getMigrationData().get( WindUpMigrator.class );
            for( Map.Entry<Data.DeplDataItem, File> item : data.deployments.entrySet() ) {
                final Data.DeplDataItem deplOrig = item.getKey(); // A path - e.g. EAP-520/server/production/deploy/httpha-invoker.sar
                
                File depl = deplOrig;
                
                // If it's a directory, zip it first. WindUp can't process directories. https://github.com/windup/windup/issues/67
                if( deplOrig.isDirectory() ){
                    depl = ZipUtils.zipDir( deplOrig );
                }

                // TODO: Use WindUpAction instead.
                File reportDir = new File(reportsTmpDir, deplOrig.getName() );
                try {
                    windupEng.processArchive( depl, reportDir );
                    data.deployments.put( deplOrig, reportDir ); // Store the resulting report dir to the map.
                }
                catch( Exception ex ){
                    problems.add( new MigrationException("Failed processing deployment with WindUp: " + deplOrig.getPath()
                            + "\n    " + ex.getLocalizedMessage(), ex) );
                }
            }
            if( ! problems.isEmpty() ){
                throw new MigrationExceptions("Failed processing the source server deployments with WindUp", problems);
            }
        }
        catch( Exception ex ){
            throw new MigrationException("Failed processing the source server deployments with WindUp:\n    " + ex.getLocalizedMessage(), ex);
        }
    }// createActions()

    
    
    
    /**
     *  Should return all dirs in which the deployers search for deployments.
     *  Currently only returns the deployments/ directory (hard-coded). Fake as hell.
     */
    private static List<File> readAS5DeploymentScannersDirectoriesInfo( AS5Config as5 ) {
        return Collections.singletonList( as5.getDeployDir() );
    }




    private List<File> listDeploymentsInDir( File dir ) throws IOException {
        return new DeploymentWalker().scan( dir );
    }

    static class DeploymentWalker extends DirectoryWalker {
        @Override protected boolean handleDirectory( File directory, int depth, Collection results ) throws IOException {
            
            // If not a deployment, recurse into this dir.
            if( ! DEPLOYMENT_SUFFIX_FILTER.accept( directory ) )
                return true;
            // Else, add it to the found deployments and don't recurse.
            results.add( directory );
            return false;
        }

        @Override protected void handleFile( File file, int depth, Collection results ) throws IOException {
            // If it's a  deployment, add it.
            if( DEPLOYMENT_SUFFIX_FILTER.accept( file ) )
                results.add( file );
        }
        
        public List<File> scan( File dir ) throws IOException {
            List<File> depls = new LinkedList();
            this.walk( dir, depls );
            return depls;
        }
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
    protected static class Data extends MigratorData {
        public final Map<Data.DeplDataItem, File> deployments = new HashMap();

        @Override public <T extends IConfigFragment> List<T> getConfigFragments() {
            return new ArrayList( deployments.keySet() );
        }
        
        /** Just overriding File. TBD: Make File a member, not base. */
        protected static class DeplDataItem extends File implements IConfigFragment {
            public DeplDataItem( String pathname ) {
                super( pathname );
            }
            public DeplDataItem( File parent, String child ) {
                super( parent, child );
            }
        }
    }
    
    private final static FileFilter DEPLOYMENT_SUFFIX_FILTER = 
        new SuffixFileFilter(new String[]{"war","ear","jar","sar","har", "rar"});
    
}// class
