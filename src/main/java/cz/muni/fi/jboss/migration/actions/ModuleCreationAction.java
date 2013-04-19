package cz.muni.fi.jboss.migration.actions;

import cz.muni.fi.jboss.migration.ex.ActionException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ModuleCreationAction extends AbstractStatefulAction {
    
    private static final String MODULE_XML_FNAME = "module.xml";

    File src;
    File dest;
    Document moduleDoc;
    File moduleXml;
    boolean overwrite;


    public ModuleCreationAction( Class<? extends IMigrator> fromMigrator, File jar, File dest, Document moduleDoc, boolean overwrite) {
        super(fromMigrator);
        this.src = jar;
        this.dest = dest;
        this.moduleDoc = moduleDoc;
        this.overwrite = overwrite;
    }


    @Override
    public String toDescription() {
        return "Create an AS 7 module from .jar " + this.src.getPath() + " into " + this.dest.getParent();
    }
    

    @Override
    public void preValidate() throws MigrationException {
        if( ! src.exists() )
            throw new ActionException(this, "Module source jar doesn't exist: " + src.getPath());
        if( dest.exists() && ! overwrite )
            throw new ActionException(this, "Module jar exists in AS 7, overwrite not allowed: " + dest.getAbsolutePath());
    }


    @Override
    public void perform() throws MigrationException {
        // Create a module.
        try {
            FileUtils.copyFile(this.src, this.dest);
            File moduleXml = new File(this.dest.getParentFile(), MODULE_XML_FNAME);
            //if( ! moduleXml.createNewFile() )
            //    throw new ActionException(this, "Creation of module.xml failed - don't have write permission in " + moduleXml.getParent());
            if( moduleXml.exists() && ! this.overwrite )
                throw new ActionException(this, MODULE_XML_FNAME + " already exists: " + moduleXml.getPath() );

            Utils.transformDocToFile(this.moduleDoc, moduleXml);
            this.moduleXml = moduleXml;
        }
        catch (IOException ex) {
            throw new ActionException(this, "Copying failed: " + ex.getMessage(), ex);
        }
        catch (TransformerException e) {
            throw new ActionException(this, "Creation of " + MODULE_XML_FNAME + " failed: " + e.getMessage(), e);
        }

        setState(State.DONE);
    }


    @Override
    public void rollback() throws MigrationException {
        if (this.isAfterPerform()) {
            // TODO: For now only delete folder of created module( migration/logging and migration/driver still exist=>delete after?)
            FileUtils.deleteQuietly(this.dest.getParentFile());
        }

        setState(State.ROLLED_BACK);

    }


    @Override
    public void postValidate() throws MigrationException {
    }


    @Override
    public void backup() throws MigrationException {
        setState(State.BACKED_UP);
    }


    @Override
    public void cleanBackup() {
        setState(State.FINISHED);
    }

}// class
