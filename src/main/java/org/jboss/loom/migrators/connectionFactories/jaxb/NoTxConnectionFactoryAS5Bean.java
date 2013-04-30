package org.jboss.loom.migrators.connectionFactories.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Class for unmarshalling and representing no-tx-connection-factory (AS5)
 *
 * @author Roman Jakubco
 */
@XmlRootElement(name = "no-tx-connection-factory")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "no-tx-connection-factory")

public class NoTxConnectionFactoryAS5Bean extends AbstractConnectionFactoryAS5Bean {

}
