package org.jboss.loom.utils.as7;

import java.io.File;
import java.io.IOException;
import org.jboss.loom.TestAppConfig;
import org.jboss.loom.conf.AS7Config;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.utils.Utils;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class AS7ModuleUtilsTest {
    

    @Test
    public void testIdentifyModuleContainingJar() throws IOException {
        System.out.println( "identifyModuleContainingJar" );
        
        Configuration conf = TestAppConfig.createTestConfig_EAP_520("production");
        AS7Config as7conf = conf.getGlobal().getAS7Config();
        
        File jar = Utils.createPath( as7conf.getModulesDir(), "com/h2database/h2/main/h2-1.3.168.jar" );
        
        String expResult = "com.h2database.h2";
        String result = AS7ModuleUtils.identifyModuleContainingJar( as7conf, jar );
        
        assertEquals( expResult, result );
    }
    
}// class