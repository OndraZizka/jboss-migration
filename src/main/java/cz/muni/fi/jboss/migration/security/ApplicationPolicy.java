package cz.muni.fi.jboss.migration.security;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * 
 * @author  Roman Jakubco
 * Date: 10/2/12
 * Time: 9:03 PM
 */
@XmlRootElement(name = "application-policy")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "application-policy")
public  class ApplicationPolicy{
    @XmlAttribute(name = "name")
    private String applicationPolicyName;
    @XmlElementWrapper(name = "authentication")
    @XmlElements( @XmlElement(name = "login-module", type = LoginModuleAS5.class))
    private Collection<LoginModuleAS5> loginModules;

    public String getApplicationPolicyName() {
        return applicationPolicyName;
    }

    public void setApplicationPolicyName(String applicationPolicyName) {
        this.applicationPolicyName = applicationPolicyName;
    }

    public Collection<LoginModuleAS5> getLoginModules() {
        return loginModules;
    }

    public void setLoginModules(Collection<LoginModuleAS5> loginModules) {
        this.loginModules = loginModules;
    }
}

