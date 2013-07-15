package org.jboss.loom.test.jaxb.discOuter;

import javax.xml.bind.annotation.XmlSeeAlso;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorNode;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlDiscriminatorNode("@disc")
@XmlSeeAlso({SubFoo.class, SubBar.class})
public class Base {
    

}// class
