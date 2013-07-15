package org.jboss.loom.test.jaxb.beans.extractor;

import javax.xml.bind.annotation.XmlSeeAlso;
import org.eclipse.persistence.oxm.annotations.XmlClassExtractor;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlClassExtractor(BaseClassExtractor.class)
@XmlSeeAlso({SubFoo.class, SubBar.class})
public class Base {
    

}// class
