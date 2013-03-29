package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.ex.MigrationException;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class CopyAction extends AbstractStatefulAction {
    
    File src;
    File dest;
    boolean overwrite;


    public CopyAction( File src, File dest, boolean overwrite ) {
        this.src = src;
        this.dest = dest;
        this.overwrite = overwrite;
    }
    
    
    @Override
    public void preValidate() throws MigrationException {
        if( ! src.exists() )
            throw new MigrationException("File to copy doesn't exist: " + src.getPath());
        if( dest.exists() && ! overwrite )
            throw new MigrationException("Copy destination exists, overwrite not allowed: " + dest.getPath());
    }


    @Override
    public void perform() throws MigrationException {
        try {
            FileUtils.copyFile( src, dest );
            setState( State.DONE );
        } catch( IOException ex ) {
            throw new MigrationException("Copying failed: " + ex.getMessage(), ex);
        }
    }


    @Override
    public void rollback() throws MigrationException {
        if( this.isAfterPerform() )
            // Replace copied file with backup.
            ;
        setState( State.ROLLED_BACK );
    }


    @Override
    public void postValidate() throws MigrationException {
        // 
    }


    @Override
    public void backup() throws MigrationException {
        // Copy dest, if exists and overwrite allowed, to a temp file.
        
        setState( State.BACKED_UP );
    }


    @Override
    public void cleanBackup() {
        // Remove temp file.
        if( ! this.isAfterBackup() )
            return;
        setState( State.FINISHED );
    }

}// class
