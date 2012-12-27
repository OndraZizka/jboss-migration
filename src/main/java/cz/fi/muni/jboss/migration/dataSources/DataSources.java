package cz.fi.muni.jboss.migration.dataSources; /**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 8/26/12
 * Time: 1:12 PM
 */

import javax.xml.bind.annotation.*;
import java.util.Collection;

@XmlRootElement(name = "datasources" )
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "datasources")
public class DataSources {
    @XmlElements(@XmlElement(name = "local-tx-datasource", type = DatasourceAS5.class))
    private Collection<DatasourceAS5> localDatasourceAS5s;
    @XmlElements(@XmlElement(name = "xa-datasource", type = XaDatasourceAS5.class))
    private Collection<XaDatasourceAS5> xaDatasourceAS5s;

    public Collection<XaDatasourceAS5> getXaDatasourceAS5s() {
        return xaDatasourceAS5s;
    }

    public void setXaDatasourceAS5s(Collection<XaDatasourceAS5> xaDatasourceAS5s) {
        this.xaDatasourceAS5s = xaDatasourceAS5s;
    }

    public Collection<DatasourceAS5> getLocalDatasourceAS5s() {
        return localDatasourceAS5s;
    }

    public void setLocalDatasourceAS5s(Collection<DatasourceAS5> localDatasourceAS5s) {
        this.localDatasourceAS5s = localDatasourceAS5s;
    }

    @Override
    public String toString() {
        return "DataSources{" +
                "localDatasourceAS5s=" + localDatasourceAS5s +
                ",\n xaDatasourceAS5s=" + xaDatasourceAS5s +
                '}';
    }
}
