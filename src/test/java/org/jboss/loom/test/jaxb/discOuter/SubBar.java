package org.jboss.loom.test.jaxb.discOuter;

import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement
@XmlDiscriminatorValue("foo")
public class SubBar extends Base {


}// class
