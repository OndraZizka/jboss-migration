package org.jboss.loom.recog.as5;


import java.io.File;
import org.jboss.loom.recog.IServerType;
import org.jboss.loom.recog.VersionRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class JBossAS5ServerType implements IServerType {
    private static final Logger log = LoggerFactory.getLogger( JBossAS5ServerType.class ); 
    
    @Override public String getDescription() { return "JBoss AS 5.x or 6.x, or JBoss EAP 5.x"; }


    @Override
    public VersionRange recognizeVersion( File homeDir ) {
        if( isPresentInDir( homeDir ) )
            return new VersionRange( "5.0.0", "6" );
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
