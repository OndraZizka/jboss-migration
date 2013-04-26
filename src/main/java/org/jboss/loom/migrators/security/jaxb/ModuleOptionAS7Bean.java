package org.jboss.loom.migrators.security.jaxb;

import javax.xml.bind.annotation.*;

/**
 * Class for marshalling and representing module-option (AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "module-option")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "module-option")

public class ModuleOptionAS7Bean {

    @XmlAttribute(name = "name")
    private String moduleOptionName;

    @XmlAttribute(name = "value")
    private String moduleOptionValue;


    public ModuleOptionAS7Bean() {
    }


    public ModuleOptionAS7Bean( String moduleOptionName, String moduleOptionValue ) {
        this.moduleOptionName = moduleOptionName;
        this.moduleOptionValue = moduleOptionValue;
    }

    
    
    
    public String getModuleOptionName() {
        return moduleOptionName;
    }

    public void setModuleOptionName(String moduleOptionName) {
        this.moduleOptionName = moduleOptionName;
    }

    public String getModuleOptionValue() {
        return moduleOptionValue;
    }

    public void setModuleOptionValue(String moduleOptionValue) {
        this.moduleOptionValue = moduleOptionValue;
    }
}