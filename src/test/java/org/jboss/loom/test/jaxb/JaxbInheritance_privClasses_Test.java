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
 *  JAXB inheritance with MOXy, over attributes, with private classes for beans.
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class JaxbInheritance_privClasses_Test {    
    @Test
    public void testUnmarshall() throws JAXBException{
        final Unmarshaller marshaller = XmlUtils.createJaxbContext(Root.class).createUnmarshaller();
        Root root = (Root) marshaller.unmarshal( new StringReader(getXml()) );
        
        Assert.assertEquals("MOXy is used", org.eclipse.persistence.jaxb.JAXBUnmarshaller.class, marshaller.getClass() );
        Assert.assertNotNull("extracted some sub elements", root.subs.size() );
        Assert.assertEquals("2 sub elements", 2, root.subs.size() );

        //boolean rightClass = (DiscFoo.class.isAssignableFrom( root.subs.get(0).getClass() ));
        Assert.assertEquals("base elements go into subclasses", DiscFoo.class, root.subs.get(0).getClass() );
        
        //rightClass = (DiscBar.class.isAssignableFrom( root.subs.get(1).getClass() ));
        Assert.assertEquals("base elements go into subclasses", DiscBar.class, root.subs.get(1).getClass() );
    }


    private String getXml() {
        return "<?xml version='1.0' encoding='UTF-8'?>\n<root><sub disc='foo'/><sub disc='bar'/></root>";
    }
}

// --- JAXB Beans ---

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class Root {
    @XmlElement(name = "sub")
    List<Base> subs;
}

@XmlDiscriminatorNode("@disc")
@XmlSeeAlso({DiscFoo.class, DiscBar.class})
class Base {}

@XmlRootElement @XmlDiscriminatorValue("foo")
class DiscFoo extends Base {}

@XmlRootElement @XmlDiscriminatorValue("bar")
class DiscBar extends Base {}
