package org.jboss.loom.migrators.jaxr;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.jboss.loom.migrators.MBeanJaxbBase;
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;

/**
 * $JBOSS_HOME/server/all/deploy/juddi-service.sar/META-INF/jboss-service.xml

<server>
    <!-- The juddi service configration -->
    <mbean code="org.jboss.jaxr.juddi.JUDDIService" name="jboss:service=juddi">
    
        <!-- Whether we want to run the db initialization scripts -->
        <!-- Should all tables be created on Start-->
        <attribute name="CreateOnStart">false</attribute>
        <!-- Should all tables be dropped on Stop-->
        <attribute name="DropOnStop">false</attribute>
        <!-- Should all tables be dropped on Start-->
        <attribute name="DropOnStart">false</attribute>
        <!-- Datasource to Database-->
        <attribute name="DataSourceUrl">java:/DefaultDS</attribute>
        <!-- Alias to the registry-->
        <attribute name="RegistryOperator">RegistryOperator</attribute>
        <!-- Should I bind a Context to which JaxrConnectionFactory bound-->
        <attribute name="ShouldBindJaxr">true</attribute>
        <!-- Context to which JaxrConnectionFactory to bind to.
             If you have remote clients, please bind it to the global
             namespace (default behavior). To just cater to clients running
             on the same VM as JBoss, change to java:/JAXR -->
        <attribute name="BindJaxr">JAXR</attribute>
        <attribute name="DropDB">false</attribute> 
        <depends>jboss.jca:service=DataSourceBinding,name=DefaultDS</depends>
    </mbean>
</server>
   
 * 
 * @Jira: MIGR-42
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@ConfigPartDescriptor(
    name = "JAXR - jUDDI service ${name}", 
    docLink = "https://access.redhat.com/site/documentation/en-US/JBoss_Enterprise_Application_Platform/5/html-single/Administration_And_Configuration_Guide/index.html#idm2915376"
)
@XmlRootElement(name = "mbean")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "mbean")
public final class JaxrConfigBean extends MBeanJaxbBase<JaxrConfigBean> implements IConfigFragment, Origin.Wise {
       
    // Whether we want to run the db initialization scripts
    // Should all tables be created on Start
    @XmlPath("attribute[@name='CreateOnStart']/@value")
    private boolean CreateOnStart = false;

    // Should all tables be dropped on Stop
    @XmlPath("attribute[@name='DropOnStop']/@value")
    private boolean DropOnStop = false;

    // Should all tables be dropped on Start
    @XmlPath("attribute[@name='DropOnStart']/@value")
    private boolean DropOnStart = false;

    // Datasource to Database
    @XmlPath("attribute[@name='DataSourceUrl']/@value")
    private String DataSourceUrl; //java:/DefaultDS

    // Alias to the registry
    @XmlPath("attribute[@name='RegistryOperator']/@value")
    private String RegistryOperator; // RegistryOperator

    // Should I bind a Context to which JaxrConnectionFactory bound
    @XmlPath("attribute[@name='ShouldBindJaxr']/@value")
    private boolean ShouldBindJaxr; // true

    // Context to which JaxrConnectionFactory to bind to.
    // If you have remote clients, please bind it to the global namespace(default behavior). 
    // To just cater to clients running on the same VM as JBoss, change to java:/JAXR
    @XmlPath("attribute[@name='BindJaxr']/@value")
    private boolean BindJaxr; // JAXR
    @XmlPath("attribute[@name='DropDB']/@value")
    private boolean DropDB = false; 


    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public boolean isCreateOnStart() { return CreateOnStart; }
    public void setCreateOnStart( boolean CreateOnStart ) { this.CreateOnStart = CreateOnStart; }
    public boolean isDropOnStop() { return DropOnStop; }
    public void setDropOnStop( boolean DropOnStop ) { this.DropOnStop = DropOnStop; }
    public boolean isDropOnStart() { return DropOnStart; }
    public void setDropOnStart( boolean DropOnStart ) { this.DropOnStart = DropOnStart; }
    public String getDataSourceUrl() { return DataSourceUrl; }
    public void setDataSourceUrl( String DataSourceUrl ) { this.DataSourceUrl = DataSourceUrl; }
    public String getRegistryOperator() { return RegistryOperator; }
    public void setRegistryOperator( String RegistryOperator ) { this.RegistryOperator = RegistryOperator; }
    public boolean isShouldBindJaxr() { return ShouldBindJaxr; }
    public void setShouldBindJaxr( boolean ShouldBindJaxr ) { this.ShouldBindJaxr = ShouldBindJaxr; }
    public boolean isBindJaxr() { return BindJaxr; }
    public void setBindJaxr( boolean BindJaxr ) { this.BindJaxr = BindJaxr; }
    public boolean isDropDB() { return DropDB; }
    public void setDropDB( boolean DropDB ) { this.DropDB = DropDB; }
    //</editor-fold>
    
}// class
