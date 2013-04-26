package org.jboss.loom.actions;

import org.jboss.loom.ex.ActionException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.spi.IMigrator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class CopyFileAction extends FileAbstractAction {
    private static final Logger log = LoggerFactory.getLogger(CopyFileAction.class);

    private boolean overwrite;
    
    public enum IfExists {
        OVERWRITE, SKIP, WARN, FAIL
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
        return "if exists - " + this.ifExists.name().toLowerCase() + ", ";
    }
    
    

    @Override
    public void preValidate() throws MigrationException {
        if( ! src.exists() && failIfNotExist )
            throw new ActionException(this, "File to copy doesn't exist: " + src.getPath());
        if( ! dest.exists() )
            return;
        switch( this.ifExists ){
            case OVERWRITE: return;
            case FAIL: throw new ActionException(this, "Copy destination exists, overwrite not allowed: " + dest.getAbsolutePath());
            case WARN: log.warn("Copy destination exists, skipping: " + dest.getAbsolutePath()); return;
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
