package org.jboss.loom.recog;


import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Relation between AS and EAP versions.
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class AsToEapMap {
    private static final Logger log = LoggerFactory.getLogger( AsToEapMap.class );

    private static final BidiMap map = new DualHashBidiMap();
    
    static {
        
        // EAP 6
        map.put("7.2.0.Final", "6.1.0");
        map.put("7.1.3.Final", "6.0.1");
        map.put("7.1.2.Final", "6.0.0");
        
        // EAP 5
        map.put("5.2.0.GA", "5.2.0");
        map.put("5.1.0.GA", "5.1.2");
        map.put("5.1.0.GA", "5.1.1");
        map.put("5.1.0.GA", "5.1.0");
        map.put("5.1.0.GA", "5.0.1");
        map.put("5.1.0.GA", "5.0.0");
        
        // Source: https://access.redhat.com/site/articles/112673
    }
    
    public static String getAsToEap(String ver){
        return (String) map.getKey( ver );
    }

    public static String getEapToAs(String ver){
        return (String) map.inverseBidiMap().getKey( ver );
    }

}// class
