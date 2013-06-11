package org.jboss.loom.spi.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface ActionDescriptor {

    public String header();
    
    public Property[] props() default {};

}// class
