package org.jboss.loom.test.jaxb;

import java.io.File;
import java.util.List;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertNotNull;
import org.jboss.loom.TestAppConfig;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.logging.jaxb.CategoryBean;
import org.jboss.loom.migrators.mail.MailServiceBean;
import static org.jboss.loom.utils.XmlUtils.unmarshallBeans;
import org.junit.Test;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class JaxbBeanTest extends TestCase {
    
    private void printTestBanner(){
        System.out.println("==========================================");
        System.out.println("===  " + TestAppConfig.getCallingMethodName(1) + "  ===");
        System.out.println("==========================================");
    }
    
    @Test 
    public void testMailServiceBean() throws MigrationException {
        printTestBanner();
        
        List<MailServiceBean> beans = unmarshallBeans( 
            new File("testdata/as5configs/01_510all/server/all/deploy/mail-service.xml"), 
            "/server/mbean[@code='org.jboss.mail.MailService']", MailServiceBean.class);
        for( MailServiceBean ms : beans) {
            System.out.println( ms.getJndiName() );
            assertNotNull(ms.getJndiName());
            assertNotNull(ms.getMbeanName());
            assertNotNull(ms.getSmtpHost());
            //assertNotNull(ms.getSmtpUser());
        }
    }
    
    @Test 
    public void testCategoryBean() throws MigrationException {
        printTestBanner();
        
        List<CategoryBean> beans = unmarshallBeans( 
            new File("testdata/as5configs/01_510all/server/all/conf/jboss-log4j.xml"), 
            "/configuration/category", CategoryBean.class); // {http://jakarta.apache.org/log4j/}
        
        assertFalse( beans.isEmpty() );
        for( CategoryBean bean : beans) {
            System.out.println( bean.getCategoryName() );
            assertNotNull( bean.getCategoryName() );
            assertNotNull( bean.getCategoryValue());
        }
    }
    
}// class
