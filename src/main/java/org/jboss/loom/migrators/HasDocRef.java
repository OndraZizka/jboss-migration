package org.jboss.loom.migrators;


/**
 *  Documentation or specification reference, a name and a link, as Strings.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public interface HasDocRef {

    String getDocRefName();
    String getDocRefLink();

}// class
