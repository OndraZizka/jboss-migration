package org.jboss.loom.migrators.connectionFactories.jaxb;

import javax.xml.bind.annotation.*;

/**
 * Class for unmarshalling and representing tx-connection-factory (AS5)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "tx-connection-factory")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "tx-connection-factory")

public class ConnectionFactoryAS5Bean extends AbstractConnectionFactoryAS5Bean {

    //*
    @XmlElement(name = "local-transaction")
    private String localTransaction;
    //*
    @XmlElement(name = "xa-transaction")
    private String xaTransaction;
    //*
    @XmlElement(name = "no-tx-separate-pools")
    private String noTxSeparatePools;


    //*
    @XmlElement(name = "xa-resource-timeout")
    private String xaResourceTimeout;


    public String getXaTransaction() {
        return xaTransaction;
    }

    public void setXaTransaction(String xaTransaction) {
        this.xaTransaction = xaTransaction;
    }

    public String getLocalTransaction() {
        return localTransaction;
    }

    public void setLocalTransaction(String localTransaction) {
        this.localTransaction = localTransaction;
    }

    public String getNoTxSeparatePools() {
        return noTxSeparatePools;
    }

    public void setNoTxSeparatePools(String noTxSeparatePools) {
        this.noTxSeparatePools = noTxSeparatePools;
    }


    public String getXaResourceTimeout() {
        return xaResourceTimeout;
    }

    public void setXaResourceTimeout(String xaResourceTimeout) {
        this.xaResourceTimeout = xaResourceTimeout;
    }


}
