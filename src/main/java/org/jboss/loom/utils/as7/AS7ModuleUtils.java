/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.utils.as7;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import org.apache.commons.lang.StringUtils;
import org.jboss.loom.actions.ModuleCreationAction.ModuleXmlInfo;
import org.jboss.loom.conf.AS7Config;
import org.jboss.loom.ex.MigrationException;

/**
 * Util class for generation of module XML.
 *
 * @author Roman Jakubco
 */
public class AS7ModuleUtils {
    
    private static final String MODULE_NS = "urn:jboss:module:1.1";

    /**
     * Creates module.xml.
     */
    public static void createModuleXML_FreeMarker( ModuleXmlInfo moduleInfo, File modFile ) throws MigrationException {
        try {
            Configuration cfg = new Configuration();
            cfg.setClassForTemplateLoading( AS7ModuleUtils.class, "/org/jboss/loom/utils/as7/" );
            cfg.setObjectWrapper( new DefaultObjectWrapper() );
            cfg.setSharedVariable("modInfo", moduleInfo);
            
            Template temp = cfg.getTemplate("module.xml.freemarker");

            Writer out = new FileWriter( modFile );
            temp.process( null, out );
            out.close();
        } catch( TemplateException  | IOException ex ) {
            throw new MigrationException("Failed creating " + modFile.getPath() + ": " + ex.getMessage(), ex);
        }
    }
    
    
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
     * 
     * @Deprecated  Screws up namespaces. I haven't found a way to fix it in JAXP. Switched to FreeMarker.
     */
    public static Document createModuleXML(String moduleName, String jarFile, String[] deps) throws ParserConfigurationException {

        Document doc = createDoc();
        //Document doc = createDoc(MODULE_NS, "module");

        Element root = doc.createElement("module");
        //Element root = doc.createElementNS("module", MODULE_NS);
        doc.appendChild(root);
        //Element root = doc.getDocumentElement();
        root.setAttribute("xmlns", MODULE_NS);
        root.setAttribute("name", moduleName);

        Element resources = doc.createElementNS("resources", null);
        root.appendChild(resources);

        Element resource = doc.createElementNS("resource-root", null);
        resource.setAttribute("path", jarFile);
        resources.appendChild(resource);

        // Dependencies
        Element dependencies = doc.createElementNS("dependencies", null);

        boolean optional = false;
        for( String modName : deps ) {
            if( modName == null ){
                optional = true;
                continue;
            }
            Element module = doc.createElementNS("module", null);
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
        return createDoc( null, null );
    }
    
    private static Document createDoc( String namespace, String rootElmName ) throws ParserConfigurationException 
    {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        // Need specifically Xerces as it treats namespaces better way.
        /*DocumentBuilderFactory domFactory;
        try {
            domFactory = (DocumentBuilderFactory) Class.forName("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl").newInstance();
        } catch( ClassNotFoundException | InstantiationException | IllegalAccessException ex ) {
            throw new IllegalStateException("JDK's DocumentBuilderFactoryImpl not found:\n    " + ex.getMessage(), ex );
        }*/
        //DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", ClassLoader.getSystemClassLoader());
        
        domFactory.setIgnoringComments(true);
        domFactory.setNamespaceAware( false );
        domFactory.setValidating( false );
        DocumentBuilder builder = domFactory.newDocumentBuilder();

        Document doc = builder.getDOMImplementation().createDocument( namespace, rootElmName, null );// rootElmName
        return doc;
    }


    /**
     *  Returns the name of the module which uses given .jar.
     *  For example, file at modules/system/layers/base/com/h2database/h2/main/h2-1.3.168.jar
     *  should return "com.h2database.h2".
     * 
     *  The current implementation is naive, assuming that the .jar file is in the module's root dir, where module.xml is.
     * 
     *  This method behavior is likely to change with various versions of EAP.
     */
    public static String identifyModuleContainingJar( AS7Config as7Config, File jar ) {
        
        String modAbsPath = as7Config.getModulesDir().getPath();
        String jarAbsPath = jar.getParentFile().getParentFile().getPath();
        
        String commonPrefix = StringUtils.getCommonPrefix( new String[]{ modAbsPath, jarAbsPath } );
        String diff = jarAbsPath.substring( commonPrefix.length() );
        
        String modName = StringUtils.removeStart( diff, "/" );
        return modName.replace('/', '.');
    }

}// class
