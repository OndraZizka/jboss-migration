package cz.muni.fi.jboss.migration;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Helping class for remembering files which will be copied from AS5 to AS7 so they can be deleted if app fails. Also
 * helping create modules for drivers.
 *
 * @author Roman Jakubco
 * Date: 11/12/12
 * Time: 3:13 PM
 */
public class CopyMemory {
    private String name;

    private String type;

    private String targetPath;

    private String homePath;

    // Only if it is driver
    private String module;

    // Different name for jdbc driver. For Sybase and Mssql
    private String altName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getHomePath() {
        return homePath;
    }

    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }

    public String getAltName() {
        return altName;
    }

    /**
     * Setting module for different databases.
     *
     * @return
     */
    public String driverModuleGen(){
        this.module = "jdbc.drivers.";
        if(name.contains("mysql")){
          module = module + "mysql";
        }
        // Mssql
        if(name.contains("microsoft")){
            module = module + "mssql";
        }
        if(name.contains("sybase")){
            module = module + "sybase";
        }
        if(name.contains("postgresql")){
            module = module + "postgresql";
        }
        if(name.contains("oracle")){
            module = module + "oracle";
        }
        if(name.contains("hsqldb")){
            module = module + "hsqldb";
        }
        if(name.contains("db2")){
            module = module + "db2";
        }
        if(name.contains("jtds")){
            module = module + "jtds";
        }
        //return module;
        return module;
    }

    /**
     * Setting name of the Copy Memory for drivers. In special cases altName is set for alternative JDBC driver (JTDS)
     *
     * @param name driver-class from -ds.xml file from AS5
     */
    public void setDriverName(String name){
        if(name.contains("postgres")){
            this.name = "postgresql";
            return;
        }
        if(name.contains("microsoft")){
           this.name = "sqljdbc";
           this.altName = "jtds";
            return;
        }
        if(name.contains("db2")){
            this.name = "db2";
            return;
        }
        if(name.contains("sybase")){
            this.name = "sybase";
            this.altName = "jtds";
            return;
        }
        if(name.contains("mysql")){
            this.name = "sqljdbc";
            return;
        }
        if(name.contains("oracle")){
            this.name = "ojdbc";
            return;
        }
        if(name.contains("hsqldb")){
            this.name = "hsqldb";
            return;
        }
        String temp = StringUtils.substringAfter(name, ".");
        this.name = StringUtils.substringBefore(temp, ".");
    }

    /**
     * Method for creating module.xml for JDBC drivers, which will be copied to modules in AS7
     *
     * @return  Document representing created module.xml for given driver
     * @throws ParserConfigurationException  if parser cannot be initialized
     */
    public  Document createModuleXML() throws ParserConfigurationException{

        /**
         * Example of module xml,
         *  <module xmlns="urn:jboss:module:1.1" name="com.h2database.h2">
         *       <resources>
         *          <resource-root path="h2-1.3.168.jar"/>
         *       <!-- Insert resources here -->
         *       </resources>
         *       <dependencies>
         *          <module name="javax.api"/>
         *          <module name="javax.transaction.api"/>
         *          <module name="javax.servlet.api" optional="true"/>
         *       </dependencies>
         *  </module>
         */
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setIgnoringComments(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();

        Document doc =  builder.getDOMImplementation().createDocument(null, null, null);

        Element root = doc.createElement("module");
        doc.appendChild(root);

        root.setAttribute("xmlns", "urn:jboss:module:1.1");
        root.setAttribute("module", this.getModule());

        Element resources = doc.createElement("resources");
        root.appendChild(resources);

        Element resource = doc.createElement("resource-root");
        resource.setAttribute("path", this.getName());
        resources.appendChild(resource);

        Element dependencies = doc.createElement("dependencies");
        Element module1 = doc.createElement("module");
        module1.setAttribute("name", "javax.api");
        Element module2 = doc.createElement("module");
        module2.setAttribute("name", "javax.transaction.api");
        Element module3 = doc.createElement("module");
        module3.setAttribute("name", "javax.servlet.api");
        module3.setAttribute("optional", "true");

        dependencies.appendChild(module1);
        dependencies.appendChild(module2);
        dependencies.appendChild(module3);

        root.appendChild(dependencies);

        return doc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CopyMemory)) return false;

        CopyMemory that = (CopyMemory) o;

        if (homePath != null ? !homePath.equals(that.homePath) : that.homePath != null) return false;
        if (module != null ? !module.equals(that.module) : that.module != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (targetPath != null ? !targetPath.equals(that.targetPath) : that.targetPath != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (targetPath != null ? targetPath.hashCode() : 0);
        result = 31 * result + (homePath != null ? homePath.hashCode() : 0);
        result = 31 * result + (module != null ? module.hashCode() : 0);
        return result;
    }
}
