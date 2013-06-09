package org.jboss.loom.spi.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigPartDescriptor {

    public String name();
    
    public String docLink() default "";
    
    public String iconFile() default "";
    public String iconOffset() default "-12px -12px";

}// class
