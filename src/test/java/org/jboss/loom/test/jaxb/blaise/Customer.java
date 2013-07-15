package org.jboss.loom.test.jaxb.blaise;

import java.util.List;
import javax.xml.bind.annotation.*;
 
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Customer {
 
    @XmlElement(name="contact-method")
    private List<ContactMethod> contactMethods;


    public List<ContactMethod> getContactMethods() {
        return contactMethods;
    }
 
}