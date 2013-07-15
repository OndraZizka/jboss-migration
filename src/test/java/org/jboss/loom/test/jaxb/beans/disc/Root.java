package org.jboss.loom.test.jaxb.beans.disc;


import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Root {
    
    @XmlElement(name = "sub")
    List<Base> subs;

    public List<Base> getSubs() { return subs; }

}// class
