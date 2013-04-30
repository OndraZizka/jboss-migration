package org.jboss.loom.migrators.dataSources.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Class for unmarshalling and representing no-tx-datasource (AS5)
 *
 * @author Roman Jakubco
 * 
 *  TODO: Many of properties are identical across 3 types of datasources.
 *        Move them into a parent class.
 */
@XmlRootElement(name = "no-tx-datasource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "no-tx-datasource")
public class NoTxDatasourceAS5Bean extends AbstractDatasourceAS5Bean {

}