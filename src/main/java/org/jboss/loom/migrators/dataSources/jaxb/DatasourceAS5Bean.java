package org.jboss.loom.migrators.dataSources.jaxb;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Class for unmarshalling and representing local-tx-datasource (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "local-tx-datasource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "local-tx-datasource")

public class DatasourceAS5Bean extends AbstractDatasourceAS5Bean{


}// class
