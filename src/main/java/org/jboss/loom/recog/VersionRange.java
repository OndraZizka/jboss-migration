package org.jboss.loom.recog;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class VersionRange {

    public final Version from;
    public final Version to;


    public VersionRange() {
        this.from = null;
        this.to = null;
    }
    
    public VersionRange( String from, String to ) {
        this( new Version( from ), new Version( to ) );
    }
    
    public VersionRange( Version from, Version to ) {
        this.from = from;
        this.to = to;
    }
    
}// class
