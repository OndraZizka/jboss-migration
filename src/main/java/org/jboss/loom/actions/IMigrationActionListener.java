package org.jboss.loom.actions;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface IMigrationActionListener<T extends IMigrationAction>{
    
    void onAction( T action );
    
}// class
