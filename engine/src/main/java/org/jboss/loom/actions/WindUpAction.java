package org.jboss.loom.actions;


import java.io.File;
import org.jboss.loom.ex.MigrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class WindUpAction extends AbstractStatefulAction {
    private static final Logger log = LoggerFactory.getLogger( WindUpAction.class );
    
    
    private File deployment;


    @Override
    public String toDescription() {
        return "WindUp report for " + this.deployment;
    }


    @Override
    public void preValidate() throws MigrationException {
    }


    @Override public void backup() throws MigrationException {
    }


    @Override public void perform() throws MigrationException {
        
    }


    @Override public void postValidate() throws MigrationException {
    }


    @Override public void cleanBackup() {
    }


    @Override public void rollback() throws MigrationException {
    }

}// class
