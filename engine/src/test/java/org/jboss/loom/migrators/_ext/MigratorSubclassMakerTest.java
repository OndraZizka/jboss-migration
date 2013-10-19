package org.jboss.loom.migrators._ext;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class MigratorSubclassMakerTest {
    
    public MigratorSubclassMakerTest() {
    }


    @Test
    public void testCreateClass() throws Exception {
        System.out.println( "createClass" );
        String pkg = DefinitionBasedMigrator.class.getPackage().getName();
        String fullName = pkg + ".FooMigrator";
        
        Class cls = MigratorSubclassMaker.createClass( fullName );
        
        assertTrue( DefinitionBasedMigrator.class.isAssignableFrom( cls ) );
        assertEquals( fullName, cls.getName() );
        assertEquals( pkg, cls.getPackage().getName() );
        Class<? extends DefinitionBasedMigrator> migCls = cls;
    }
    
}// class