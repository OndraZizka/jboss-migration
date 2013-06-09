package org.jboss.loom.tools.report.beans;

import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jboss.loom.migrators.HasProperties;
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.tools.report.adapters.MapPropertiesAdapter;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
@XmlAccessorType( XmlAccessType.FIELD )
public class ConfigFragmentReportBean {
    
    
    @XmlAttribute
    private Class<? extends IConfigFragment> cls;

    @XmlElement
    private Origin origin;

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    @XmlJavaTypeAdapter(value = MapPropertiesAdapter.class)
    private Map<String, String> properties;

    
    public static ConfigFragmentReportBean from( IConfigFragment fragment ) {
        ConfigFragmentReportBean bean = new ConfigFragmentReportBean();
        
        bean.cls = fragment.getClass();
        
        if( fragment instanceof Origin.Wise ){
            bean.origin = ((Origin.Wise)fragment).getOrigin();
        }
        
        if( fragment instanceof HasProperties ){
            bean.properties = ((HasProperties)fragment).getProperties();
        }
        
        return bean;
    }
    

}// class
