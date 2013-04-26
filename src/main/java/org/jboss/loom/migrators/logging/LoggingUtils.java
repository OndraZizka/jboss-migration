package org.jboss.loom.migrators.logging;

import org.jboss.loom.utils.AS7ModuleUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class LoggingUtils {

    public static Document createLoggingModuleXML(String moduleName, String fileName) throws ParserConfigurationException{
        
        String[] deps = new String[]{"javax.api", "org.jboss.logging", null, "org.apache.log4j"};
        return AS7ModuleUtils.createModuleXML( moduleName, fileName, deps );
    }
    
}
