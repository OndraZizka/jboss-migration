package org.jboss.loom.migrators.deploymentScanner.jaxb;

import javax.xml.bind.annotation.*;


/**
 * User: rsearls
 * Date: 4/17/13
 */

@XmlRootElement(name = "injectType")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "injectType", propOrder = {"value"})

public class InjectType {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "bean")
    protected String bean;

    /**
     * Gets the value of the value property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the bean property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getBean() {
        return bean;
    }

    /**
     * Sets the value of the bean property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setBean(String value) {
        this.bean = value;
    }

}

