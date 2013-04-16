package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.ex.ActionException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class CopyAction extends AbstractStatefulAction {

    private File src;
    private File dest;
    private File temp;
    private boolean overwrite;
    private boolean failIfNotExist = true;


    public CopyAction(File src, File dest, boolean overwrite) {
        this.src = src;
        this.dest = dest;
        this.overwrite = overwrite;
    }


    public CopyAction( File src, File dest, boolean overwrite, boolean failIfNotExist) {
        this.src = src;
        this.dest = dest;
        this.overwrite = overwrite;
        this.failIfNotExist = failIfNotExist;
    }

    
    

    @Override
    public void preValidate() throws MigrationException {
        if ( ! src.exists() && failIfNotExist )
            throw new ActionException(this, "File to copy doesn't exist: " + src.getPath());
        if (dest.exists() && !overwrite)
            throw new ActionException(this, "Copy destination exists, overwrite not allowed: " + dest.getAbsolutePath());
    }


    @Override
    public void perform() throws MigrationException {
        try {
            FileUtils.copyFile(src, dest);
            setState(State.DONE);
        } catch (IOException ex) {
            throw new ActionException(this, "Copying failed: " + ex.getMessage(), ex);
        }
    }


    @Override
    public void rollback() throws MigrationException {
        if (this.isAfterPerform()) {
            if (this.overwrite && this.temp != null) {
                try {
                    FileUtils.copyFile(this.temp, this.dest);
                } catch (IOException e) {
                    throw new ActionException(this, "Restoring the previous file failed: " + e.getMessage(), e);
                }
            } else {
                FileUtils.deleteQuietly(this.dest);
            }
        }
        // Replace copied file with backup.

        setState(State.ROLLED_BACK);
    }


    @Override
    public void postValidate() throws MigrationException {
        // Empty - JRE would give IOEx if something.
    }


    @Override
    public void backup() throws MigrationException {
        // Copy dest, if exists and overwrite allowed, to a temp file.
        if (this.dest.exists() && this.overwrite) {
            try {
                this.temp = File.createTempFile(this.dest.getName(), null);
                FileUtils.copyFile(this.dest, this.temp);
            } catch (IOException ex) {
                throw new ActionException(this, "Creating a backup file failed: " + ex.getMessage(), ex);
            }
        }
        setState(State.BACKED_UP);
    }


    @Override
    public void cleanBackup() {
        // Remove temp file.
        if ((!this.isAfterBackup()) || (!this.overwrite))
            return;
        if (this.temp.exists()) {
            FileUtils.deleteQuietly(this.temp);
        }
        setState(State.FINISHED);
    }


    //<editor-fold defaultstate="collapsed" desc="hash/eq - use src and dest.">
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode( this.src );
        hash = 67 * hash + Objects.hashCode( this.dest );
        return hash;
    }
    
    
    @Override
    public boolean equals( Object obj ) {
        if( obj == null ) {
            return false;
        }
        if( getClass() != obj.getClass() ) {
            return false;
        }
        final CopyAction other = (CopyAction) obj;
        if( !Objects.equals( this.src, other.src ) ) {
            return false;
        }
        if( !Objects.equals( this.dest, other.dest ) ) {
            return false;
        }
        return true;
    }
    //</editor-fold>

}// class
