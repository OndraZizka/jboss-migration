package cz.muni.fi.jboss.migration.migrators.dataSources;

import cz.muni.fi.jboss.migration.GlobalConfiguration;
import cz.muni.fi.jboss.migration.MigrationContext;
import cz.muni.fi.jboss.migration.MigrationData;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:41 AM
 */
public class DatasourceMigrator implements IMigrator {

    private GlobalConfiguration globalConfig;

    private List<Pair<String,String>> config;

    public DatasourceMigrator(GlobalConfiguration globalConfig, List<Pair<String,String>> config){
        this.globalConfig = globalConfig;
        this.config =  config;
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException{
        try {
            Unmarshaller dataUnmarshaller = JAXBContext.newInstance(DataSources.class).createUnmarshaller();
            List<DataSources> dsColl = new ArrayList();

            File dsFiles = new File(globalConfig.getDirAS5() + File.separator + "deploy" );

            if(dsFiles.canRead()){
                SuffixFileFilter sf = new SuffixFileFilter("-ds.xml");
                List<File> list = (List<File>) FileUtils.listFiles(dsFiles, sf, null);

                if(list.isEmpty()){
                    throw new LoadMigrationException("No \"-ds.xml\" to parse!");
                }

                for(int i = 0; i < list.size() ; i++){
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(list.get(i));

                    Element element = doc.getDocumentElement();

                    if(element.getTagName().equalsIgnoreCase("datasources")){
                        DataSources dataSources = (DataSources)dataUnmarshaller.unmarshal(list.get(i));
                        dsColl.add(dataSources);
                    }
                }
            } else {
                throw new LoadMigrationException("Error: don't have permission for reading files in directory \"AS5_Home"
                        + File.separator+"deploy\"");

            }

            MigrationData mData = new MigrationData();

            for(DataSources ds : dsColl){
                mData.getLoadedData().addAll(ds.getLocalDatasourceAS5s());
                mData.getLoadedData().addAll(ds.getXaDatasourceAS5s());
            }

            ctx.getMigrationData().put(DatasourceMigrator.class, mData);

        } catch (JAXBException e) {
            throw new LoadMigrationException(e);
        } catch (ParserConfigurationException e) {
            throw new LoadMigrationException(e);
        } catch (SAXException e) {
            throw new LoadMigrationException(e);
        } catch (IOException e) {
            throw new LoadMigrationException(e);
        }

    }

    @Override
    public void apply(MigrationContext ctx) {

    }

    @Override
    public void migrate(MigrationContext ctx) {

    }
}
