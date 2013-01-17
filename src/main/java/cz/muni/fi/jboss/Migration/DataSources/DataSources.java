package cz.muni.fi.jboss.Migration.DataSources;


import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for unmarshalling and representing datasources (AS5)
 *
 * @author: Roman Jakubco
 * Date: 8/26/12
 * Time: 1:12 PM
 */

@XmlRootElement(name = "datasources" )
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "datasources")

public class DataSources {

    @XmlElements(@XmlElement(name = "local-tx-datasource", type = DatasourceAS5.class))
    private Set<DatasourceAS5> localDatasourceAS5s;
    @XmlElements(@XmlElement(name = "xa-datasource", type = XaDatasourceAS5.class))
    private Set<XaDatasourceAS5> xaDatasourceAS5s;

    public Set<XaDatasourceAS5> getXaDatasourceAS5s() {
        return xaDatasourceAS5s;
    }

    public void setXaDatasourceAS5s(Collection<XaDatasourceAS5> xaDatasourceAS5s) {
        Set<XaDatasourceAS5> temp = new HashSet();
        temp.addAll(xaDatasourceAS5s);
        this.xaDatasourceAS5s = temp;
    }

    public Set<DatasourceAS5> getLocalDatasourceAS5s() {
        return localDatasourceAS5s;
    }

    public void setLocalDatasourceAS5s(Collection<DatasourceAS5> localDatasourceAS5s) {
        Set<DatasourceAS5> temp = new HashSet();
        temp.addAll(localDatasourceAS5s);
        this.localDatasourceAS5s = temp;
    }
}
