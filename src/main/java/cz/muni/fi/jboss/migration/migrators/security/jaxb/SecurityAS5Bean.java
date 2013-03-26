package cz.muni.fi.jboss.migration.migrators.security.jaxb;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing security policy (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "policy")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "policy")

public class SecurityAS5Bean {

    @XmlElements(@XmlElement(name = "application-policy", type = ApplicationPolicyBean.class))
    private Set<ApplicationPolicyBean> applicationPolicies;

    public Set<ApplicationPolicyBean> getApplicationPolicies() {
        return applicationPolicies;
    }

    public void setApplicationPolicies(Collection<ApplicationPolicyBean> applicationPolicies) {
        Set<ApplicationPolicyBean> temp = new HashSet();
        temp.addAll(applicationPolicies);
        this.applicationPolicies = temp;
    }


}
