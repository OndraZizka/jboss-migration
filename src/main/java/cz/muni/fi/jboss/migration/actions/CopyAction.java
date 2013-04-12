package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.ex.MigrationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class CopyAction extends AbstractStatefulAction {
    
    File src;
    File dest;
    File temp;
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
        // Probably empty..
    }


    @Override
    public void backup() throws MigrationException {
        // Copy dest, if exists and overwrite allowed, to a temp file.
        if( this.dest.exists() && this.overwrite ){
            try {
                this.temp = File.createTempFile(this.dest.getName(), null);
                FileUtils.copyFile(this.dest, this.temp);
            } catch (IOException ex) {
                throw new MigrationException("Creating of backup file failed:" + ex.getMessage(), ex);
            }
        }
        setState( State.BACKED_UP );
    }


    @Override
    public void cleanBackup() {
        // Remove temp file.
        if( ! this.isAfterBackup() )
            return;
        if( this.temp.exists() ){
            FileUtils.deleteQuietly( this.temp );
        }
        setState( State.FINISHED );
    }

}// class
