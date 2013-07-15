package org.jboss.loom.test.jaxb.discOuter;

import javax.xml.bind.annotation.XmlSeeAlso;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorNode;
import org.jboss.loom.test.jaxb.JaxbInheritance_InnerStatic_Test;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlDiscriminatorNode("@disc")
@XmlSeeAlso({JaxbInheritance_InnerStatic_Test.DiscFoo.class, JaxbInheritance_InnerStatic_Test.DiscBar.class})
public class Base {
    

}// class
