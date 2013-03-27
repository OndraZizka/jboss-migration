package cz.muni.fi.jboss.migration;


/**
 * Stores files to be copied from AS5 to AS7. 
 * Used for cleanup when be deleted if the app fails.
 * 
 * Je to info o vsetkych potrebnych filoch z AS5, ktore musia byt prenesene do AS7,
 * aby bola migracia kompletna/uspesna. 
 * 
 * Zaznamenavaju sa o nich informacie:
 * - kde su v AS5
 * - kde budu v AS7
 * - meno
 * - typ dat pre rozdelovanie a spravne umiestnenie
 * - v pripade veci, ktore musia byt v moduloch drzia aj meno svojho buduceho modulu
 * 
 * Toto vsetko sa pouzije na ich kopirovanie do AS7. 
 * Ak zlyha migracia z dakeho dovodu tak sa pouzije ich ulozena cielova path v AS7 aby sa mohli zmazat a zostal AS7 server neporuseny. 
 *
 * @author Roman Jakubco
 */
public class FileTransferInfo {
    
    public static enum Type{
        DRIVER, /*LOG,*/ RESOURCE, SECURITY, LOGMODULE
    }
    
    private String name;

    private Type type;

    // TODO: Change to File.
    private String targetPath;

    private String homePath;

    /** Module name. Only for drivers. */
    private String moduleName;

    // TODO: Too specific fod DS module. Generalize.
    // Different name for jdbc driver. For Sybase and Mssql
    private String altName;


    @Override
    public String toString() {
        return "FileTransferInfo{" + "name=" + name + ", type=" + type + ", targetPath=" + targetPath + ", homePath=" + homePath + ", moduleName=" + moduleName + ", altName=" + altName + '}';
    }

    
    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTargetPath() { return targetPath; }
    public void setTargetPath(String targetPath) { this.targetPath = targetPath; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public String getHomePath() { return homePath; }
    public void setHomePath(String homePath) { this.homePath = homePath; }

    public void setAltName(String altName) { this.altName = altName; }
    public String getAltName() { return altName; }
    //</editor-fold>
    

    
    // TBC: Do we need this? -- We do, context has a Set<RollbackData>.
    //<editor-fold defaultstate="collapsed" desc="hash/eq">
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileTransferInfo)) return false;
        
        FileTransferInfo that = (FileTransferInfo) o;
        
        if(Type.LOGMODULE.equals(type)){
            if (homePath != null   ? !homePath.equals(that.homePath)     : that.homePath != null) return false;
            if (moduleName != null     ? !moduleName.equals(that.moduleName)         : that.moduleName != null) return false;
            if (targetPath != null ? !targetPath.equals(that.targetPath) : that.targetPath != null) return false;
            if (type != null       ? !type.equals(that.type)             : that.type != null) return false;
            
            return true;
        }
        
        if (homePath != null   ? !homePath.equals(that.homePath)     : that.homePath != null) return false;
        if (moduleName   != null   ? !moduleName.equals(that.moduleName)         : that.moduleName != null) return false;
        if (name     != null   ? !name.equals(that.name)             : that.name != null) return false;
        if (targetPath != null ? !targetPath.equals(that.targetPath) : that.targetPath != null) return false;
        if (type     != null   ? !type.equals(that.type)             : that.type != null) return false;
        // TODO: Improve the boolean logic, handle with a static method.
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (targetPath != null ? targetPath.hashCode() : 0);
        result = 31 * result + (homePath != null ? homePath.hashCode() : 0);
        result = 31 * result + (moduleName != null ? moduleName.hashCode() : 0);
        return result;
    }
    //</editor-fold>
    
}// class
