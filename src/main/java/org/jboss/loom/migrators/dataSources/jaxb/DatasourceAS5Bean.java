package org.jboss.loom.migrators.dataSources.jaxb;


import javax.xml.bind.annotation.*;

/**
 * Class for unmarshalling and representing local-tx-datasource (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "local-tx-datasource")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "local-tx-datasource")

public class DatasourceAS5Bean extends AbstractDatasourceAS5Bean{

    @XmlElement(name = "transaction-isolation")
    private String transIsolation;


    public String getTransIsolation() {
        return transIsolation;
    }

    public void setTransIsolation(String transIsolation) {
        this.transIsolation = transIsolation;
    }
}// class
