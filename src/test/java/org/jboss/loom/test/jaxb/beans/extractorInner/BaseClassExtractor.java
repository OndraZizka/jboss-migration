package org.jboss.loom.test.jaxb.beans.extractorInner;

import org.eclipse.persistence.descriptors.ClassExtractor;
import org.eclipse.persistence.sessions.Record;
import org.eclipse.persistence.sessions.Session;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class BaseClassExtractor extends ClassExtractor {

    @Override
    public Class extractClassFromRow( Record rec, Session session ) {
        
        if( rec.get("@disc").equals("foo") )
                return JaxbInheritance_XmlClassExtractor_Test.SubFoo.class;
        
        if( rec.get("@disc").equals("bar") )
                return JaxbInheritance_XmlClassExtractor_Test.SubBar.class;
        
        return JaxbInheritance_XmlClassExtractor_Test.Base.class;
    }

}// class
