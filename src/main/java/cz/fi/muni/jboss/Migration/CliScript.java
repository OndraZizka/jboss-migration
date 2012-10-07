package cz.fi.muni.jboss.Migration;

import cz.fi.muni.jboss.Migration.DataSources.DatasourceAS7;
import cz.fi.muni.jboss.Migration.DataSources.XaDatasourceAS7;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 10/7/12
 * Time: 2:28 PM
 */
public interface CliScript {
    public void createDatasourceScript(DatasourceAS7 datasourceAS7);
    public void createXaDatasourceScript(XaDatasourceAS7 xaDatasourceAS7);
}
