package org.jboss.loom.utils.el;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention( RetentionPolicy.RUNTIME)
public @interface EL {
    
    public ResolvingStage stage() default ResolvingStage.CREATION;
    
    
    public enum ResolvingStage {
        CREATION,
        BEFORE_CHILDREN // Not supported for now, but could be used e.g. for <warning> instead of hard-coded handling.
    }
    
}
