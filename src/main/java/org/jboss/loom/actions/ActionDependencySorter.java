package org.jboss.loom.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Sorts a list of actions so that dependencies go first, and depending actions go after.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ActionDependencySorter {
    
    public List<IMigrationAction> sort( List<IMigrationAction> actions ){
        
        List<IMigrationAction> ret = new ArrayList(actions);
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
                return -b;
            }
            catch( AbstractStatefulAction.CircularDependencyException ex ){
                throw new RuntimeException( ex );
            }
        }
    }
    
}// class


