package org.jboss.loom.recog;

import org.jboss.loom.recog.as7.JBossAS7ServerType;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.jboss.loom.ex.MigrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ServerVersionRecognizer {
    private static final Logger log = LoggerFactory.getLogger( ServerVersionRecognizer.class );
    
    //public enum ServerType { JBOSS_AS, TOMCAT, WEBSPHERE, WEBLOGIC, GLASSFISH }; // Should rather be classes, to make it pluginable.
    
    public Class<? extends IServerType> recognizeType( File serverRootDir ) throws MigrationException{
        for( Class<? extends IServerType> typeClass : findServerTypes() ){
            IServerType type = instantiate(typeClass);
            if( type.isPresentInDir(serverRootDir) )
                return typeClass;
        }
        return null;
    }
    
    public VersionRange recognizeVersion( Class<? extends IServerType> typeClass, File serverRootDir ) throws MigrationException{
        IServerType type = instantiate( typeClass );
        return type.recognizeVersion( serverRootDir );
        // TODO: Could be called statically?
    }


    /**
     *  Finds classes implementing IServerType.
     *  Currently static.
     */
    private static Collection<Class<? extends IServerType>> findServerTypes() {
        return (List) Arrays.asList(JBossAS7ServerType.class);
    }


    /**
     *  Just wraps the potential exception.
     */
    private IServerType instantiate( Class<? extends IServerType> typeClass ) throws MigrationException {
        try {
            return typeClass.newInstance();
        } catch( InstantiationException | IllegalAccessException ex ) {
            throw new MigrationException("Failed instantiating ServerType "+typeClass.getSimpleName()+": " + ex.getMessage(), ex);
        }
    }
    
}// class
