package org.jboss.loom.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.xml.transform.TransformerException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  XSLT transformation; works like copy, only does the transformation in addition.
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class XsltAction extends CopyFileAction implements IMigrationAction {
    private static final Logger log = LoggerFactory.getLogger( XsltAction.class );

    @Override protected String verb() { return "Trasform"; }

    @Override public String addToDescription() { return ", using template " + this.xsltFile.getPath(); }

    
    // Props
    private File xsltFile;

    // Ctors
    public XsltAction( Class<? extends IMigrator> fromMigrator, File src, File xsltFile, File dest, IfExists ifExists ) {
        super( fromMigrator, src, dest, ifExists );
        this.xsltFile = xsltFile;
    }

    public XsltAction( Class<? extends IMigrator> fromMigrator, File src, File xsltFile, File dest, IfExists ifExists, boolean failIfNotExist ) {
        super( fromMigrator, src, dest, ifExists, failIfNotExist );
        this.xsltFile = xsltFile;
    }
    
    
    // Overrides
    
    @Override public void preValidate() throws MigrationException {
        super.preValidate();
        if( this.xsltFile == null )
            throw new MigrationException("XSLT template not set.");
        if( ! this.xsltFile.exists() )
            throw new MigrationException("XSLT template doesn't exist.");
        if( ! this.xsltFile.isFile())
            throw new MigrationException("XSLT template is not a file.");
    }


    /**
     *  Does the actual XSLT transformation.
     */
    @Override public void doPerform() throws TransformerException, FileNotFoundException {
        XmlUtils.transform( this.src, this.dest, new FileInputStream( this.xsltFile ));
    }

}// class
