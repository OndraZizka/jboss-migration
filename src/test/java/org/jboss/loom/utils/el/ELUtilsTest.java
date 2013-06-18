package org.jboss.loom.utils.el;

import java.util.HashMap;
import java.util.Map;
import org.jboss.loom.utils.el.IExprLangEvaluator.IVariablesProvider;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ELUtilsTest {
    
    private static final Map<String, Object> PERSON_MAP_01 =
            new HashMap(){{
                put("person", new Person("Ondra"));
            }};
    
    private static final IVariablesProvider PERSON_MAP_01_VAR_PROVIDER =
        new IVariablesProvider<Object>() {
            @Override public Object getVariable( String name ) {
                return PERSON_MAP_01.get( name );
            }
        };
    

    @Test public void testSimpleEvaluator() {
        System.out.println( "SimpleEvaluator" );
        doTestBean( new IExprLangEvaluator.SimpleEvaluator(PERSON_MAP_01) );
    }


    @Test public void testSimpleEvaluator2() {
        System.out.println( "SimpleEvaluator2" );
        doTestSimple( new IExprLangEvaluator.SimpleEvaluator(PERSON_MAP_01) );
    }
    
    @Test public void testJuelCustomResolverEvaluator() {
        System.out.println("JuelCustomResolverEvaluator");
        doTestBean( new IExprLangEvaluator.JuelCustomResolverEvaluator(PERSON_MAP_01_VAR_PROVIDER) );
    }
    
    
    void doTestSimple( IExprLangEvaluator ev ){
        String greet = ev.evaluateEL("Hello ${person}!");
        assertEquals( "Hello Ondra!", greet );
    }
    
    void doTestBean( IExprLangEvaluator ev ) {
        String greet = ev.evaluateEL("Hello ${person.name} ${person.surname}, ${person.age}!");
        assertEquals( "Hello Ondra , 19!", greet );
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