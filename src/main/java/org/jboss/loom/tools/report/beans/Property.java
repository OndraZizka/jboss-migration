package org.jboss.loom.tools.report.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
@XmlAccessorType( value = XmlAccessType.NONE )
public final class Property {
    @XmlAttribute
    public String name;
    @XmlAttribute
    public String value;


    public Property() {
    }


    public Property( String name, String value ) {
        this.name = name;
        this.value = value;
    }

}// class
