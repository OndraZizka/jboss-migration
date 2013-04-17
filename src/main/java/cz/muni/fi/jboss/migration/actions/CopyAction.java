package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.ex.ActionException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class CopyAction extends FileAbstractAction {

    private boolean overwrite;


    public CopyAction(File src, File dest, boolean overwrite) {
        super(src, dest);
        this.overwrite = overwrite;
    }


    public CopyAction( File src, File dest, boolean overwrite, boolean failIfNotExist) {
        super( src, dest, failIfNotExist );
        this.overwrite = overwrite;
    }

    
    @Override protected String verb() {
        return "Merge";
    }
    
    @Override
    public String addToDescription() {
        return "may" + (this.overwrite ? "" : " not") + " overwrite, ";
    }
    
    

    @Override
    public void preValidate() throws MigrationException {
        if( ! src.exists() && failIfNotExist )
            throw new ActionException(this, "File to copy doesn't exist: " + src.getPath());
        if( src.exists() && !overwrite)
            throw new ActionException(this, "Copy destination exists, overwrite not allowed: " + dest.getAbsolutePath());
    }


    @Override
    public void perform() throws MigrationException {
        try {
            FileUtils.copyFile( src, dest );
            setState(State.DONE);
        } catch (IOException ex) {
            throw new ActionException(this, "Copying failed: " + ex.getMessage(), ex);
        }
    }

}// class
