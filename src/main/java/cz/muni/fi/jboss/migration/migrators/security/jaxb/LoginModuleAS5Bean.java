package cz.muni.fi.jboss.migration.migrators.security.jaxb;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing login-module (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "login-module")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "login-module")

public class LoginModuleAS5Bean {
    @XmlAttribute(name = "code")
    private String loginModule;

    @XmlAttribute(name = "flag")
    private String loginModuleFlag;

    @XmlElements(@XmlElement(name = "module-option", type = ModuleOptionAS5Bean.class))
    private Set<ModuleOptionAS5Bean> moduleOptions;

    public String getLoginModule() {
        return loginModule;
    }

    public void setLoginModule(String loginModule) {
        this.loginModule = loginModule;
    }

    public Set<ModuleOptionAS5Bean> getModuleOptions() {
        return moduleOptions;
    }

    public void setModuleOptions(Collection<ModuleOptionAS5Bean> moduleOptions) {
        Set<ModuleOptionAS5Bean> temp = new HashSet();
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
