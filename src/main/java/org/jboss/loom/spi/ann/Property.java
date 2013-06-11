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
@Target({ElementType.FIELD, ElementType.METHOD })
public @interface Property {
    
    public String name();
    public String expr() default "";
    public String label() default "";
    public String style() default "";
    
}// class
