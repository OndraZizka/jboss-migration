package org.jboss.loom.migrators.logging.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
public class HandlerBeanBase {
    
    @XmlAttribute(name = "name")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
     
}// class
