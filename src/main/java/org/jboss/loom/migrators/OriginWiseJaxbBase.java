package org.jboss.loom.migrators;

import javax.xml.bind.annotation.XmlTransient;
import org.jboss.loom.spi.IConfigFragment;

/**
 * Base class for JAXB beans; it's Origin.Wise and a IConfigFragment.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public abstract class OriginWiseJaxbBase<T extends OriginWiseJaxbBase> implements IConfigFragment, Origin.Wise {
 
    // Origin
    @XmlTransient
    private Origin origin;
    @Override public Origin getOrigin() { if( origin == null ) origin = new Origin( null ); return origin; }
    @Override public T setOrigin( Origin origin ) { this.origin = origin; return (T) this; }
    
}// class
