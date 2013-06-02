package org.jboss.loom.recog;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Version {

    public String version;

    public Version( String version ) {
        this.version = version;
    }
    
    public int compare( Version other ) {
        return 0; // TODO
    }

}// class
