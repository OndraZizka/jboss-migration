package org.jboss.loom.migrators._ext.queries;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
public class FooBean {

    @XmlAttribute public String name;
    
    @XmlAttribute public String value;
    

}// class
