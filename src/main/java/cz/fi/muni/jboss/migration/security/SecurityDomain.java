package cz.fi.muni.jboss.migration.security;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * 
 * @author  Roman Jakubco
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
    private Collection<LoginModuleAS7> loginModules;

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

    public Collection<LoginModuleAS7> getLoginModules() {
        return loginModules;
    }

    public void setLoginModules(Collection<LoginModuleAS7> loginModules) {
        this.loginModules = loginModules;
    }
}