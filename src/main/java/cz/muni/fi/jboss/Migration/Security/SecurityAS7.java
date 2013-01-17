package cz.muni.fi.jboss.Migration.Security;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for marshalling and representing security subsystem (AS7)
 *
 * @author: Roman Jakubco
 * Date: 9/23/12
 * Time: 6:28 PM
 */

@XmlRootElement(name = "security-domains")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "subsystem")

public class SecurityAS7 {

    @XmlElements(@XmlElement(name = "security-domain", type = SecurityDomain.class))
    private Set<SecurityDomain> securityDomains;

    public Set<SecurityDomain> getSecurityDomains() {
        return securityDomains;
    }

    public void setSecurityDomains(Collection<SecurityDomain> securityDomains) {
        Set<SecurityDomain> temp = new HashSet();
        temp.addAll(securityDomains);
        this.securityDomains = temp;
    }




}
