package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.ex.MigrationException;
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
    

    public MergePropertiesFilesAction( File src, File dest ) {
        super( src, dest );
    }


    public MergePropertiesFilesAction( File src, File dest, boolean failIfNotExist ) {
        super( src, dest, failIfNotExist );
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
