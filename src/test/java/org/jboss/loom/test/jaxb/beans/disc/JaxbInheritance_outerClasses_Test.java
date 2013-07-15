package org.jboss.loom.test.jaxb.beans.disc;

import java.io.StringReader;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.jboss.loom.utils.XmlUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *  JAXB inheritance with MOXy, over attributes, with inner static classes for beans.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class JaxbInheritance_outerClasses_Test {    
    @Test
    public void testUnmarshall() throws JAXBException{
        final Unmarshaller marshaller = XmlUtils.createJaxbContext( org.jboss.loom.test.jaxb.beans.disc.Root.class)
                .createUnmarshaller();
        org.jboss.loom.test.jaxb.beans.disc.Root root = (org.jboss.loom.test.jaxb.beans.disc.Root)
            marshaller.unmarshal( new StringReader(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><sub disc=\"foo\"/><sub disc=\"bar\"/></root>") );

        Assert.assertEquals("MOXy is used", org.eclipse.persistence.jaxb.JAXBUnmarshaller.class, marshaller.getClass() );
        Assert.assertEquals("base elements go into subclasses", SubFoo.class, root.getSubs().get(0).getClass() );
        Assert.assertEquals("base elements go into subclasses", SubBar.class, root.getSubs().get(1).getClass() );
    }
}
