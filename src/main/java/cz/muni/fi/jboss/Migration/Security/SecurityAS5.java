package cz.muni.fi.jboss.Migration.Security;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing security policy (AS5)
 *
 * @author: Roman Jakubco
 * Date: 9/23/12
 * Time: 6:28 PM
 */

@XmlRootElement(name = "policy")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "policy")

public class SecurityAS5 {

      @XmlElements(@XmlElement(name = "application-policy", type = ApplicationPolicy.class))
      private Set<ApplicationPolicy> applicationPolicies;

    public Set<ApplicationPolicy> getApplicationPolicies() {
        return applicationPolicies;
    }

    public void setApplicationPolicies(Collection<ApplicationPolicy> applicationPolicies) {
        Set<ApplicationPolicy> temp = new HashSet();
        temp.addAll(applicationPolicies);
        this.applicationPolicies = temp;
    }





}
