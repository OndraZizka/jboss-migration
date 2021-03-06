package org.jboss.loom.migrators._ext.queries;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.jboss.loom.spi.IConfigFragment;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name = "foo")
public class AttrBean implements IConfigFragment {

    @XmlAttribute public String name;
    
    @XmlAttribute public String value;


    public String getName() { return name; }
    public String getValue() { return value; }

}// class
