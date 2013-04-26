package org.jboss.loom.migrators.deploymentScanner.jaxb;

import java.util.ArrayList;
import javax.xml.bind.annotation.*;


/**
 *
 */

@XmlRootElement(name = "list")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listType", propOrder = {})
public class ListType {

    @XmlElement(name = "value")
    protected java.util.List<ValueType> value;
    @XmlAttribute(name = "elementClass")
    protected String elementClass;

    /**
     * Gets the value of the value property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the value property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValue().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ValueType }
     *
     *
     */
    public java.util.List<ValueType> getValue() {
        if (value == null) {
            value = new ArrayList<ValueType>();
        }
        return this.value;
    }

    /**
     * Gets the value of the elementClass property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getElementClass() {
        return elementClass;
    }

    /**
     * Sets the value of the elementClass property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setElementClass(String value) {
        this.elementClass = value;
    }

}
