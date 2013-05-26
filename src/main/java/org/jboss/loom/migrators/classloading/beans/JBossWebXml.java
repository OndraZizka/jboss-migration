package org.jboss.loom.migrators.classloading.beans;

/**
 * 
        <jboss-web>
            <class-loading java2ClassLoadingCompliance="false">
                <loader-repository>
                    my.package:loader=my-app.war
                   <loader-repository-config>
                      java2ParentDelegation=false
                   </loader-repository-config>
                </loader-repository>
            </class-loading>
        </jboss-web>

 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class JBossWebXml {
    
    public Boolean java2ClassLoadingCompliance;


    // The rest TBD if needed / requested.
    
    
    
    public Boolean getJava2ClassLoadingCompliance() { return java2ClassLoadingCompliance; }
    public void setJava2ClassLoadingCompliance( Boolean java2ClassLoadingCompliance ) { this.java2ClassLoadingCompliance = java2ClassLoadingCompliance; }

}
