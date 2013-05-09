/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.deploymentScanner.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * User: rsearls
 * Date: 4/17/13
 */

/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the generated package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

    //private final static QName _Bean_QNAME = new QName("", "bean");
    private final static QName _PropertyTypeInject_QNAME = new QName("", "inject");
    private final static QName _PropertyTypeList_QNAME = new QName("", "list");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: generated
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BeanType }
     *

    public BeanType createBeanType() {
        return new BeanType();
    }
     */
    /**
     * Create an instance of {@link PropertyType }
     *
     */
    public PropertyType createPropertyType() {
        return new PropertyType();
    }

    /**
     * Create an instance of {@link InjectType }
     *
     */
    public InjectType createInjectType() {
        return new InjectType();
    }

    /**
     * Create an instance of {@link ListType }
     *
     */
    public ListType createListType() {
        return new ListType();
    }

    /**
     * Create an instance of {@link ValueType }
     *
     */
    public ValueType createValueType() {
        return new ValueType();
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link BeanType }{@code >}}
     *

    @XmlElementDecl(namespace = "", name = "bean")
    public JAXBElement<BeanType> createBean(BeanType value) {
        return new JAXBElement<BeanType>(_Bean_QNAME, BeanType.class, null, value);
    }
     */
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InjectType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "inject", scope = PropertyType.class)
    public JAXBElement<InjectType> createPropertyTypeInject(InjectType value) {
        return new JAXBElement<InjectType>(_PropertyTypeInject_QNAME, InjectType.class, PropertyType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "list", scope = PropertyType.class)
    public JAXBElement<ListType> createPropertyTypeList(ListType value) {
        return new JAXBElement<ListType>(_PropertyTypeList_QNAME, ListType.class, PropertyType.class, value);
    }

}

