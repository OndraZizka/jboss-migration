package org.jboss.loom.test.jaxb.blaise;

import java.io.StringReader;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.jboss.loom.utils.XmlUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *  JAXB inheritance with MOXy, from http://blog.bdoughan.com/2012/02/jaxb-and-inheritance-eclipselink-moxy.html.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class JaxbInheritanceBlaiseTest {    
    @Test
    public void testUnmarshall() throws JAXBException{
        final Unmarshaller marshaller = XmlUtils.createJaxbContext( Customer.class ).createUnmarshaller();
        Customer root = (Customer) marshaller.unmarshal( new StringReader( getXml() ));

        Assert.assertEquals("MOXy is used", org.eclipse.persistence.jaxb.JAXBUnmarshaller.class, marshaller.getClass() );
        Assert.assertEquals("base elements go into subclasses", PhoneNumber.class, root.getContactMethods().get(0).getClass() );
        Assert.assertEquals("base elements go into subclasses", Address.class, root.getContactMethods().get(1).getClass() );
        Assert.assertEquals("base elements go into subclasses", PhoneNumber.class, root.getContactMethods().get(2).getClass() );
    }


    private String getXml() {
        return
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<customer>\n" +
            "    <contact-method number='555-1111'/>\n" +
            "    <contact-method street='1 A St' city='Any Town'/>\n" +
            "    <contact-method number='555-2222'/>\n" +
            "</customer>";
    }

}// class
