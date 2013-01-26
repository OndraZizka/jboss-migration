package cz.muni.fi.jboss.migration.old;

import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.migrators.connectionFactories.ConnectionFactories;
import cz.muni.fi.jboss.migration.migrators.connectionFactories.ResourceAdaptersSub;
import cz.muni.fi.jboss.migration.migrators.dataSources.DataSources;
import cz.muni.fi.jboss.migration.migrators.dataSources.DatasourceAS7;
import cz.muni.fi.jboss.migration.migrators.dataSources.DatasourcesSub;
import cz.muni.fi.jboss.migration.migrators.dataSources.XaDatasourceAS7;
import cz.muni.fi.jboss.migration.migrators.logging.Logger;
import cz.muni.fi.jboss.migration.migrators.logging.LoggingAS5;
import cz.muni.fi.jboss.migration.migrators.logging.LoggingAS7;
import cz.muni.fi.jboss.migration.migrators.security.SecurityAS5;
import cz.muni.fi.jboss.migration.migrators.security.SecurityAS7;
import cz.muni.fi.jboss.migration.migrators.security.SecurityDomain;
import cz.muni.fi.jboss.migration.migrators.server.*;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Roman Jakubco
 * Date: 8/26/12
 * Time: 3:15 PM
 */
public class main {
    public static void main(String[] args) {
        // Basic implementation for testing
        try {
            final JAXBContext context = JAXBContext.newInstance(DataSources.class) ;
            final JAXBContext context1 = JAXBContext.newInstance(DatasourcesSub.class);
            final JAXBContext context2=JAXBContext.newInstance(ResourceAdaptersSub.class);
            final JAXBContext context3=JAXBContext.newInstance(ConnectionFactories.class);
            final JAXBContext context4=JAXBContext.newInstance(ServerAS5.class);
            final JAXBContext context5=JAXBContext.newInstance(ServerSub.class);
            final JAXBContext context6=JAXBContext.newInstance(SocketBindingGroup.class);
            final JAXBContext context7=JAXBContext.newInstance(LoggingAS5.class);
            final JAXBContext context8=JAXBContext.newInstance(LoggingAS7.class);
            final JAXBContext context9=JAXBContext.newInstance(SecurityAS7.class);
            final JAXBContext context10=JAXBContext.newInstance(SecurityAS5.class);

            Unmarshaller unmarshaller=context.createUnmarshaller();
            Unmarshaller unmarshaller3=context3.createUnmarshaller();
            Unmarshaller unmarshaller4=context4.createUnmarshaller();
            Unmarshaller unmarshaller7=context7.createUnmarshaller();
            Unmarshaller unmarshaller10=context10.createUnmarshaller();

            Collection<DataSources> dataSourcesCollection = new ArrayList<>();

            DataSources dataSources=(DataSources)unmarshaller.unmarshal(new File("datasources.xml"));
            dataSourcesCollection.add(dataSources);

            ServerAS5 serverAS5=(ServerAS5)unmarshaller4.unmarshal(new File("server.xml"));
            LoggingAS5 loggingAS5= (LoggingAS5)unmarshaller7.unmarshal(new File("logging.xml"));
            SecurityAS5 securityAS5=(SecurityAS5)unmarshaller10.unmarshal(new File("security.xml"));
            ConnectionFactories connectionFactories=(ConnectionFactories)unmarshaller3.unmarshal(new File("resourceAdapters.xml"));

            Collection<ConnectionFactories> cont = new ArrayList<>();
            cont.add(connectionFactories);

            Configuration conf = new Configuration();
            GlobalConfiguration global = new GlobalConfiguration();
            global.setDirAS5("/home/Reon/Programing/jboss-5.1.0.GA/server/default");
            conf.setOptions(global);
            MigrationContext ctx = new MigrationContext();
            ctx.createBuilder();
            ServerMigrator migrator = new ServerMigrator(global, null);
            migrator.loadAS5Data(ctx);

            List<Node> nodeList = migrator.generateDomElements(ctx);

            for(Node n : nodeList){
                System.out.println(n.toString());
            }


//            Migration migration=new MigrationImpl(true);
//
//            final StringWriter writer=new StringWriter();
//
//            // Datasource Marshaller
//            Marshaller marshaller=context1.createMarshaller();
//            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
//            marshaller.marshal(migration.datasourceSubMigration(dataSourcesCollection),writer);
//            writer.write("\n\n");
//
//            // Server config Marshaller
//            Marshaller marshaller4=context5.createMarshaller();
//            marshaller4.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
//            marshaller4.marshal(migration.serverMigration(serverAS5) ,writer);
//            writer.write("\n\n");
//
//            // SocketBinding marshaller
//            Marshaller marshaller1 = context6.createMarshaller();
//            marshaller1.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
//            marshaller1.marshal(migration.getSocketBindingGroup(),writer);
//            writer.write("\n\n");
//
//            // Logging marshaller
//            Marshaller marshaller7=context8.createMarshaller();
//            marshaller7.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
//            marshaller7.marshal(migration.loggingMigration(loggingAS5) ,writer);
//             writer.write("\n\n");
//
//            // Security Marshaller
//            Marshaller marshaller10=context9.createMarshaller();
//            marshaller10.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
//            marshaller10.marshal(migration.securityMigration(securityAS5) ,writer);
//            writer.write("\n\n");
//
//            // Resource adapters Marshaller
//            Marshaller marshaller3=context2.createMarshaller();
//            marshaller3.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
//            marshaller3.marshal(migration.resourceAdaptersMigration(cont) ,writer);
//
//            System.out.println(writer.toString());
//
//            CliScript cliScript= new CliScriptImpl();
//            DatasourcesSub datasourcesSub= migration.datasourceSubMigration(dataSourcesCollection);
//            for(DatasourceAS7 datasourceAS7 : datasourcesSub.getDatasource()){
//                System.out.println(cliScript.createDatasourceScript(datasourceAS7));
//            }
//            for(XaDatasourceAS7 xaDatasourceAS7 : datasourcesSub.getXaDatasource()){
//                System.out.println(cliScript.createXaDatasourceScript(xaDatasourceAS7));
//            }
//            //ResourceAdaptersSub connectionFactoriesSub = migration.connectionFactoriesMigration(connectionFactories);
//            //for(ResourceAdapter connectionFactoryAS7 : connectionFactoriesSub.getResourceAdapters()){
//            //   System.out.println(cliScript.createResAdapterScript(connectionFactoryAS7));
//            //}
//            LoggingAS7 loggingAS7 = migration.loggingMigration(loggingAS5);
//            for(Logger logger : loggingAS7.getLoggers()){
//                System.out.println(cliScript.createLoggerScript(logger));
//
//            }
//            SecurityAS7 securityAS7 = migration.securityMigration(securityAS5);
//            for(SecurityDomain securityDomain : securityAS7.getSecurityDomains()){
//                System.out.println(cliScript.createSecurityDomainScript(securityDomain));
//            }
//            ServerSub serverSub = migration.serverMigration(serverAS5);
//            for(ConnectorAS7 connectorAS7 : serverSub.getConnectors()){
//                System.out.println(cliScript.createConnectorScript(connectorAS7));
//            }
//            for(VirtualServer virtualServer : serverSub.getVirtualServers()){
//                System.out.println(cliScript.createVirtualServerScript(virtualServer));
//            }
//            SocketBindingGroup socketBindingGroup= migration.getSocketBindingGroup();
//
//
//            for(SocketBinding socketBinding : socketBindingGroup.getSocketBindings()){
//                System.out.println(cliScript.createSocketBinding(socketBinding));
//            }
//
//            System.out.println(cliScript.createHandlersScript(loggingAS7));




        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
