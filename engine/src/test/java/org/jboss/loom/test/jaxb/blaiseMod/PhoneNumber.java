package org.jboss.loom.test.jaxb.blaiseMod;

import javax.xml.bind.annotation.*;
 
@XmlAccessorType(XmlAccessType.FIELD)
public class PhoneNumber extends ContactMethod {
 
    @XmlAttribute
    protected String number;
 
}