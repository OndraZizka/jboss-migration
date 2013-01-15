package cz.muni.fi.jboss.Migration.Security;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: Roman Jakubco
 * Date: 10/2/12
 * Time: 9:04 PM
 */

@XmlRootElement(name = "login-module")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "login-module")

public  class LoginModuleAS5{
    @XmlAttribute(name = "code")
    private String loginModule;
    @XmlAttribute(name = "flag")
    private String loginModuleFlag;
    @XmlElements(@XmlElement(name = "module-option", type = ModuleOptionAS5.class))
    private Set<ModuleOptionAS5> moduleOptions;

    public String getLoginModule() {
        return loginModule;
    }

    public void setLoginModule(String loginModule) {
        this.loginModule = loginModule;
    }

    public Set<ModuleOptionAS5> getModuleOptions() {
        return moduleOptions;
    }

    public void setModuleOptions(Collection<ModuleOptionAS5> moduleOptions) {
        Set<ModuleOptionAS5> temp = new HashSet();
        temp.addAll(moduleOptions);
        this.moduleOptions = temp;
    }

    public String getLoginModuleFlag() {
        return loginModuleFlag;
    }

    public void setLoginModuleFlag(String loginModuleFlag) {
        this.loginModuleFlag = loginModuleFlag;
    }
}
