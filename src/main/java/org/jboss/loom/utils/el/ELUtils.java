package org.jboss.loom.utils.el;


import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ELUtils {
    private static final Logger log = LoggerFactory.getLogger( ELUtils.class );


    public static void evaluateObjectMembersEL( Object obj, JuelCustomResolverEvaluator eval ) {
        
        Class curClass = obj.getClass();
        while( curClass != null  &&  ! Object.class.equals( curClass ) ){
            for( Field fld : curClass.getDeclaredFields() ){
                //if( ! fld.getType().equals( String.class ))
                if( ! String.class.isAssignableFrom(  fld.getType() ) )
                    continue;
                if( null == fld.getAnnotation( EL.class ))
                    continue;

                try {
                    String orig = (String) fld.get( obj );
                    String res = eval.evaluateEL( orig );
                    fld.set( obj, res );
                } catch( IllegalArgumentException | IllegalAccessException ex ) {
                    throw new IllegalStateException("Failed resolving EL in " + obj + ": " + ex.getMessage(), ex);
                }
            }
            curClass = curClass.getSuperclass();
        }
    }

}// class
