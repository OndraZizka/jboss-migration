package cz.muni.fi.jboss.Migration.Security;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: Roman Jakubco
 * Date: 9/23/12
 * Time: 6:28 PM
 */

@XmlRootElement(name = "subsystem")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "subsystem")

public class SecurityAS7 {

//    @XmlAttribute(name = "xmlns")
//    private String xmlns="urn:jboss:domain:security:1.1";
    @XmlElementWrapper(name = "security-domains")
    @XmlElements(@XmlElement(name = "security-domain", type = SecurityDomain.class))
    private Set<SecurityDomain> securityDomains;

//    public String getXmlns() {
//        return xmlns;
//    }
//
//    public void setXmlns(String xmlns) {
//        this.xmlns = xmlns;
//    }

    public Set<SecurityDomain> getSecurityDomains() {
        return securityDomains;
    }

    public void setSecurityDomains(Collection<SecurityDomain> securityDomains) {
        Set<SecurityDomain> temp = new HashSet();
        temp.addAll(securityDomains);
        this.securityDomains = temp;
    }




}
