package org.jboss.loom.migrators.classloading.beans;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class DeploymentClassloadingConfig {
    
    JBossClassloadingXml jbossClassloading;
    
    JBossWebXml jbossWeb;


    public JBossClassloadingXml getJbossClassloadingConf() { return jbossClassloading; }
    public void setJbossClassloadingConf( JBossClassloadingXml jbossClassloading ) { this.jbossClassloading = jbossClassloading; }
    public JBossWebXml getJbossWebConf() { return jbossWeb; }
    public void setJbossWebConf( JBossWebXml jbossWeb ) { this.jbossWeb = jbossWeb; }
   
}// class
