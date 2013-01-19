package cz.muni.fi.jboss.migration.security;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing application-policy (AS5)
 *
 * @author Roman Jakubco
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
    private Set<LoginModuleAS5> loginModules;

    public String getApplicationPolicyName() {
        return applicationPolicyName;
    }

    public void setApplicationPolicyName(String applicationPolicyName) {
        this.applicationPolicyName = applicationPolicyName;
    }

    public Set<LoginModuleAS5> getLoginModules() {
        return loginModules;
    }

    public void setLoginModules(Collection<LoginModuleAS5> loginModules) {
        Set<LoginModuleAS5> temp = new HashSet();
        temp.addAll(loginModules);
        this.loginModules = temp;
    }
}

