package org.jboss.loom.utils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ZipUtils {
    private static final Logger log = LoggerFactory.getLogger( ZipUtils.class );

    /**
     *  Zip given directory into a file.
     */
    public static File zipDir( File dir ) throws ZipException, IOException {
        // Get a temp path.
        Path tmp = Files.createTempFile( "WindRide-" + dir.getName(), "." + StringUtils.substringAfterLast( dir.getName(), ".") );
        log.debug("Created temp file " + tmp.toString());

        // Initiate ZipFile object with the path/name of the zip file.
        long ms = new Date().getTime();
        ZipFile zipFile = new ZipFile( tmp.toFile() );

        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);

        // Add folder to the zip file
        zipFile.addFolder( dir, parameters);
    
        return zipFile.getFile();
    }

}// class
