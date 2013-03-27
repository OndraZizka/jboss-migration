package cz.muni.fi.jboss.migration.migrators.dataSources;

import cz.muni.fi.jboss.migration.FileTransferInfo;
import org.apache.commons.lang.StringUtils;

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
    
    /**
     * Sets the name of the Copy Memory for drivers.
     * In special cases altName is set for alternative JDBC driver (JTDS).
     *
     * @param className  Driver class from -ds.xml file from AS5 config.
     */
    public static void deriveAndSetDriverName(FileTransferInfo rb, String className) {
        
        rb.setName( null );
        rb.setAltName( null );
        
        if( className.contains("postgres")) {
            rb.setName("postgresql");
        }
        else if( className.contains("microsoft")) {
            rb.setName("sqljdbc");
            rb.setAltName("jtds");
        }
        else if( className.contains("db2")) {
            rb.setName("db2");
        }
        else if( className.contains("sybase")) {
            rb.setName("sybase");
            rb.setAltName("jtds");
        }
        else if( className.contains("mysql")) {
            rb.setName("sqljdbc");
        }
        else if( className.contains("oracle")) {
            rb.setName("ojdbc");
        }
        else if( className.contains("hsqldb")) {
            rb.setName("hsqldb");
        }
        else {
            // Guesstimate the value from the classname: org.foo.Bar -> "foo".
            String temp = StringUtils.substringAfter(className, ".");
            rb.setName(StringUtils.substringBefore(temp, "."));
        }
    }
    
    
    /**
     * Setting module for different databases.
     *
     * @return string containing generate module name
     * @deprecated TODO: Refactor & reuse cz.muni.fi.jboss.migration.migrators.dataSources.Util.deriveAndSetDriverName().
     *                   (Or rather the other way around.)
     */
    public static String deriveDriverModuleName(String driverName) {
        String module = "migration.jdbc.drivers.";
        if (driverName.contains("mysql")) {
            module = module + "mysql";
        }
        // Mssql
        if (driverName.contains("microsoft")) {
            module = module + "mssql";
        }
        if (driverName.contains("sybase")) {
            module = module + "sybase";
        }
        if (driverName.contains("postgresql")) {
            module = module + "postgresql";
        }
        if (driverName.contains("oracle")) {
            module = module + "oracle";
        }
        if (driverName.contains("hsqldb")) {
            module = module + "hsqldb";
        }
        if (driverName.contains("db2")) {
            module = module + "db2";
        }
        if (driverName.contains("jtds")) {
            module = module + "jtds";
        }

        return module;
    }

    

}// class
