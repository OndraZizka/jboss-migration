package org.jboss.loom.migrators;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
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

    public boolean filterClass( Class<? extends IMigrator> next );
    
    
    
    /**
     *  Takes any migrator.
     */
    public static class All implements IMigratorFilter {
        @Override public boolean filterDefinition( MigratorDefinition def ) { return true; }
        @Override public boolean filterInstance( IMigrator def ) { return true; }
        @Override public boolean filterClass( Class<? extends IMigrator> next ) { return true; }
    }
    
        
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
        public boolean filterClass( Class<? extends IMigrator> migCls ) {
            return names.contains( migCls.getName() );
        }

        @Override
        public boolean filterInstance( IMigrator def ) {
            return true;
        }


        @Override public String toString() {
            return "Only migrators named: " + StringUtils.join( this.names, ", ");
        }
    }

    // Wildcards?
    
}// class
