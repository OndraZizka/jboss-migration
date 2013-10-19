package org.jboss.loom.test.jaxb.extractorInner;

import java.io.StringReader;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.eclipse.persistence.oxm.annotations.XmlClassExtractor;
import org.jboss.loom.utils.XmlUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *  JAXB inheritance with MOXy, over attributes, using @XmlClassExtractor with private classes for beans.
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 *  @see  http://blog.bdoughan.com/2012/02/jaxb-and-inheritance-eclipselink-moxy.html
 */
public class JaxbInheritance_XmlClassExtractorInner_Test {
    @Test
    public void testUnmarshall() throws JAXBException{
        final Unmarshaller marshaller = XmlUtils.createJaxbContext(Root.class).createUnmarshaller();
        Root root = (Root) marshaller.unmarshal( new StringReader(getXml()) );
        
        Assert.assertEquals("MOXy is used", org.eclipse.persistence.jaxb.JAXBUnmarshaller.class, marshaller.getClass() );
        Assert.assertNotNull("Extracted some sub elements", root.subs.size() );
        Assert.assertEquals("2 sub elements", 2, root.subs.size() );
        Assert.assertEquals("<sub> go into subclasses", SubFoo.class, root.subs.get(0).getClass() );
        Assert.assertEquals("<sub> go into subclasses", SubBar.class, root.subs.get(1).getClass() );
    }


    private String getXml() {
        return "<?xml version='1.0' encoding='UTF-8'?>\n<root><sub disc='foo'/><sub disc='bar'/></root>";
    }
    
    
    // --- JAXB Beans ---
    
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Root {
        @XmlElement(name = "sub")
        List<Base> subs;
    }

    @XmlClassExtractor(BaseClassExtractor.class)
    @XmlSeeAlso({SubFoo.class, SubBar.class})
    public static abstract class Base {}

    @XmlRootElement
    public static class SubFoo extends Base {}

    @XmlRootElement
    public static class SubBar extends Base {}

}// class
