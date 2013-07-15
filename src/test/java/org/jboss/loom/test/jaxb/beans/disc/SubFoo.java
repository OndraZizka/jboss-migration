package org.jboss.loom.test.jaxb.beans.disc;

import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
@XmlDiscriminatorValue("foo")
public class SubFoo {

    
}// class
