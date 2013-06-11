package org.jboss.loom.utils;

import java.util.HashMap;
import org.jboss.loom.utils.el.IExprLangEvaluator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ELUtilsTest {
    
    @Test
    public void testEvaluateEL() {
        System.out.println( "evaluateEL" );

        String greet = new IExprLangEvaluator.SimpleEvaluator().evaluateEL("Hello ${person.name} ${person.surname}, ${person.age}!", 
            new HashMap(){{
                put("person", new Person("Ondra"));
            }}
        );

        //assertEquals( "Hello  , !", greet );
        //assertEquals( "Hello Ondra Ondra, Ondra!", greet );
        assertEquals( "Hello Ondra , 19!", greet );
    }


    @Test
    public void testEvaluateEL2() {
        System.out.println( "evaluateEL2" );

        String greet = new IExprLangEvaluator.SimpleEvaluator().evaluateEL("Hello ${person}!", 
            new HashMap(){{
                put("person", new Person("Ondra"));
            }}
        );

        assertEquals( "Hello Ondra!", greet );
    }
    
    public static class Person {
        String name;
        public Person( String name ) { this.name = name; }
        public String getName(){ return this.name; }
        public int getAge(){ return 19; }
        
        @Override public String toString() {
            return this.name;
        }
    }
    
}// class