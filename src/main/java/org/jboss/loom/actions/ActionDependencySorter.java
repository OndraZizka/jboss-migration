package org.jboss.loom.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Sorts a list of actions so that dependencies go first, and depending actions go after.
 * 
 * @Jira  MIGR-104
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ActionDependencySorter {
    
    public static <T extends IMigrationAction> List<T> sort( List<T> actions ){
        
        List<T> ret = new ArrayList(actions);
        Collections.sort( ret, new DepComparator() );
        return ret;
    }
    
    static class DepComparator implements Comparator<IMigrationAction> {
        @Override
        public int compare( IMigrationAction o1, IMigrationAction o2 ) {
            try {
                int a = o1.dependsOn( o2 );
                if( a != -1 )  return -a;

                int b = o2.dependsOn( o1 );
                if( b != -1 )  return -b;
                
                //return Integer.compare( o1.hashCode(), o2.hashCode() ); // For deterministic behavior.
                return 0; // All right that was not a good idea - it breaks those from Migrators which don't set dependency yet...
            }
            catch( AbstractStatefulAction.CircularDependencyException ex ){
                throw new RuntimeException( ex );
            }
        }
    }
    
}// class


