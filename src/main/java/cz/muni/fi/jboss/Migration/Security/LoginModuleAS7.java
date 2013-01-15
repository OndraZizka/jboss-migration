package cz.muni.fi.jboss.Migration.Security;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: Roman Jakubco
 * Date: 10/2/12
 * Time: 9:05 PM
 */

@XmlRootElement(name = "login-module")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "login-module")

public class LoginModuleAS7{

    @XmlAttribute(name = "code")
    private String loginModuleCode;
    @XmlAttribute(name = "flag")
    private String loginModuleFlag;
    @XmlElements(@XmlElement(name = "module-option", type = ModuleOptionAS7.class))
    private Set<ModuleOptionAS7> moduleOptions;

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

    public Set<ModuleOptionAS7> getModuleOptions() {
        return moduleOptions;
    }

    public void setModuleOptions(Collection<ModuleOptionAS7> moduleOptions) {
        Set<ModuleOptionAS7> temp = new HashSet();
        temp.addAll(moduleOptions);
        this.moduleOptions = temp;
    }
}

