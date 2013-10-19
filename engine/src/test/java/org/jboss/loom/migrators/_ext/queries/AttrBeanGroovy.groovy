package org.jboss.loom.migrators._ext.queries;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.jboss.loom.spi.IConfigFragment;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name="foo")
public class AttrBeanGroovy implements IConfigFragment {

    @XmlAttribute String name;
    
    @XmlAttribute String value;
    

}// class
