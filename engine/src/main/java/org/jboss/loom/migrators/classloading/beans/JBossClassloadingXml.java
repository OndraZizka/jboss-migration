package org.jboss.loom.migrators.classloading.beans;

/**
        <classloading xmlns="urn:jboss:classloading:1.0"
                      domain="DefaultDomain"
                      top-level-classloader="true"
                      export-all="NON_EMPTY"
                      import-all="true">
        </classloading>
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class JBossClassloadingXml {
    
    private String domain;
    private String topLevelClassloader;
    private Boolean exportAll;
    private Boolean importAll;
    
    private String name;
    private String parentDomain;
    private Boolean parentFirst;

    

    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getDomain() { return domain; }
    public void setDomain( String domain ) { this.domain = domain; }
    public String getTopLevelClassloader() { return topLevelClassloader; }
    public void setTopLevelClassloader( String topLevelClassloader ) { this.topLevelClassloader = topLevelClassloader; }
    public Boolean getExportAll() { return exportAll; }
    public void setExportAll( Boolean exportAll ) { this.exportAll = exportAll; }
    public Boolean getImportAll() { return importAll; }
    public void setImportAll( Boolean importAll ) { this.importAll = importAll; }
    public String getName() { return name; }
    public void setName( String name ) { this.name = name; }
    public String getParentDomain() { return parentDomain; }
    public void setParentDomain( String parentDomain ) { this.parentDomain = parentDomain; }
    public Boolean getParentFirst() { return parentFirst; }
    public void setParentFirst( Boolean parentFirst ) { this.parentFirst = parentFirst; }
    //</editor-fold>
    
}// class
