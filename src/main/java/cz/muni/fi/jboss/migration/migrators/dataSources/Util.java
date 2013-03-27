package cz.muni.fi.jboss.migration.migrators.dataSources;

import cz.muni.fi.jboss.migration.RollbackData;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class Util {
    
    /**
     * Sets the name of the Copy Memory for drivers.
     * In special cases altName is set for alternative JDBC driver (JTDS).
     *
     * @param className  Driver class from -ds.xml file from AS5 config.
     */
    public static void deriveAndSetDriverName(RollbackData rb, String className) {
        
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
