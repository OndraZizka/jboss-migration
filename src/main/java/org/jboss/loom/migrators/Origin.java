package org.jboss.loom.migrators;

import java.io.File;

/**
 *  Information about where the given piece of config data came from.
 *  Typically, it would keep a file info, but also server info, part of XML, etc.
 * 
 *  @Jira:  MIGR-109  Config (JAXB) beans to remember where did they come from.
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Origin {
    
    private String server;
    private File file;
    private String part;


    /** Currently, all the data we know come from files. */
    public Origin( File file ) {
        this.file = file;
    }

    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getServer() { return server; }
    public void setServer( String server ) { this.server = server; }
    public File getFile() { return file; }
    public void setFile( File file ) { this.file = file; }
    public String getPart() { return part; }
    public void setPart( String part ) { this.part = part; }
    //</editor-fold>
    
    
    /**
     *  For config data beans which know where did they come from.
     */
    public interface Wise {
        
        Origin getOrigin();
        
    }    
    
    
}// class
