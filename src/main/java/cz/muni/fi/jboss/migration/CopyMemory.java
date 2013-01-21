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
    private String targetPath;
    private String homePath;
    // Only if it is driver
    private String module;

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

    public String driverModuleGen(){

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

        //return module;
        return "module";
    }
}
