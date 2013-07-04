package org.jboss.loom.test.jaxb;

import java.io.StringReader;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorNode;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;
import org.jboss.loom.utils.XmlUtils;
import org.junit.Assert;
import org.junit.Test;
import org.xnio.streams.ReaderInputStream;


/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class JaxbInheritanceTest {    
    @Test
    public void testUnmarshall() throws JAXBException{
        final Unmarshaller marshaller = XmlUtils.createJaxbContext(Root.class).createUnmarshaller();
        Root root = (Root) marshaller.unmarshal( new StringReader(getXml()) );

        boolean rightClass = (DiscFoo.class.isAssignableFrom( root.subs.get(0).getClass() ));
        Assert.assertTrue( "base elements go into subclasses", rightClass );
        
        rightClass = (DiscBar.class.isAssignableFrom( root.subs.get(1).getClass() ));
        Assert.assertTrue( "base elements go into subclasses", rightClass );
    }


    private String getXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><sub disc=\"foo\"/><sub disc=\"bar\"/></root>";
    }
}

@XmlRootElement
class Root {
    @XmlElement(name = "sub")
    List<Base> subs;
}

@XmlDiscriminatorNode("@disc")
@XmlSeeAlso({DiscFoo.class, DiscBar.class})
class Base {}

@XmlRootElement @XmlDiscriminatorValue("foo")
class DiscFoo {}

@XmlRootElement @XmlDiscriminatorValue("bar")
class DiscBar {}
