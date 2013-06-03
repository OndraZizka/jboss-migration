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
    
    public String getFrom_preferProduct(){
        return this.from.verProduct != null ? this.from.verProduct : this.from.verProject;
    }
    
    public String getTo_preferProduct(){
        return this.to.verProduct != null ? this.to.verProduct : this.to.verProject;
    }
    
    public static VersionRange forProduct( String from, String to, IProjectAndProductVersionBidiMapper mapper ){
        return new VersionRange(
            new Version( mapper.getProductToProjectVersion( from ), from ),
            new Version( mapper.getProductToProjectVersion( to ),   to )
        );
    }


    public boolean isExactVersion() {
        return to == null || from.equals( to );
    }
    
}// class
