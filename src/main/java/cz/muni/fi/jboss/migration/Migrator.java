package cz.muni.fi.jboss.migration;

import cz.muni.fi.jboss.migration.ex.*;
import cz.muni.fi.jboss.migration.migrators.connectionFactories.ResAdapterMigrator;
import cz.muni.fi.jboss.migration.migrators.dataSources.DatasourceMigrator;
import cz.muni.fi.jboss.migration.migrators.logging.LoggingMigrator;
import cz.muni.fi.jboss.migration.migrators.security.SecurityMigrator;
import cz.muni.fi.jboss.migration.migrators.server.ServerMigrator;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.eclipse.persistence.exceptions.JAXBException;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Migrator is class, which represents all functions of the application.
 *
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:25 AM
 */

public class Migrator {

    private Configuration config;

    private MigrationContext ctx;

    private List<IMigrator> migrators ;

    public Migrator(Configuration config, MigrationContext context){
        this.config = config;
        ctx = context;
        migrators = createMigrators();
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
        }
    }

    public void apply() throws ApplyMigrationException {
        for(IMigrator mig : migrators){
            mig.apply(this.ctx);
        }
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult result = new StreamResult( new File(config.getGlobal().getStandaloneFilePath()));
            //StreamResult result = new StreamResult(System.out);
            DOMSource source = new DOMSource(ctx.getStandaloneDoc());
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }


    }


    public List<Node> getDOMElements() throws MigrationException{
        List<Node> elements = new ArrayList<>();
        for(IMigrator mig : migrators){
            elements.addAll(mig.generateDomElements(this.ctx));
        }

        return elements;
    }

    public List<String> getCLIScripts() throws CliScriptException{
        List<String> scripts = new ArrayList<>();
        for(IMigrator mig : migrators){
            scripts.addAll(mig.generateCliScripts(this.ctx));
        }

        return scripts;
    }

    public void copyItems() throws CopyException{
        String targetPath = config.getGlobal().getDirAS7();
        File dir = new File(config.getGlobal().getDirAS5() + File.separator + config.getGlobal().getProfileAS5());
        for(CopyMemory cp : ctx.getCopyMemories()){
            if(cp.getName() == null || cp.getName().isEmpty()){
                throw new NullPointerException();
            }

            NameFileFilter nff;
            if(cp.getType().equals("driver")){
                final String name = cp.getName();
                nff = new NameFileFilter(name){
                    @Override
                    public boolean accept(File file) {
                        if(file.getName().contains(name)){
                            if(file.getName().contains("jar")){
                                return true;
                            } else{
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                };
            } else{
                nff = new NameFileFilter(cp.getName());
            }

            List<File> list = (List<File>) FileUtils.listFiles(dir, nff, FileFilterUtils.makeCVSAware(null));
            switch(cp.getType()){
                case "driver":{
                    // TODO:Can there be only one jar of selected driver or many different versions?
                    if(list.isEmpty()){
                        throw new CopyException("Cannot locate driver jar for driver:" + cp.getName() + "!",
                                new FileNotFoundException(cp.getName()));
                    } else{
                        cp.setHomePath(list.get(0).getAbsolutePath());
                        cp.setName(list.get(0).getName());
                        String module = "";
                        // TODO: need better idea for module creating
                        if(cp.getModule() != null){
                            String[] parts = cp.getModule().split("\\.");
                            module = "";
                            for(String s : parts){
                                module = module + s + File.separator;
                            }
                            cp.setTargetPath(targetPath + File.separator + "modules" + File.separator + module  + "main");
                        } else{
                            throw new CopyException("Error: Module for driver is null!");
                        }
                    }
                } break;
                case "log":{
                    if(list.isEmpty()){
                        throw  new NullPointerException("Cannot locate log file: " + cp.getName() + "!");
                    } else{
                        cp.setHomePath(list.get(0).getAbsolutePath());
                        cp.setTargetPath(targetPath + File.separator + "standalone" + File.separator +"log" );
                    }
                } break;
                case "security":{
                    if(list.isEmpty()){
                        throw  new CopyException("Cannot locate security file: " + cp.getName() + "!",
                                new FileNotFoundException(cp.getName()));
                    } else{
                        cp.setHomePath(list.get(0).getAbsolutePath());
                        cp.setTargetPath(targetPath + File.separator + "standalone" + File.separator + "configuration");
                    }
                } break;
                case "resource":{
                    if(list.isEmpty()){
                        throw  new CopyException("Cannot locate security file: " + cp.getName() + "!",
                                new FileNotFoundException(cp.getName()));
                    } else{
                        cp.setHomePath(list.get(0).getAbsolutePath());
                        cp.setTargetPath(targetPath + File.separator + "standalone" + File.separator + "deployments");
                    }
                } break;
            }
        }

        try {
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

            for(CopyMemory cp : ctx.getCopyMemories()){
                if(cp.getType().equals("driver")){
                    File directories = new File(cp.getTargetPath() + File.separator);
                    FileUtils.forceMkdir(directories);
                    File module = new File(directories.getAbsolutePath() + File.separator + "module.xml");
                    module.createNewFile();
                    transformer.transform(new DOMSource(cp.createModuleXML()), new StreamResult(module));
                }
                FileUtils.copyFileToDirectory(new File(cp.getHomePath()), new File(cp.getTargetPath()));
            }
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            throw new CopyException(e);
        }
    }
}
