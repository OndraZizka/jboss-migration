package org.jboss.loom.migrators._ext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import org.jboss.loom.conf.GlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Programatically creates subclasses of DefinitionBasedMigrator.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 * 
 *  TODO: Perhaps extend ClassLoader?
 */
public class MigratorSubclassMaker {
    private static final Logger log = LoggerFactory.getLogger( MigratorSubclassMaker.class );

    
    static Class<? extends DefinitionBasedMigrator> createClass( String fullName )
            throws NotFoundException, CannotCompileException
    {
        ClassPool pool = ClassPool.getDefault();
        
        // Create the class.
        CtClass cc = pool.makeClass( fullName );
        final CtClass superClass = pool.get( DefinitionBasedMigrator.class.getName() );
        cc.setSuperclass( superClass );
        cc.setModifiers( Modifier.PUBLIC );
        
        // Add a constructor which will call super( ... );
        CtClass[] params = new CtClass[]{
            pool.get( MigratorDefinition.class.getName() ),
            pool.get( GlobalConfiguration.class.getName()) 
        };
        final CtConstructor ctor = new CtConstructor( params, cc );
        //ctor.setBody(" super(); ");
        ctor.setBody( superClass.getDeclaredConstructor( params ), null );
        cc.addConstructor( ctor );
        
        // Add getter which will return the migrator id (e.g. "datasources")
        
        return cc.toClass();
    }


    /**
     *  Instantiates given DefinitionBasedMigrator subclass and initializes it according to given migrator definition.
     */
    static DefinitionBasedMigrator instantiate( 
            Class<? extends DefinitionBasedMigrator> migClass, 
            MigratorDefinition def, 
            GlobalConfiguration globConf 
    )
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
    {
        Constructor<? extends DefinitionBasedMigrator> ctor = migClass.getConstructor( MigratorDefinition.class, GlobalConfiguration.class );
        if( ctor == null)
            throw new IllegalArgumentException("Class doesn't have constructor ( MigratorDefinition, GlobalConfiguration ): "
                    + migClass.getName() );
        
        DefinitionBasedMigrator mig = ctor.newInstance( def, globConf );
        return mig;
    }
    
}// class
