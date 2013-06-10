package org.jboss.loom.tools.report.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang.StringUtils;
import org.jboss.loom.migrators.HasProperties;
import org.jboss.loom.migrators.Origin;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;
import org.jboss.loom.tools.report.adapters.MapPropertiesAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
@XmlAccessorType( XmlAccessType.NONE )
public class ConfigFragmentReportBean {
    private static final Logger log = LoggerFactory.getLogger(ConfigFragmentReportBean.class);
    
    
    private ConfigPartDescriptor annotation;
    
    @XmlAttribute(name = "class")
    private Class<? extends IConfigFragment> cls;

    @XmlElement
    private Origin origin;

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    @XmlJavaTypeAdapter(value = MapPropertiesAdapter.class)
    private Map<String, String> properties;

    
    /**
     * Constructor, kind of.
     */
    public static ConfigFragmentReportBean from( IConfigFragment fragment ) {
        ConfigFragmentReportBean bean = new ConfigFragmentReportBean();
        
        bean.cls = fragment.getClass();
        
        // Annotations.
        bean.annotation = fragment.getClass().getAnnotation( ConfigPartDescriptor.class );
        
        if( fragment instanceof Origin.Wise ){
            bean.origin = ((Origin.Wise)fragment).getOrigin();
        }
        
        // Properties - own.
        if( fragment instanceof HasProperties ){
            bean.properties = ((HasProperties)fragment).getProperties();
        }
        // Properties - getters to a map.
        else {
            /*try {
                bean.properties = BeanUtils.describe( fragment );
            } catch(     IllegalAccessException | InvocationTargetException | NoSuchMethodException ex ) {
                log.warn("Failed extracting properties from " + bean.getClass().getSimpleName() + ":\n    " + ex.getMessage(), ex );
            }*/
            bean.properties = describeBean( fragment );
        }
        
        return bean;
    }
    
    
    /* Derived from an annotation. TODO: Move to a base class?*/
    @XmlAttribute
    public String getName(){ return this.annotation == null ? null : nullIfEmpty( this.annotation.name() ); }
    @XmlAttribute
    public String getDocLink(){ return this.annotation == null ? null : nullIfEmpty( this.annotation.docLink() ); }
    @XmlAttribute
    public String getIconFile(){ return this.annotation == null ? null : nullIfEmpty( this.annotation.iconFile() ); }
    
    
    private static String nullIfEmpty(String str){ return str == null ? null : (str.isEmpty() ? null : str); }
    
    
    /**
     *  Extracts all String getters properties to a map.
     */
    public static Map<String, String> describeBean(IConfigFragment bean){
        
        Map<String, String> ret = new HashMap();
                
        Method[] methods = bean.getClass().getMethods();
        for( Method method : methods ) {
            String name = method.getName();
            if( method.getParameterTypes().length != 0 )  continue;
            if( ! method.getReturnType().equals( String.class ) )  continue;
            if( ! (name.startsWith("get") || name.startsWith("is")) )  continue;
            try {
                ret.put( StringUtils.uncapitalize( name ), (String) method.invoke(bean));
            } catch(     IllegalAccessException | IllegalArgumentException | InvocationTargetException ex ) {
                log.warn("Failed extracting property from " + bean.getClass().getSimpleName() + ":\n    " + ex.getMessage(), ex );
            }
        }
        return ret;
    }


}// class
