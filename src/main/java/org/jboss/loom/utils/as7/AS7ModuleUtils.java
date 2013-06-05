/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.utils.as7;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/**
 * Util class for generation of module XML.
 *
 * @author Roman Jakubco
 */
public class AS7ModuleUtils {

    /**
     * Method for creating module.xml.
     *
     * @param moduleName The name of the created module.
     * @param fileName The name of the file deployed as module.
     * @returns A document representing created module.xml.
     * @throws javax.xml.parsers.ParserConfigurationException if parser cannot be initialized.
     */
    public static Document createModuleXML(String moduleName, String fileName, String[] deps) throws ParserConfigurationException {

        /**
         * Example of module xml,
         * <module xmlns="urn:jboss:module:1.1" name="com.h2database.h2">
         *     <resources>
         *         <resource-root path="h2-1.3.168.jar"/>
         *     </resources>
         *     <dependencies>
         *         <module name="javax.api"/>
         *         <module name="javax.transaction.api"/>
         *         <module name="javax.servlet.api" optional="true"/>
         *     </dependencies>
         * </module>
         */
        Document doc = createDoc();

        Element root = doc.createElement("module");
        doc.appendChild(root);

        root.setAttribute("xmlns", "urn:jboss:module:1.1");
        root.setAttribute("name", moduleName);

        Element resources = doc.createElement("resources");
        root.appendChild(resources);

        Element resource = doc.createElement("resource-root");
        resource.setAttribute("path", fileName);
        resources.appendChild(resource);

        // Dependencies
        Element dependencies = doc.createElement("dependencies");

        boolean optional = false;
        for( String modName : deps ) {
            if( modName == null ){
                optional = true;
                continue;
            }
            Element module = doc.createElement("module");
            module.setAttribute("name", modName);
            if( optional )
                module.setAttribute("optional", "true");
            dependencies.appendChild(module);
            optional = false;
        }

        root.appendChild(dependencies);

        return doc;
    }



    public static File transformDocToFile(Document doc, File file) throws TransformerException {
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform( new DOMSource(doc), new StreamResult(file));

        return file;
    }


    private static Document createDoc() throws ParserConfigurationException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setIgnoringComments(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();

        Document doc = builder.getDOMImplementation().createDocument(null, null, null);
        return doc;
    }

}// class