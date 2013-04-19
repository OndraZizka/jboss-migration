package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.ex.ActionException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class CopyFileAction extends FileAbstractAction {

    private boolean overwrite;
    
    public enum IfExists {
        OVERWRITE, SKIP, FAIL
    }
    
    private IfExists ifExists = IfExists.FAIL;


    public CopyFileAction(Class<? extends IMigrator> fromMigrator, File src, File dest, IfExists ifExists) {
        super( fromMigrator, src, dest);
        this.ifExists = ifExists;
    }


    public CopyFileAction(Class<? extends IMigrator> fromMigrator, File src, File dest, IfExists ifExists, boolean failIfNotExist) {
        super( fromMigrator, src, dest, failIfNotExist );
        this.ifExists = ifExists;
    }

    
    @Override protected String verb() {
        return "Copy";
    }
    
    @Override
    public String addToDescription() {
        return "if exists, " + this.ifExists.name().toLowerCase();
    }
    
    

    @Override
    public void preValidate() throws MigrationException {
        if( ! src.exists() && failIfNotExist )
            throw new ActionException(this, "File to copy doesn't exist: " + src.getPath());
        if( ! dest.exists() )
            return;
        switch( this.ifExists ){
            case FAIL: throw new ActionException(this, "Copy destination exists, overwrite not allowed: " + dest.getAbsolutePath());
            case OVERWRITE: return;
            case SKIP: return;
        }
    }


    @Override
    public void perform() throws MigrationException {
        if( dest.exists() && this.ifExists == IfExists.SKIP )
            return;
        try {
            FileUtils.copyFile( src, dest );
            setState(State.DONE);
        } catch (IOException ex) {
            throw new ActionException(this, "Copying failed: " + ex.getMessage(), ex);
        }
    }

}// class
