package org.jboss.loom.migrators;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.jboss.loom.spi.IConfigFragment;

/**
 * Base class for MBeans; with Origin.Wise and a list of IConfigFragments.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name = "mbean")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "mbean")
public abstract class MBeanJaxbBase<T extends MBeanJaxbBase> implements IConfigFragment, Origin.Wise {
    
    @XmlAttribute(name = "name") 
    private String mbeanName;
    

    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getMbeanName() { return mbeanName; }
    public void setMbeanName( String mbeanName ) { this.mbeanName = mbeanName; }
    //</editor-fold>
    
    
    // Origin
    private Origin origin;
    @Override public Origin getOrigin() { if( origin == null ) origin = new Origin( null ); return origin; }
    @Override public T setOrigin( Origin origin ) { this.origin = origin; return (T) this; }
    
}// class
