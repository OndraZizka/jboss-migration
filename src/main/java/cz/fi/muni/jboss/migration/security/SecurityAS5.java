package cz.fi.muni.jboss.migration.security;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * 
 * @author  Roman Jakubco
 * Date: 9/23/12
 * Time: 6:28 PM
 */
@XmlRootElement(name = "policy")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "policy")
public class SecurityAS5 {
      @XmlElements(@XmlElement(name = "application-policy", type = ApplicationPolicy.class))
      private Collection<ApplicationPolicy> applicationPolicies;

    public Collection<ApplicationPolicy> getApplicationPolicies() {
        return applicationPolicies;
    }

    public void setApplicationPolicies(Collection<ApplicationPolicy> applicationPolicies) {
        this.applicationPolicies = applicationPolicies;
    }





}
