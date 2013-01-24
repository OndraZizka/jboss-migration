package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.ex.ApplyMigrationException;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.migrators.connectionFactories.ResAdapterMigrator;
import cz.muni.fi.jboss.migration.migrators.dataSources.DatasourceMigrator;
import cz.muni.fi.jboss.migration.migrators.logging.LoggingMigrator;
import cz.muni.fi.jboss.migration.migrators.security.SecurityMigrator;
import cz.muni.fi.jboss.migration.migrators.server.ServerMigrator;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import org.eclipse.persistence.exceptions.JAXBException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:25 AM
 */


//// Performs the whole process. This is the "X" on the picture.
//class Migrator {
//
//    private Configuration config;
//    private MigrationContext ctx;
//
//    private List<IMigrator> migrators = createMigrators();
//
//    // This way it will be simpler than instantiating from Class... We can add that later.
//    private static List<IMigrator> createMigrators(){
//        List<IMigrator> migrators = new LinkedList();
//        migrators.add( new DatasourceMigrator(
//                this.config.getGlobal(),
//                this.config.getForMigrator(DatasourceMigrator.class)
//        ) );
//        migrators.add( ... );
//        migrators.add( ... );
//        return migrators;
//    }
//
//
//    public Migrator( Configuration config, MigrationContext ctx ){ ... }
//
//
//    public void init(){
//    }
//
//    public void loadAS5Data() throws LoadMigrationException {
//        for( IMigrator mig : this.migrators ){
//            mig.loadAS5Data( this.ctx );
//        }
//    }
//
//    public void apply() ApplyMigrationException {
//        for( IMigrator mig : this.migrators ){
//            mig.apply( this.ctx );
//        }
//    }
//
//    public List<DOMElement> generateDomElements(){ ... }
//
//    public List<String> generateCliCommands(){ ... }
//
//}
public class Migrator {

    private Configuration config;

    private MigrationContext ctx;

    private List<IMigrator> migrators = createMigrators();

    public Migrator(Configuration config, MigrationContext context){
        this.config = config;
        ctx = context;
    }

    private List<IMigrator> createMigrators(){
        List<IMigrator> migrators = new LinkedList();
        migrators.add( new DatasourceMigrator(this.config.getGlobal(), this.config.getForMigrator(DatasourceMigrator.class)));
        migrators.add( new ResAdapterMigrator(this.config.getGlobal(), this.config.getForMigrator(ResAdapterMigrator.class)));
        migrators.add( new SecurityMigrator(this.config.getGlobal(), this.config.getForMigrator(SecurityMigrator.class)));
        migrators.add( new LoggingMigrator(this.config.getGlobal(), this.config.getForMigrator(LoggingMigrator.class)));
        migrators.add( new ServerMigrator(this.config.getGlobal(), this.config.getForMigrator(ServerMigrator.class)));

        return migrators;
    }

    public void init(){

    }

    public void loadAS5Data() throws LoadMigrationException {
        try {
            for(IMigrator mig : migrators){
                mig.loadAS5Data(this.ctx);
            }
        } catch (JAXBException e) {
            throw new LoadMigrationException(e);
        } catch (FileNotFoundException e){
            throw new LoadMigrationException(e);
        }
    }

    public void apply() throws ApplyMigrationException {
          for(IMigrator mig : migrators){
              mig.apply(this.ctx);
          }
    }

    public List<Element> getDOMElements(){
        return null;
    }

    public List<String> getCLIScripts(){
        return  null;
    }
}
