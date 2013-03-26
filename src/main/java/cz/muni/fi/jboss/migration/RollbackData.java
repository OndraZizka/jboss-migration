package cz.muni.fi.jboss.migration;


/**
 * Helper class to store files which will be copied from AS5 to AS7 so they can be deleted if app fails.
 *
 * @author Roman Jakubco
 */
public class RollbackData {
    
    public static enum Type{
        DRIVER, LOG, RESOURCE, SECURITY, LOGMODULE
    }
    
    private String name;

    private Type type;

    // TODO: Change to File.
    private String targetPath;

    private String homePath;

    // Only if it is driver
    private String module;

    // TODO: Too specific fod DS module. Generalize.
    // Different name for jdbc driver. For Sybase and Mssql
    private String altName;

    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTargetPath() { return targetPath; }
    public void setTargetPath(String targetPath) { this.targetPath = targetPath; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getHomePath() { return homePath; }
    public void setHomePath(String homePath) { this.homePath = homePath; }

    public void setAltName(String altName) { this.altName = altName; }
    public String getAltName() { return altName; }
    //</editor-fold>
    

    
    // TBC: Do we need this?
    //<editor-fold defaultstate="collapsed" desc="hash/eq">
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RollbackData)) return false;
        
        RollbackData that = (RollbackData) o;
        
        if(type.equals(Type.LOGMODULE)){
            if (homePath != null ? !homePath.equals(that.homePath) : that.homePath != null) return false;
            if (module != null ? !module.equals(that.module) : that.module != null) return false;
            if (targetPath != null ? !targetPath.equals(that.targetPath) : that.targetPath != null) return false;
            if (type != null ? !type.equals(that.type) : that.type != null) return false;
            
            return true;
        }
        
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
    //</editor-fold>
    
}// class
