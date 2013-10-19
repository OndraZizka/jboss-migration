package org.jboss.loom.migrators;

import javax.xml.bind.annotation.XmlAttribute;
import org.jboss.loom.spi.IConfigFragment;

/**
 * Base class for MBeans; it's Origin.Wise and a IConfigFragment.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
//@XmlRootElement(name = "mbean")
//@XmlAccessorType(XmlAccessType.NONE)
public abstract class MBeanJaxbBase<T extends MBeanJaxbBase> extends OriginWiseJaxbBase<T> implements IConfigFragment, Origin.Wise {
 
    // MBean name
    @XmlAttribute(name = "name") 
    public String getMbeanName() { return mbeanName; }
    public void setMbeanName( String mbeanName ) { this.mbeanName = mbeanName; }
    private String mbeanName;
    
    
}// class
