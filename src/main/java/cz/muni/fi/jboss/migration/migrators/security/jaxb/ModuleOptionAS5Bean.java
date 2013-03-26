package cz.muni.fi.jboss.migration.migrators.security.jaxb;

import javax.xml.bind.annotation.*;

/**
 * Class for unmarshalling and representing module-option (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "module-option")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "module-option")

public class ModuleOptionAS5Bean {

    @XmlValue
    private String moduleValue;

    @XmlAttribute(name = "name")
    private String moduleName;

    public String getModuleValue() {
        return moduleValue;
    }

    public void setModuleValue(String moduleValue) {
        this.moduleValue = moduleValue;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
}
