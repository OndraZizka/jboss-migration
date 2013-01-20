package cz.muni.fi.jboss.migration;

/**
 * Helping class for remembering files which will be copied from AS5 to AS7 so they can be deleted if app fails.
 * Only idea and first try...
 *
 * @author Roman Jakubco
 * Date: 11/12/12
 * Time: 3:13 PM
 */
public class CopyMemory {
    private String name;
    private String type;
    private String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String driverModuleGen(String name){
        String module = "";
        if(name.contains("mysql")){

        }
        // Mssql
        if(name.contains("microsoft")){

        }
        if(name.contains("sybase")){

        }
        if(name.contains("postgresql")){

        }
        if(name.contains("oracle")){

        }
        if(name.contains("hsqldb")){

        }
        if(name.contains("db2")){

        }
        return module;
    }
}
