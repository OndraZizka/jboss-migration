package cz.muni.fi.jboss.migration.migrators.security.jaxb;

import cz.muni.fi.jboss.migration.spi.IConfigFragment;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing application-policy (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "application-policy")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "application-policy")

public class ApplicationPolicyBean implements IConfigFragment {

    @XmlAttribute(name = "name")
    private String applicationPolicyName;

    @XmlElementWrapper(name = "authentication")
    @XmlElements(@XmlElement(name = "login-module", type = LoginModuleAS5Bean.class))
    private Set<LoginModuleAS5Bean> loginModules;

    public String getApplicationPolicyName() {
        return applicationPolicyName;
    }

    public void setApplicationPolicyName(String applicationPolicyName) {
        this.applicationPolicyName = applicationPolicyName;
    }

    public Set<LoginModuleAS5Bean> getLoginModules() {
        return loginModules;
    }

    public void setLoginModules(Collection<LoginModuleAS5Bean> loginModules) {
        Set<LoginModuleAS5Bean> temp = new HashSet();
        temp.addAll(loginModules);
        this.loginModules = temp;
    }
}

