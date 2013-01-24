package cz.muni.fi.jboss.migration;

import org.apache.commons.lang.StringUtils;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:25 AM
 */
//
//Concole UI
//        ==========
//
//        Usage:
//
//        java -jar AsMigrator.jar [<option>, ...] [as5.dir=]<as5.dir> [as7.dir=]<as7.dir>
//
//        Options:
//
//        as5.profile=<name>
//Path to AS 5 profile.
//        Default: "default"
//
//        as7.confPath=<path>
//Path to AS 7 config file.
//        Default: "standalone/conf/standalone.xml"
//
//        conf.<module>.<property>=<value> := Module-specific options.
//
//<module> := Name of one of modules. E.g. datasource, jaas, security, ...
//<property> := Name of the property to set. Specific per module. May occur multiple times.

public class MigratorApp {
    public static void main(String[] args) {
        GlobalConfiguration global = new GlobalConfiguration();


// Process arguments...
        for( int i = 0; i < args.length; i++ ) {

            // Jednoduch**é **- be**z parametru*
            if( "-xml".equals(args[i]) ) {

            }
            //**S parametrem*
            if( args[i].contains("as5.dir") ) {
                  global.setDirAS5(StringUtils.substringAfter(args[i],"="));
            }

        }
    }
}
