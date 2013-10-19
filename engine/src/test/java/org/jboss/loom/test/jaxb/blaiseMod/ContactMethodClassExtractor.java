package org.jboss.loom.test.jaxb.blaiseMod;


import org.eclipse.persistence.descriptors.ClassExtractor;
import org.eclipse.persistence.sessions.Record;
import org.eclipse.persistence.sessions.Session;
 
public class ContactMethodClassExtractor extends ClassExtractor{
 
    @Override
    public Class extractClassFromRow(Record record, Session session) {
        Object rec = record.get("@disc");
        if( "address".equals(rec) ){
            return Address.class;
        } else if( "phone".equals(rec) ) {
            return PhoneNumber.class;
        }
        return null;
    }
 
}