package cz.muni.fi.jboss.Migration.Security;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for marshalling and representing security-domain (AS7)
 *
 * @author: Roman Jakubco
 * Date: 10/2/12
 * Time: 9:04 PM
 */

@XmlRootElement(name = "security-domain")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "security-domain")

public class SecurityDomain{

    @XmlAttribute(name = "name")
    private String securityDomainName;
    @XmlAttribute(name = "cache-type")
    private String cacheType;
    @XmlElementWrapper(name = "authentication")
    @XmlElements(@XmlElement(name = "login-module", type =LoginModuleAS7.class ))
    private Set<LoginModuleAS7> loginModules;

    public String getSecurityDomainName() {
        return securityDomainName;
    }

    public void setSecurityDomainName(String securityDomainName) {
        this.securityDomainName = securityDomainName;
    }

    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    public Set<LoginModuleAS7> getLoginModules() {
        return loginModules;
    }

    public void setLoginModules(Collection<LoginModuleAS7> loginModules) {
        Set<LoginModuleAS7> temp = new HashSet();
        temp.addAll(loginModules);
        this.loginModules = temp;
    }
}