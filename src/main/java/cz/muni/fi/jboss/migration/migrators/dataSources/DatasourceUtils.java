package cz.muni.fi.jboss.migration.migrators.dataSources;

import cz.muni.fi.jboss.migration.utils.AS7ModuleUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;

/** 
 * @author Roman Jakubco
 */
public class DatasourceUtils {
    
    public static Document createJDBCDriverModuleXML(String moduleName, String fileName) throws ParserConfigurationException {

        String[] deps = new String[]{"javax.api", "javax.transaction.api", null, "javax.servlet.api"};
        // Servlet API necessary only for H2 AFAIK.
        
        return AS7ModuleUtils.createModuleXML( moduleName, fileName, deps );

    }
    
}// class
