package cz.muni.fi.jboss.migration.migrators.logging;

import cz.muni.fi.jboss.migration.GlobalConfiguration;
import cz.muni.fi.jboss.migration.MigrationContext;
import cz.muni.fi.jboss.migration.MigrationData;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import javafx.util.Pair;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.List;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:42 AM
 */
public class LoggingMigrator implements IMigrator {

    private GlobalConfiguration globalConfig;

    private List<Pair<String,String>> config;

    public LoggingMigrator(GlobalConfiguration globalConfig, List<Pair<String,String>> config){
        this.globalConfig = globalConfig;
        this.config =  config;
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException, FileNotFoundException{
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(LoggingAS5.class).createUnmarshaller();

            File file = new File(globalConfig.getDirAS5() + File.separator + "conf" + File.separator + "jboss-log4j.xml");

            LoggingAS5 loggingAS5;

            if(file.canRead()){
               loggingAS5 = (LoggingAS5)unmarshaller.unmarshal(file);
            }else{
                throw new FileNotFoundException("Cannot find/open file: " + file.getAbsolutePath());
            }

            MigrationData mData = new MigrationData();
            mData.getLoadedData().addAll(loggingAS5.getCategories());
            mData.getLoadedData().addAll(loggingAS5.getAppenders());
            mData.getLoadedData().add(loggingAS5.getRootLoggerAS5());

            ctx.getMigrationData().put(LoggingMigrator.class, mData);



        } catch (JAXBException e) {
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
