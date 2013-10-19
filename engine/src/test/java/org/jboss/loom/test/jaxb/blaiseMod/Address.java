package org.jboss.loom.test.jaxb.blaiseMod;

import javax.xml.bind.annotation.*;
 
@XmlAccessorType(XmlAccessType.FIELD)
public class Address extends ContactMethod {
 
    @XmlAttribute
    protected String street;
 
    @XmlAttribute
    protected String city;
 
}