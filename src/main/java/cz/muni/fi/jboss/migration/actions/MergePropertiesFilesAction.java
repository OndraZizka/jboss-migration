package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import java.io.File;

/**
 *  Merges two properties files - overwrites properties in dest with those from src.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class MergePropertiesFilesAction extends FileAbstractAction {
    
    
    @Override protected String verb() {
        return "Merge";
    }
    

    public MergePropertiesFilesAction( Class<? extends IMigrator> fromMigrator, File src, File dest ) {
        super( fromMigrator, src, dest );
    }


    public MergePropertiesFilesAction( Class<? extends IMigrator> fromMigrator, File src, File dest, boolean failIfNotExist ) {
        super( fromMigrator, src, dest, failIfNotExist );
    }
    
    

    @Override
    public void perform() throws MigrationException {
        throw new UnsupportedOperationException("Not yet implemented.");
        /*try {
            // TODO
            setState(State.DONE);
        } catch (IOException ex) {
            throw new ActionException(this, "Copying failed: " + ex.getMessage(), ex);
        }*/
    }
    
}// class
