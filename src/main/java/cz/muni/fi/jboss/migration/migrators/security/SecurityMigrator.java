package cz.muni.fi.jboss.migration.migrators.security;

import cz.muni.fi.jboss.migration.GlobalConfiguration;
import cz.muni.fi.jboss.migration.MigrationContext;
import cz.muni.fi.jboss.migration.MigrationData;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import javafx.util.Pair;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:42 AM
 */
public class SecurityMigrator implements IMigrator {

    private GlobalConfiguration globalConfig;

    private List<Pair<String,String>> config;

    public SecurityMigrator(GlobalConfiguration globalConfig, List<Pair<String, String>> config){
        this.globalConfig = globalConfig;
        this.config =  config;
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException, FileNotFoundException{
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(SecurityAS5.class).createUnmarshaller();

            File file = new File(globalConfig.getDirAS5() + File.separator + "conf" + File.separator + "login-config.xml");

            if(file.canRead()){
                SecurityAS5 securityAS5 = (SecurityAS5)unmarshaller.unmarshal(file);

                MigrationData mData = new MigrationData();
                mData.getLoadedData().addAll(securityAS5.getApplicationPolicies());

                ctx.getMigrationData().put(SecurityMigrator.class, mData);

            } else {
                throw new FileNotFoundException("Cannot find/open file: " + file.getAbsolutePath());
            }

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
