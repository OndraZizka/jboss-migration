package org.jboss.loom.test.jaxb.beans.extractor;

import java.io.StringReader;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.jboss.loom.utils.XmlUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *  JAXB inheritance with MOXy, over attributes, using @XmlClassExtractor with private classes for beans.
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 *  @see  http://blog.bdoughan.com/2012/02/jaxb-and-inheritance-eclipselink-moxy.html
 */
public class JaxbInheritance_XmlClassExtractor_Test {
    @Test
    public void testUnmarshall() throws JAXBException{
        final Unmarshaller marshaller = XmlUtils.createJaxbContext(Root.class).createUnmarshaller();
        Root root = (Root) marshaller.unmarshal( new StringReader(getXml()) );
        
        Assert.assertEquals("MOXy is used", org.eclipse.persistence.jaxb.JAXBUnmarshaller.class, marshaller.getClass() );
        Assert.assertNotNull("Extracted some sub elements", root.subs.size() );
        Assert.assertEquals("2 sub elements", 2, root.subs.size() );
        Assert.assertEquals("sub elements go into subclasses", SubFoo.class, root.subs.get(0).getClass() );
        Assert.assertEquals("sub elements go into subclasses", SubBar.class, root.subs.get(1).getClass() );
    }


    private String getXml() {
        return "<?xml version='1.0' encoding='UTF-8'?>\n<root> <sub disc='foo'/> <sub disc='bar'/> </root>";
    }

}// class
