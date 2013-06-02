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
    
    
    /**
     *  Ask all known implementations of IServerType whether their server is in the directory.
     * 
     *  TODO: Return an instance?
     */
    public static Class<? extends IServerType> recognizeType( File serverRootDir ) throws MigrationException{
        for( Class<? extends IServerType> typeClass : findServerTypes() ){
            IServerType type = instantiate(typeClass);
            if( type.isPresentInDir(serverRootDir) )
                return typeClass;
        }
        return null;
    }
    
    /**
     *  Asks given IServerType what version is in the given directory.
     *  TODO: Make method of IServerType?
     */
    public static VersionRange recognizeVersion( Class<? extends IServerType> typeClass, File serverRootDir ) throws MigrationException{
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
    private static IServerType instantiate( Class<? extends IServerType> typeClass ) throws MigrationException {
        try {
            return typeClass.newInstance();
        } catch( InstantiationException | IllegalAccessException ex ) {
            throw new MigrationException("Failed instantiating ServerType "+typeClass.getSimpleName()+": " + ex.getMessage(), ex);
        }
    }
    
}// class
