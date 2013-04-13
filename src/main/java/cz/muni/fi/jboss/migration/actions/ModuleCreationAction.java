package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.utils.AS7ModuleUtils;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ModuleCreationAction extends AbstractStatefulAction {
    
    File src;
    File dest;
    Document moduleDoc;
    File moduleXml;
    boolean overwrite;


    public ModuleCreationAction( File src, File dest, Document moduleDoc, boolean overwrite ) {
        this.src = src;
        this.dest = dest;
        this.moduleDoc = moduleDoc;
        this.overwrite = overwrite;
    }
    
    @Override
    public void preValidate() throws MigrationException {
        if( ! src.exists() )
            throw new MigrationException("File to copy doesn't exist: " + src.getPath());
        if( dest.exists() && ! overwrite )
            throw new MigrationException("Copy destination exists, overwrite not allowed: " + dest.getAbsolutePath());
    }


    @Override
    public void perform() throws MigrationException {
        // Create a module.
        try {
            FileUtils.copyFile( this.src, this.dest );
            File moduleXml = new File(this.dest.getParentFile(), "module.xml");
            if( ! moduleXml.createNewFile() )
               throw new MigrationException("Creation of module.xml failed => don't have permission for writing in " +
                       "directory: " + moduleXml.getParent());

            try {
                AS7ModuleUtils.transformDocToFile( this.moduleDoc, moduleXml );
            } catch (TransformerException e) {
                throw new MigrationException("Creation of the module.xml failed: " + e.getMessage(), e);
            }
            this.moduleXml = moduleXml;

        } catch( IOException ex ) {
            throw new MigrationException("Copying failed: " + ex.getMessage(), ex);
        }
        
         setState(State.DONE);

    }


    @Override
    public void rollback() throws MigrationException {
        if( this.isAfterPerform() ){
            // TODO: For now only delete folder of created module( migration/logging and migration/driver still exist=>delete after?)
            FileUtils.deleteQuietly( this.dest.getParentFile() );
        }

        setState(State.ROLLED_BACK);

    }


    @Override
    public void postValidate() throws MigrationException {
    }


    @Override
    public void backup() throws MigrationException {
        setState( State.BACKED_UP );
    }


    @Override
    public void cleanBackup() {
        setState( State.FINISHED );
    }

}// class
