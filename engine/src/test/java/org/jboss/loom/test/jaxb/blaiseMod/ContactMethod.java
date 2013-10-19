package org.jboss.loom.test.jaxb.blaiseMod;

import javax.xml.bind.annotation.XmlSeeAlso;
import org.eclipse.persistence.oxm.annotations.XmlClassExtractor;
 
@XmlClassExtractor(ContactMethodClassExtractor.class)
@XmlSeeAlso({Address.class, PhoneNumber.class})
public abstract class ContactMethod {
 
}