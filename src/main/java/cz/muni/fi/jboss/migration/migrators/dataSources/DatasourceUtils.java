package cz.muni.fi.jboss.migration.migrators.dataSources;

import cz.muni.fi.jboss.migration.FileTransferInfo;
import cz.muni.fi.jboss.migration.utils.AS7ModuleUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Roman:
 * Tieto dve metody podla mna su uplne zbytocne v buducnosti. 
 * Toto fungovalo len na oko s tym, ze som povedal ze sa budu len tieto drivery migrovat. 
 * Omnoho lepsie by bolo pouzit techniku z loggingu. 
 * Teda pouzit metodu na prehladavanie JAR files a najdenie v nich classu, 
 * ktora je uvedena v datasourcoch a podla toho setnut rollback data. A presunut ju do AS7 a vytvorit module.
 * 
 * Tym padom by odpadlo aj setovanie alternatvneho driveru lebo aplikacia by snad nasla jar toho alternativneho driveru
 * (snad nebudu dve rozlisne jar files s rovnakou classou...). 
 * Nepremenil som to zatial lebo som si nebol isty o spravnosti a tvojom nazore na prehladavanie 
 * skoro vsetkych JAR files v AS5 a hladanie v nich odpovedajucu classu uz v loggingu a nie to este tu.
 * 
 * Tym padom by metoda deriveAndSetDriverName stratila zmysel lebo by sa rovno setlo meno jarka v rollbackData. No a na vymyslanie nazvu modulu by sa dalo vymysliet nieco praktickejsie, co by sa odvodzovalo od mena samotneho JAR filu alebo umely nazov. 
 * 
 * @author Roman Jakubco
 */
public class DatasourceUtils {
    
    public static Document createJDBCDriverModuleXML(String moduleName, String fileName) throws ParserConfigurationException {

        String[] deps = new String[]{"javax.api", "javax.transaction.api", null, "javax.servlet.api"};
        // Servlet API necessary only for H2 AFAIK.
        
        return AS7ModuleUtils.createModuleXML( moduleName, fileName, deps );

    }
    
    /**
     * Sets the name of the Copy Memory for drivers.
     * In special cases altName is set for alternative JDBC driver (JTDS).
     *
     * @param className  Driver class from -ds.xml file from AS5 config.
     * @deprecated No longer needed. New approach to modules in datasources
     */
    public static void deriveAndSetDriverName(FileTransferInfo fti, String className) {
        
        fti.setName( null );
        fti.setAltName( null );
        
        if( className.contains("postgres")) {
            fti.setName("postgresql");
        }
        else if( className.contains("microsoft")) {
            fti.setName("sqljdbc");
            fti.setAltName("jtds");
        }
        else if( className.contains("db2")) {
            fti.setName("db2");
        }
        else if( className.contains("sybase")) {
            fti.setName("sybase");
            fti.setAltName("jtds");
        }
        else if( className.contains("mysql")) {
            fti.setName("sqljdbc");
        }
        else if( className.contains("oracle")) {
            fti.setName("ojdbc");
        }
        else if( className.contains("hsqldb")) {
            fti.setName("hsqldb");
        }
        else {
            // Guesstimate the value from the classname: org.foo.Bar -> "foo".
            String temp = StringUtils.substringAfter(className, ".");
            fti.setName(StringUtils.substringBefore(temp, "."));
        }
    }
    
    
    /**
     * @returns A module name, e.g. "migration.jdbc.drivers.mssql".
     * @deprecated No longer needed. New approach to modules in datasources
     */
    public static String deriveDriverModuleName(String driverName) {
        String ident = deriveDatabaseIdentifier(driverName);
        if( ident == null)
            ident = "unknown";
        return "migration.jdbc.drivers." + ident;
    }
    
    public static String deriveDatabaseIdentifier(String driverName) {
        if (driverName.contains("mysql")) {
            return "mysql";
        }
        if (driverName.contains("microsoft")) {
            return "mssql";
        }
        if (driverName.contains("sybase")) {
            return "sybase";
        }
        if (driverName.contains("postgresql")) {
            return "postgresql";
        }
        if (driverName.contains("oracle")) {
            return "oracle";
        }
        if (driverName.contains("hsqldb")) {
            return "hsqldb";
        }
        if (driverName.contains("db2")) {
            return "db2";
        }
        if (driverName.contains("jtds")) {
            return "jtds";
        }
        return null;
    }



}// class
