package org.jboss.loom.migrators;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.jboss.loom.ex.MigrationException;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class DeploymentConfigUtils {

    public static final String UNZIP_DIR_PREFIX = "JBossAS-MigrTmp-";
    public static final String TMP_DIR_SUFFIX = "-unzip1~~";

    
    
    public enum DeploymentType {
        
        EAR("META-INF"), WAR("META-INF"), JAR("META-INF");
        
        private final String infDir;


        private DeploymentType( String infDir ) {
            this.infDir = infDir;
        }

        /**
         *  The same as valueOf(), only case-insensitive, and returns null if no match.
         */
        public static DeploymentType from( String str ){
            try {
                return valueOf( str.toUpperCase() );
            } catch (IllegalArgumentException ex ){
                return null;
            }
        }

        
        public String getInfDir() { return infDir; }
       
    }// enum DeploymentType

    
    
    
    
    /**
     *  Unzips given zip to a temp dir.
     */
    public static File unzipDeployment( File deplZip ) throws MigrationException {
        try {
            Path tmpDir = Files.createTempDirectory( UNZIP_DIR_PREFIX + deplZip.getName() + TMP_DIR_SUFFIX );
            tmpDir.toFile().deleteOnExit();

            ZipFile zipFile = new ZipFile(deplZip);
            zipFile.extractAll( tmpDir.toFile().getPath() );
            
            return tmpDir.toFile();
        }
        catch( ZipException ex ){
            throw new MigrationException("Failed unzipping the app " + deplZip.getPath() + ": " + ex.getMessage(), ex);
        }
        catch( IOException ex ){
            throw new MigrationException("Failed creating a tmp dir for the app " + deplZip.getPath() + ": " + ex.getMessage(), ex);
        }
    }
    
    
}// class
