package cz.muni.fi.jboss.migration.utils;

import cz.muni.fi.jboss.migration.ex.CliScriptException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author Roman Jakubco
 *         Date: 2/2/13
 *         Time: 1:05 PM
 */
public class Utils {

    public static void throwIfBlank(String object, String errMsg, String name) throws CliScriptException{
        if((object == null) || (object.isEmpty())){
            throw new CliScriptException(name + errMsg) ;
        }
    }
}
