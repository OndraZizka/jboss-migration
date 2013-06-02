package org.jboss.loom.recog.as7;


import java.io.File;
import org.jboss.loom.recog.IServerType;
import org.jboss.loom.recog.VersionRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class JBossAS7ServerType implements IServerType {
    private static final Logger log = LoggerFactory.getLogger( JBossAS7ServerType.class ); 
    
    @Override public String getDescription() { return "JBoss AS 7+ or JBoss EAP 6+"; }


    @Override
    public VersionRange recognizeVersion( File homeDir ) {
        if( isPresentInDir( homeDir ) )
            return new VersionRange( "7.0.0", null );
        return new VersionRange();
    }


    @Override
    public boolean isPresentInDir( File homeDir ) {
        if( ! new File(homeDir, "jboss-modules.jar").exists() )
            return false;
        if( ! new File(homeDir, "standalone/configuration").exists() )
            return false;
        if( ! new File(homeDir, "bin/standalone.sh").exists() )
            return false;
        
        return true;
    }

}// class
