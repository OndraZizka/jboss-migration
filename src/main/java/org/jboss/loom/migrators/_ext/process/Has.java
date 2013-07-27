package org.jboss.loom.migrators._ext.process;

import java.util.List;
import org.jboss.loom.actions.IMigrationAction;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface Has {
    
    public static interface Warnings {
        void addWarning( String warn );
        List<String> getWarnings();
    }
    
    public static interface Actions {
        void addAction( IMigrationAction action );
        List<IMigrationAction> getActions();
    }
    
}
