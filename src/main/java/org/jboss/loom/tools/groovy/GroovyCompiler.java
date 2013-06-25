package org.jboss.loom.tools.groovy;


import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class GroovyCompiler {
    private static final Logger log = LoggerFactory.getLogger( GroovyCompiler.class );
    
    private static final String GROOVY_CLASS = "/org/jboss/loom/tools/groovy/Foo.groovy";

    public static void compile(){

        try {
            if( 0 > 1 ){
                ClassLoader parent = GroovyCompiler.class.getClassLoader();
                GroovyClassLoader loader = new GroovyClassLoader(parent);
                Class groovyClass = loader.parseClass(new File("src/test/groovy/script/HelloWorld.groovy"));

                // let's call some method on an instance
                GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
                Object[] args = {};
                groovyObject.invokeMethod("run", args);
            }
            
            //If you have an interface you wish to use which you implement in the Groovy script you can use it as follows:

            InputStream groovyClassIS = GroovyCompiler.class.getResourceAsStream( GROOVY_CLASS );

            GroovyClassLoader gcl = new GroovyClassLoader();
            Class clazz = gcl.parseClass(groovyClassIS, "SomeClassName.groovy");
            Object obj = clazz.newInstance();
            IFoo action = (IFoo) obj;
            System.out.println( action.foo());
        }
        catch( CompilationFailedException | IOException | InstantiationException | IllegalAccessException ex ){
            log.error("Failed. " + ex.getMessage(), ex);
        }

    }
    
    public static void main( String[] args ) {
        GroovyCompiler.compile();
    }

}// class
