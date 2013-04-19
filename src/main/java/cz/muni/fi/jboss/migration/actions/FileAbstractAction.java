package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.ex.ActionException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public abstract class FileAbstractAction extends AbstractStatefulAction {
    
    protected File src;
    protected File dest;
    protected boolean failIfNotExist = true;
    private File temp;

    
    @Override
    public String toDescription() {
        return this.verb() + " file, " + addToDescription()
                + (this.failIfNotExist ? "" : "don't ") + "fail if exists,"
                + "\n    from " + this.src.getPath()
                + "\n      to " + this.dest.getPath();
    }
    
    protected abstract String verb();
    
    protected String addToDescription(){ return ""; }
    
    
    public FileAbstractAction(Class<? extends IMigrator> fromMigrator, File src, File dest) {
        super(fromMigrator);
        this.src = src;
        this.dest = dest;
    }


    public FileAbstractAction(Class<? extends IMigrator> fromMigrator, File src, File dest, boolean failIfNotExist) {
        super(fromMigrator);
        this.src = src;
        this.dest = dest;
        this.failIfNotExist = failIfNotExist;
    }
    

    @Override
    public void preValidate() throws MigrationException {
        if ( ! src.exists() && failIfNotExist )
            throw new ActionException(this, "File to "+ verb().toLowerCase() +" doesn't exist: " + src.getPath());
    }



    @Override
    public void rollback() throws MigrationException {
        if( ! this.isAfterPerform() )  return;
        
        // Delete the new file.
        FileUtils.deleteQuietly( this.dest );
        
        // Restore the backup file, if we created any.
        if( this.temp != null ) {
            try {
                FileUtils.moveFile(this.temp, this.dest);
            } catch (IOException ex) {
                throw new ActionException(this, "Restoring the previous file failed: " + ex.getMessage(), ex);
            }
        }
        setState(IMigrationAction.State.ROLLED_BACK);
    }


    @Override
    public void postValidate() throws MigrationException {
        // Empty - JRE would give IOEx if something.
    }


    /**
     * Copies the dest file, if it exists, to a temp file.
     */
    @Override
    public void backup() throws MigrationException {
        
        if( ! this.dest.exists() ) return;
        
        try {
            this.temp = File.createTempFile(this.dest.getName(), null);
            FileUtils.copyFile(this.dest, this.temp);
        } catch (IOException ex) {
            throw new ActionException(this, "Creating a backup file failed: " + ex.getMessage(), ex);
        }
        setState(IMigrationAction.State.BACKED_UP);
    }


    /**
     * Removes the temp file.
     */
    @Override
    public void cleanBackup() {
        
        if( this.temp == null )
            return;
        if( this.temp.exists() ) {
            FileUtils.deleteQuietly(this.temp);
        }
        setState(IMigrationAction.State.FINISHED);
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
        final FileAbstractAction other = (FileAbstractAction) obj;
        if( !Objects.equals( this.src, other.src ) ) {
            return false;
        }
        if( !Objects.equals( this.dest, other.dest ) ) {
            return false;
        }
        return true;
    }
    //</editor-fold>


    
    public File getSrc() {
        return src;
    }

    public File getDest() {
        return dest;
    }
    
}
