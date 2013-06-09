package org.jboss.loom.tools.report.beans;


import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jboss.loom.ctx.MigrationData;
import org.jboss.loom.migrators.HasProperties;
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.IMigrator;
import org.jboss.loom.tools.report.adapters.IConfigFragmentAdapter;
import org.jboss.loom.tools.report.adapters.MapPropertiesAdapter;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
@XmlAccessorType( XmlAccessType.FIELD)
public class MigrationDataReportBean {
    
    @XmlAttribute
    private Class<? extends IMigrator> fromMigrator;
    
    @XmlElementWrapper(name = "configFragments")
    @XmlElement(name = "configFragment")
    @XmlJavaTypeAdapter(value = IConfigFragmentAdapter.class)
    private List<IConfigFragment> configFragments;
    
    @XmlElement
    private Origin origin;
    
    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    @XmlJavaTypeAdapter(value = MapPropertiesAdapter.class)
    private Map<String, String> properties;
    
    
    /**  Constructor, kind of */
    public static MigrationDataReportBean from( MigrationData migData ) {
        MigrationDataReportBean bean = new MigrationDataReportBean();
        
        bean.fromMigrator = migData.getFromMigrator();
        bean.configFragments = migData.getConfigFragments();
        
        if( migData instanceof Origin.Wise ){
            bean.origin = ((Origin.Wise)migData).getOrigin();
        }
        
        if( migData instanceof HasProperties ){
            bean.properties = ((HasProperties)migData).getProperties();
        }
        
        return bean;
    }


    public Class<? extends IMigrator> getFromMigrator() { return fromMigrator; }
    public List<IConfigFragment> getConfigFragments() { return configFragments; }
    public Origin getOrigin() { return origin; }
    public Map<String, String> getProperties() { return properties; }
    

}// class
