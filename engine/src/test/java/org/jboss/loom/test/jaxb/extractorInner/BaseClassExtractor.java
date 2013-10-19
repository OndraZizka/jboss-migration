package org.jboss.loom.test.jaxb.extractorInner;

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
                return JaxbInheritance_XmlClassExtractorInner_Test.SubFoo.class;
        
        if( rec.get("@disc").equals("bar") )
                return JaxbInheritance_XmlClassExtractorInner_Test.SubBar.class;
        
        return JaxbInheritance_XmlClassExtractorInner_Test.Base.class;
    }

}// class
