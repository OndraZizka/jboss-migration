package cz.fi.muni.jboss.migration.security;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
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
    private Collection<ModuleOptionAS5> moduleOptions;

    public String getLoginModule() {
        return loginModule;
    }

    public void setLoginModule(String loginModule) {
        this.loginModule = loginModule;
    }

    public Collection<ModuleOptionAS5> getModuleOptions() {
        return moduleOptions;
    }

    public void setModuleOptions(Collection<ModuleOptionAS5> moduleOptions) {
        this.moduleOptions = moduleOptions;
    }

    public String getLoginModuleFlag() {
        return loginModuleFlag;
    }

    public void setLoginModuleFlag(String loginModuleFlag) {
        this.loginModuleFlag = loginModuleFlag;
    }
}
