package org.jboss.loom.migrators;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jboss.loom.migrators._ext.MigratorDefinition;
import org.jboss.loom.spi.IMigrator;

/**
 *  Filters migrators - used to filter out migrators based on user input and config.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public interface IMigratorFilter {
    
    boolean filterDefinition( MigratorDefinition def );
    
    boolean filterInstance( IMigrator def );
    
    
    
    /**
     *  Looks for an exact match in the set of names it keeps.
     *  Only works with definitions; returns true for all instances.
     */
    public static class ByNames implements IMigratorFilter {
        
        private final Set<String> names = new HashSet();


        public ByNames( List<String> onlyMigrators ) {
            this.names.addAll( onlyMigrators );
        }

        public Set<String> getNames() {
            return names;
        }

        @Override
        public boolean filterDefinition( MigratorDefinition def ) {
            return names.contains( def.getName() );
        }

        @Override
        public boolean filterInstance( IMigrator def ) {
            return true;
        }
        
    }

    // Wildcards?
    
}// class
