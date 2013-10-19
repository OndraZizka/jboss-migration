package org.jboss.loom.test.jaxb.blaise;


import org.eclipse.persistence.descriptors.ClassExtractor;
import org.eclipse.persistence.sessions.Record;
import org.eclipse.persistence.sessions.Session;
 
public class ContactMethodClassExtractor extends ClassExtractor{
 
    @Override
    public Class extractClassFromRow(Record record, Session session) {
        if(null != record.get("@street")) {
            return Address.class;
        } else if(null != record.get("@number")) {
            return PhoneNumber.class;
        }
        return null;
    }
 
}