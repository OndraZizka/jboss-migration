package org.jboss.loom.migrators.mail;

import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.tools.groovy.IFoo;

public class Foo implements IFoo {
    
    String test = "Ahoj";
    
    public String foo(){
        return "Foooooooooo Action!";
    }
    
}// class