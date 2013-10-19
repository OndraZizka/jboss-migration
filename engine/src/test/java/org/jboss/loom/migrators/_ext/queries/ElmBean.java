package org.jboss.loom.migrators._ext.queries;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name = "bar")
public class ElmBean {

    @XmlAttribute public String name;
    
    //@XmlElement
    @XmlPath("/value/text()")
    public String value;


    public String getName() { return name; }
    public String getValue() { return value; }

}// class
