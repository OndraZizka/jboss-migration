package org.jboss.loom.migrators._ext;

import java.lang.reflect.Modifier;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Programatically creates subclasses of DefinitionBasedMigrator.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class MigratorSubclassMaker {
    private static final Logger log = LoggerFactory.getLogger( MigratorSubclassMaker.class );

    public static Class<? extends DefinitionBasedMigrator> createClass( String fullName )
            throws NotFoundException, CannotCompileException
    {
        ClassPool pool = ClassPool.getDefault();
        
        // Create the class.
        CtClass cc = pool.makeClass( fullName );
        cc.setSuperclass( pool.get(DefinitionBasedMigrator.class.getName()) );
        cc.setModifiers( Modifier.PUBLIC );
        
        // Add getter which will return the migrator id (e.g. "datasources")
        
        return cc.toClass();
    }
    

}// class
