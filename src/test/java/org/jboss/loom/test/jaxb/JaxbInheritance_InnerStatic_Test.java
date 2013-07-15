package org.jboss.loom.test.jaxb;

import java.io.StringReader;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorNode;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;
import org.jboss.loom.utils.XmlUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *  JAXB inheritance with MOXy, over attributes, with inner static classes for beans.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class JaxbInheritance_InnerStatic_Test {    
    @Test
    public void testUnmarshall() throws JAXBException{
        final Unmarshaller marshaller = XmlUtils.createJaxbContext(Root.class).createUnmarshaller();
        Root root = (Root) marshaller.unmarshal( new StringReader(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><sub disc=\"foo\"/><sub disc=\"bar\"/></root>") );

        Assert.assertEquals("MOXy is used", org.eclipse.persistence.jaxb.JAXBUnmarshaller.class, marshaller.getClass() );
        Assert.assertEquals("base elements go into subclasses", DiscFoo.class, root.subs.get(0).getClass() );
        Assert.assertEquals("base elements go into subclasses", DiscBar.class, root.subs.get(1).getClass() );
    }
    
    // --- JAXB Beans ---
    
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Root {
        @XmlElement(name = "sub")
        List<Base> subs;
    }

    @XmlDiscriminatorNode("@disc")
    @XmlSeeAlso({DiscFoo.class, DiscBar.class})
    public static class Base {}

    @XmlRootElement @XmlDiscriminatorValue("foo")
    public static class DiscFoo {}

    @XmlRootElement @XmlDiscriminatorValue("bar")
    public static class DiscBar {}

}
