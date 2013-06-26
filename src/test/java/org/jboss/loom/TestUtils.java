package org.jboss.loom;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class TestUtils {
    private static final Logger log = LoggerFactory.getLogger( TestUtils.class );


    public static void printTestBanner() {
        System.out.println( "==========================================" );
        System.out.println( "===  " + TestAppConfig.getCallingMethodName( 1 ) + "  ===" );
        System.out.println( "==========================================" );
    }

    

}// class
