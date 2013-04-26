package org.jboss.loom.migrators.security.jaxb;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for marshalling and representing login-module (AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "login-module")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "login-module")

public class LoginModuleAS7Bean {

    @XmlAttribute(name = "code")
    private String loginModuleCode;

    @XmlAttribute(name = "flag")
    private String loginModuleFlag;

    @XmlElements(@XmlElement(name = "module-option", type = ModuleOptionAS7Bean.class))
    private Set<ModuleOptionAS7Bean> moduleOptions;

    public String getLoginModuleCode() {
        return loginModuleCode;
    }

    public void setLoginModuleCode(String loginModuleCode) {
        this.loginModuleCode = loginModuleCode;
    }

    public String getLoginModuleFlag() {
        return loginModuleFlag;
    }

    public void setLoginModuleFlag(String loginModuleFlag) {
        this.loginModuleFlag = loginModuleFlag;
    }

    public Set<ModuleOptionAS7Bean> getModuleOptions() {
        return moduleOptions;
    }

    public void setModuleOptions(Collection<ModuleOptionAS7Bean> moduleOptions) {
        Set<ModuleOptionAS7Bean> temp = new HashSet();
        temp.addAll(moduleOptions);
        this.moduleOptions = temp;
    }
}

