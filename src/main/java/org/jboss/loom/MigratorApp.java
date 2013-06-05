/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom;

import org.jboss.loom.conf.AS7Config;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.Configuration.ModuleSpecificProperty;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.ex.RollbackMigrationException;
import org.jboss.loom.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import org.jboss.loom.conf.ConfigurationValidator;


/**
 * Main class of the application.
 * Contains app-specific code, like arguments parsing, instantiating the Migrator etc.
 *
 * @author Roman Jakubco
 */
public class MigratorApp {
    
    private static final Logger log = LoggerFactory.getLogger(MigratorApp.class);
    
    
    public static void main(String[] args) {
        if (args.length == 0) {
            Utils.writeHelp();
            return;
        }
                        
        // Parse arguments.
        Configuration configuration = parseArguments( args );
        if( null == configuration )
            System.exit(1);
        
        // Apply defaults.
        applyDefaults( configuration );
        
        // MIGR-84
        if( null != System.getenv("JBOSS_HOME") );
            log.warn("JBOSS_HOME is set, might cause the migration to fail. Unset if you run in trouble.");
        
        // Validate config.
        List<String> problems = ConfigurationValidator.validate( configuration );
        if( !problems.isEmpty() ){
            for( String problem : problems )
                log.error(problem);
            System.exit(1);
        }
        
        // Migrate.
        try {
            migrate( configuration );
        } catch (MigrationException ex) {
            log.error("Migration failed (details at DEBUG level): " + ex.getMessage());
            log.debug("Migration failed: ", ex);
        }

        
    }// main()


    // TODO: Some files are declared for example in standard profile in AS5 but files which they reference are not? security web-console*

    
    
    /**
     *  Parses app's arguments.
     *  @returns  Configuration initialized according to args.
     */
    static Configuration parseArguments(String[] args) {
    
        // Global config
        GlobalConfiguration globalConfig = new GlobalConfiguration();
        
        // Module-specific options.
        List<ModuleSpecificProperty> moduleConfigs = new LinkedList<>();
        
        
        // For each argument...
        for (String arg : args) {
            arg = StringUtils.removeStart( arg, "--" );
            
            if( arg.equals("help") ){
                Utils.writeHelp();
                return null;
            }
            if( arg.startsWith("as5.dir=") || arg.startsWith("eap5.dir=") || arg.startsWith("src.dir=") ) {
                globalConfig.getAS5Config().setDir(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("dest.dir=") || arg.startsWith("eap6.dir=") || arg.startsWith("dest.dir=") || arg.startsWith("wfly.dir=") ) {
                globalConfig.getAS7Config().setDir(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("as5.profile=") || arg.startsWith("eap5.profile=") || arg.startsWith("src.profile=") ) {
                globalConfig.getAS5Config().setProfileName(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("dest.confPath=") || arg.startsWith("eap6.confPath=") || arg.startsWith("dest.conf.file=") || arg.startsWith("wfly.confPath=") ) {
                globalConfig.getAS7Config().setConfigPath(StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("dest.mgmt=") ||  arg.startsWith("eap6.mgmt=") ||  arg.startsWith("dest.mgmt=") ||  arg.startsWith("wfly.mgmt=") ) {
                parseMgmtConn( StringUtils.substringAfter(arg, "="), globalConfig.getAS7Config() );
                continue;
            }

            if( arg.startsWith("app.path=") ) {
                globalConfig.addDeploymentPath( StringUtils.substringAfter(arg, "="));
                continue;
            }

            if( arg.startsWith("valid.skip") ) {
                globalConfig.setSkipValidation(true);
                continue;
            }

            if( arg.equals("dry") || arg.equals("dryRun") || arg.equals("dry-run") ) {
                globalConfig.setDryRun(true);
                continue;
            }
            
            if( arg.equals("test") || arg.equals("testRun") || arg.equals("test-run") ) {
                globalConfig.setTestRun(true);
                continue;
            }
            
            if( arg.startsWith("report.dir=") ) {
                globalConfig.setReportDir( StringUtils.substringAfter(arg, "="));
                continue;
            }

            

            // Module-specific configurations.
            // TODO: Process by calling IMigrator instances' callback.
            if (arg.startsWith("conf.")) {
                
                // --conf.<module>.<property.name>[=<value>]
                String conf = StringUtils.substringAfter(arg, ".");
                String module = StringUtils.substringBefore(conf, ".");
                String propName = StringUtils.substringAfter(conf, ".");
                int pos = propName.indexOf('=');
                String value = null;
                if( pos == -1 ){
                    value = propName.substring(pos+1);
                    propName = propName.substring(0, pos);
                }
                
                moduleConfigs.add( new ModuleSpecificProperty(module, propName, value));
            }

            
            // Unrecognized.
            
            if( ! arg.contains("=") ){
                // TODO: Could be AS5 or AS7 dir.
            }
            
            System.err.println("Warning: Unknown argument: " + arg + " !");
            Utils.writeHelp();
            continue;
        }

        Configuration configuration = new Configuration();
        configuration.setModuleConfigs(moduleConfigs);
        configuration.setGlobalConfig(globalConfig);
        
        return configuration;
        
    }// parseArguments()

    

    /**
     *  Sets the default values.
     */
    private static void applyDefaults(Configuration configuration) {
        // TODO
    }

    
    
    /**
     *  Performs the migration.
     */
    public static void migrate( Configuration conf ) throws MigrationException {
        
        log.info("Commencing migration.");
        
        MigrationEngine migrator = new MigrationEngine(conf);

        try {
            migrator.doMigration();
            log.info("");
            log.info("Migration was successful.");
        }
        catch( Throwable ex ) {
            log.error("Migration failed. See previous messages for progress. ");
            
            if( ex instanceof RollbackMigrationException ){
                log.error("Yet, the rollback attempt failed as well. "
                        + "The server configuration may have ended up in an inconsistent state!");
                log.error("");
                RollbackMigrationException rollEx = (RollbackMigrationException) ex;
                log.error("Rollback failure cause: " + rollEx.getRollbackCause(), rollEx.getRollbackCause());
                ex = rollEx.getCause();
            }
            
            log.error("");
            log.error("Migration failure cause: " + ex, ex);
        }
        
    }// migrate()


    /**
     * @param mgmtConn   localhost:9999
     */
    private static void parseMgmtConn( String mgmtConn, AS7Config aS7Config ) {
        String host = StringUtils.substringBefore(mgmtConn, ":");
        if( ! mgmtConn.contains(":"))  return;
        
        String port = StringUtils.substringAfter(mgmtConn, ":");
        aS7Config.setHost( host );
        try {
            aS7Config.setManagementPort( Integer.parseInt( port ) );
        } catch( NumberFormatException ex ){
            aS7Config.setManagementPort( -1 );
        }
    }

    
}// class
