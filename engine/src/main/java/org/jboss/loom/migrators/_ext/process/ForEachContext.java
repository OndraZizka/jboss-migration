package org.jboss.loom.migrators._ext.process;

import java.util.Iterator;
import org.jboss.loom.migrators._ext.DefinitionBasedMigrator;
import org.jboss.loom.migrators._ext.MigratorDefinition;
import org.jboss.loom.spi.IConfigFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  ForEachContext provides the iteration over a collection of IConfigFragments (from a query result).
 *  It does *not* accept actions nor warnings.
 */
class ForEachContext implements ProcessingStackItem, /*Has.Actions, Has.Warnings,*/ Iterable<IConfigFragment> {
    private static final Logger log = LoggerFactory.getLogger( ForEachContext.class );
    
    final MigratorDefinition.ForEachDef def;
    final MigratorDefinitionProcessor processor;
    final Iterator<IConfigFragment> it;
    IConfigFragment current = null;


    ForEachContext( MigratorDefinition.ForEachDef forEachDef, MigratorDefinitionProcessor processor ) {
        this.def = forEachDef;
        this.processor = processor;
        // Initialize the iterator.
        DefinitionBasedMigrator.ConfigLoadResult queryResult = processor.defBasedMig.getQueryResultByName( this.def.queryName );
        this.it = queryResult.configFragments.iterator();
    }


    // Iterator delegation.
    @Override
    public Iterator<IConfigFragment> iterator() {
        return new Iterator<IConfigFragment>() {
            
            @Override public boolean hasNext() { return it.hasNext(); }

            @Override public IConfigFragment next() {
                ForEachContext.this.current = it.next();
                return ForEachContext.this.current;
            }

            @Override public void remove() {
                throw new UnsupportedOperationException( "Remove not supported." );
            }
        };
    }


    // getVariable()
    @Override
    public Object getVariable( String name ) {
        if( !def.variableName.equals( name ) ) {
            return null;
        }
        return this.current;
    }


    /*
    @Override
    public void addAction( IMigrationAction action ) {
        ProcessingStackItem top = processor.getStack().peek();
        if( !(top instanceof Has.Actions) ) {
            throw new IllegalArgumentException( "It's not possible to add actions to " + top );
        }
        ((Has.Actions) top).addAction( action );
    }


    @Override
    public List<IMigrationAction> getActions() {
        ProcessingStackItem top = processor.getStack().peek();
        if( !(top instanceof Has.Actions) ) {
            throw new IllegalArgumentException( "Doesn't have actions: " + top );
        }
        return ((Has.Actions) top).getActions();
    }


    @Override
    public void addWarning( String warn ) {
        ProcessingStackItem top = processor.getStack().peek();
        if( !(top instanceof Has.Warnings) ) {
            throw new IllegalArgumentException( "It's not possible to add warnings to " + top );
        }
        ((Has.Warnings) top).addWarning( warn );
    }


    @Override
    public List<String> getWarnings() {
        ProcessingStackItem top = processor.getStack().peek();
        if( !(top instanceof Has.Warnings) ) {
            throw new IllegalArgumentException( "Doesn't have warnings: " + top );
        }
        return ((Has.Warnings) top).getWarnings();
    }
    */

}// class
