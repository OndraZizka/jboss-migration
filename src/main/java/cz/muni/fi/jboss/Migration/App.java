package cz.muni.fi.jboss.Migration;

import cz.muni.fi.jboss.Migration.ConnectionFactories.ConnectionFactories;
import cz.muni.fi.jboss.Migration.ConnectionFactories.ResourceAdapter;
import cz.muni.fi.jboss.Migration.ConnectionFactories.ResourceAdaptersSub;
import cz.muni.fi.jboss.Migration.DataSources.*;
import cz.muni.fi.jboss.Migration.Logging.Logger;
import cz.muni.fi.jboss.Migration.Logging.LoggingAS5;
import cz.muni.fi.jboss.Migration.Logging.LoggingAS7;
import cz.muni.fi.jboss.Migration.Security.SecurityAS5;
import cz.muni.fi.jboss.Migration.Security.SecurityAS7;
import cz.muni.fi.jboss.Migration.Security.SecurityDomain;
import cz.muni.fi.jboss.Migration.Server.ConnectorAS7;
import cz.muni.fi.jboss.Migration.Server.ServerAS5;
import cz.muni.fi.jboss.Migration.Server.ServerSub;
import cz.muni.fi.jboss.Migration.Server.SocketBindingGroup;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import org.w3c.dom.*;
import org.w3c.dom.Element;
import org.xml.sax.*;

import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

/**
 * Class representing UI of the application
 *
 * @author: Roman Jakubco
 * Date: 10/3/12
 * Time: 1:36 PM
 */

public class App {

    public static Document insertIntoXml(Document doc, Node node, String name) throws  ParserConfigurationException,
            IOException, SAXException, TransformerException {

        NodeList parents = doc.getElementsByTagName("subsystem");
        for(int i = 0; i < parents.getLength(); i++){
            if( !(parents.item(i) instanceof Element)) {
                continue;
            }

            Node parent = parents.item(i);
            NamedNodeMap atts = parent.getAttributes();

            for(int k = 0; k < atts.getLength(); k++){
                String nameSpace = atts.item(k).getNodeValue();

                if(nameSpace.contains(name) && !nameSpace.contains("webservices")){
                    NodeList appenders = node.getChildNodes();
                    for(int j = 0; j < appenders.getLength(); j++){
                        if( !(appenders.item(j) instanceof Element)) {
                            continue;
                        }

                        Node appender = appenders.item(j);

                        if(appender.getNodeName().equals("drivers") || appender.getNodeName().equals("root-logger")){
                            continue;
                        }

                        Node adopted = doc.adoptNode(appender.cloneNode(true));

                        if(name.equals("datasource") || name.equals("security")){
                            parent = parents.item(i).getFirstChild();
                            while( !(parent instanceof Element)) {
                                parent = parents.item(i).getFirstChild().getNextSibling();
                            }
                            Node lastNode = parent.getLastChild();
                            while(!(lastNode instanceof Element)){
                                 lastNode = parent.getLastChild().getPreviousSibling();
                            }
                            parent.insertBefore(adopted, lastNode);
                            continue;
                        }

                        if(name.equals("web")){
                            Node lastNode = parent.getLastChild();
                            while(!(lastNode instanceof Element)){
                                lastNode = parent.getLastChild().getPreviousSibling();
                            }
                            parent.insertBefore(adopted, lastNode);
                            continue;
                        }

                        parent.appendChild(adopted);


                    }
                }
            }
        }
       return  doc;
    }

    public static void  insertDriver(Document doc, Node node) throws  ParserConfigurationException,
            IOException, SAXException, TransformerException{
        NodeList  drivers = doc.getElementsByTagName("drivers");
        for(int i = 0; i < drivers.getLength(); i++){
            if( !(drivers.item(i) instanceof Element)) {
                continue;
            }

            NodeList appenders = node.getChildNodes();

            for(int j = 0; j < appenders.getLength(); j++){
                if( !(appenders.item(j) instanceof Element)) {
                    continue;
                }

                Node appender = appenders.item(j);
                Node adopted = doc.adoptNode(appender.cloneNode(true));
                drivers.item(i).appendChild(adopted);
            }
        }
    }

    public static void main(String[] args) {
        File temp = null;
        String home = "";
        String target =  "";
        String serverName = "";
        String subs = "";
        boolean xml =  true;
        boolean cli = true;
        boolean log = false;
        boolean data = false;
        boolean security = false;
        boolean server = false;
        boolean resource = false;
        boolean copy = true;
        boolean subsystem = false;
        boolean merge = false;
        String cmd = "";
        File directory = new File(".");
        BufferedWriter out = null;
        String xmlOut = "outXml.xml";

        try {
            for(int i = 0; i < args.length; i++){
                cmd = cmd.concat(args[i]);
            }

            String[] commands = cmd.split("--");

            if(commands.length == 1){
                if(commands[0].equals("help")){
                    System.out.println("help");
                    return;
                }

                if(commands[0].startsWith("target")){
                    int index = commands[0].indexOf("=");
                    target = commands[0].substring(index);

                }

            } else{
                for(int i = 0; i < commands.length; i++){
                    if(commands[i].isEmpty()){
                        continue;
                    }

                    if(commands[i].startsWith("home=")){
                        int index = commands[i].indexOf("=");
                        home = commands[i].substring(index+1);
                        continue;
                    }

                    if(commands[i].startsWith("target=")){
                        int index = commands[i].indexOf("=");
                        target = commands[i].substring(index+1);
                        continue;
                    }

                    if(commands[i].startsWith("merge")){
                        merge = true;
                        continue;
                    }


                    if(commands[i].startsWith("server-name=")){
                        int index = commands[i].indexOf("=");
                        serverName = commands[i].substring(index+1);
                        continue;
                    }

                    if(commands[i].startsWith("only-cli")){
                        xml = false;
                        continue;
                    }

                    if(commands[i].startsWith("only-xml")){
                        cli = false;
                        continue;

                    }

                    if(commands[i].startsWith("subsystem=")){
                        subsystem = true;
                        int index = commands[i].indexOf("=");
                        subs = commands[i].substring(index+1);
                        continue;
                    }

                    if(commands[i].startsWith("dont-copy-resources")){
                        copy = false;
                        continue;

                    }

                    System.err.println("Error wrong command :" + "--" + commands[i]);
                    return;
                }
            }

            if(target.isEmpty()){
                System.err.println("No directory for AS7: Directory of AS7 must be specified with parameter \"target=\"");
                return;
            }

            if(home.isEmpty()){
               home = directory.getCanonicalPath();
            }

            if(serverName.isEmpty()){
                serverName = "standard";
            }

            subs = subs.replaceAll(" ", "");
            String[] subsParts = subs.split("\\,");
            // TODO:??
            if((subsParts.length > 5) || (subsParts.length==0)){
                System.err.println("chyba ");
            }

            if (subsystem) {
                for(int i = 0; i<subsParts.length; i++){
                    if(subsParts[i].equalsIgnoreCase("log")){
                        log = true;
                        continue;
                    }

                    if(subsParts[i].equalsIgnoreCase("datasource")){
                        data = true;
                        continue;
                    }

                    if(subsParts[i].equalsIgnoreCase("security")){
                        security = true;
                        continue;
                    }

                    if(subsParts[i].equalsIgnoreCase("server")){
                        server = true;
                        continue;
                    }

                    if(subsParts[i].equalsIgnoreCase("resource")){
                        resource = true;
                        continue;
                    }

                    System.err.println("Wrong name of subsystem in paramater \"subsystem=\" :" + subsParts[i]);
                    return;
                }
            }  else {
                log = true;
                data = true;
                security = true;
                server = true;
                resource = true;
            }

            String serverPath = home + File.separator + "server" +File.separator + serverName;
            Migration migration = new MigrationImpl(copy);
            LoggingAS7 loggingAS7 =  null;
            SecurityAS7 securityAS7 = null;
            ServerSub serverSub = null;
            DatasourcesSub dsSub = null;
            ResourceAdaptersSub resAdapSub = null;
            Set<DataSources> dsColl = new HashSet();
            Set<ConnectionFactories> connFacColl = new HashSet();

            if(log){
                BufferedReader br = new BufferedReader(new FileReader(
                        new File(serverPath + File.separator +"conf" +File.separator + "jboss-log4j.xml")));
                String line;
                StringBuilder sb = new StringBuilder();

                while((line = br.readLine()) != null){
                    if(line.contains("<!DOCTYPE")){
                        continue;
                    }
                    sb.append(line.replaceAll("log4j:", "").replace("xmlns:log4j=\"http://jakarta.apache.org/log4j/\"", "") + "\n");
                }

                final JAXBContext logContext = JAXBContext.newInstance(LoggingAS5.class);
                Unmarshaller unmarshaller = logContext.createUnmarshaller();

                temp = new File("temp.xml");
                FileWriter fileWriter = new FileWriter(temp);
                fileWriter.write(sb.toString());
                fileWriter.close();

                if(temp.canRead()){
                    LoggingAS5 loggingAS5 = (LoggingAS5)unmarshaller.unmarshal(temp);
                    loggingAS7 = migration.loggingMigration(loggingAS5);
                }else{
                    System.err.println("Error: don't have permission for reading files in directory \"AS5_Home"
                            +File.separator + "server"+ File.separator+"conf\"");
                    return;
                }
            }

            if(data){
                final JAXBContext dataContext = JAXBContext.newInstance(DataSources.class);
                Unmarshaller dataUnmarshaller = dataContext.createUnmarshaller();
                final JAXBContext resourceContext = JAXBContext.newInstance(DataSources.class);
                Unmarshaller resourceUnmarshaller = resourceContext.createUnmarshaller();

                File dsFiles = new File(serverPath + File.separator + "deploy" );

                if(dsFiles.canRead()){
                    SuffixFileFilter sf = new SuffixFileFilter("-ds.xml");
                    List<File> list = (List<File>) FileUtils.listFiles(dsFiles,sf,null );

                    for(int i = 0; i < list.size() ; i++){
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc = db.parse( list.get(i) );
                        Element element =doc.getDocumentElement();

                        if(element.getTagName().equalsIgnoreCase("datasources")){
                            DataSources dataSources = (DataSources)dataUnmarshaller.unmarshal(list.get(i));
                            dsColl.add(dataSources);
                        } else {
                            if(element.getTagName().equalsIgnoreCase("connection-factories")){
                                if(resource){
                                    ConnectionFactories connFac =
                                            (ConnectionFactories)resourceUnmarshaller.unmarshal(list.get(i));
                                    connFacColl.add(connFac);
                                }
                            }  else {
                                System.err.println("Error: Wrong format of XML files of datasources and connection-factories"
                                        + "( \"-ds.xml\" files ");
                                return;
                            }

                        }

                    }
                } else {
                    System.err.println("Error: don't have permission for reading files in directory \"AS5_Home"
                            +File.separator + "server"+ File.separator+"deploy\"");
                    return;
                }

                dsSub = migration.datasourceSubMigration(dsColl);

                if(resource){
                    resAdapSub = migration.resourceAdaptersMigration(connFacColl);
                }

            }

            if(security){
                final JAXBContext securityContext = JAXBContext.newInstance(SecurityAS5.class);
                Unmarshaller unmarshaller = securityContext.createUnmarshaller();

                File securityFile = new File(serverPath + File.separator + "conf" + File.separator + "login-config.xml");

                if(securityFile.canRead()){
                    SecurityAS5 securityAS5 = (SecurityAS5)unmarshaller.unmarshal(securityFile);
                    securityAS7 = migration.securityMigration(securityAS5);
                } else {
                    System.err.println("Error: don't have permission for reading files in directory \"AS5_Home"
                            +File.separator + "server"+ File.separator+"deploy\"");
                    return;
                }

            }

            if(server){
               final JAXBContext serverContext = JAXBContext.newInstance(ServerAS5.class );
               Unmarshaller unmarshaller = serverContext.createUnmarshaller();

               File serverFile = new File(serverPath + File.separator + "deploy" + File.separator
                       + "jbossweb.sar" + File.separator + "server.xml");

               if(serverFile.canRead()){
                   ServerAS5 serverAS5 = (ServerAS5)unmarshaller.unmarshal(serverFile);
                   serverSub = migration.serverMigration(serverAS5);
               }  else{
                   System.err.println("Error: don't have permission for reading files in directory \"AS5_Home"
                           +File.separator + "server"+ File.separator+"deploy\"");
                   return;
               }

            }

            if(xml){
                //final Comment comment = doc.createComment("This is a comment");
                //doc.appendChild(comment);

                // to systemout at this moment for testing
                //final StreamResult streamResult = new StreamResult(System.out);
                //final StreamResult streamResult = new StreamResult(new File("testxml.xml"));

                StringWriter writer= new StringWriter();
                final StreamResult streamResult = new StreamResult(writer);
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
                writer.write("<migration>\n");

                final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder builder = factory.newDocumentBuilder();

                final TransformerFactory tf = TransformerFactory.newInstance();
                final Transformer serializer = tf.newTransformer();
                serializer.setOutputProperty(OutputKeys.INDENT, "yes");
                serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
                serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");



                if(data){
                    final JAXBContext dataSubContext = JAXBContext.newInstance(DatasourcesSub.class);
                    final Binder<Node> dataBinder = dataSubContext.createBinder();
                    final Document docData = builder.getDOMImplementation().createDocument(null, null, null);
                    dataBinder.marshal(dsSub,docData);
                    final DOMSource domData = new DOMSource(docData);
                    serializer.transform(domData, streamResult);

                }

                if(security){
                    final JAXBContext securitySubContext = JAXBContext.newInstance(SecurityAS7.class);
                    final Binder<Node> securityBinder = securitySubContext.createBinder();
                    final Document docSec = builder.getDOMImplementation().createDocument(null, null, null);
                    securityBinder.marshal(securityAS7,docSec);

                    final DOMSource domSecurity = new DOMSource(docSec);
                    serializer.transform(domSecurity, streamResult);
                }

                if(resource){
                    if(resAdapSub != null){
                        final JAXBContext resourceSubContext = JAXBContext.newInstance(ResourceAdaptersSub.class);
                        final Binder<Node> resourceBinder = resourceSubContext.createBinder();
                        final Document docResource = builder.getDOMImplementation().createDocument(null, null, null);
                        //Object[] test = connFacColl.toArray();
                        resourceBinder.marshal(resAdapSub, docResource);
                        final DOMSource domResource = new DOMSource(docResource);
                        serializer.transform(domResource, streamResult);
                    }

                }

                if(server){
                    final JAXBContext serverSubContext = JAXBContext.newInstance(ServerSub.class);
                    final JAXBContext socketContext = JAXBContext.newInstance(SocketBindingGroup.class);

                    final Binder<Node> socketBinder = socketContext.createBinder();
                    final Binder<Node> serverBinder = serverSubContext.createBinder();

                    final Document docServer = builder.getDOMImplementation().createDocument(null, null, null);
                    final Document docSocket = builder.getDOMImplementation().createDocument(null, null, null);

                    serverBinder.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                    serverBinder.marshal(serverSub,docServer);
                    socketBinder.marshal(migration.getSocketBindingGroup(),docSocket);


                    final DOMSource domServer = new DOMSource(docServer);
                    final DOMSource domSocket = new DOMSource(docSocket);

                    serializer.transform(domServer, streamResult);
                    serializer.transform(domSocket, streamResult);


                }

                if(log){
                    final JAXBContext loggingSubContext = JAXBContext.newInstance(LoggingAS7.class);
                    final Binder<Node> loggingBinder = loggingSubContext.createBinder();
                    final Document docLog = builder.getDOMImplementation().createDocument(null, null, null);
                    loggingBinder.marshal(loggingAS7,docLog);

                    final DOMSource domLog = new DOMSource(docLog);
                    serializer.transform(domLog, streamResult);

                }
                writer.write("</migration>");
                FileWriter fstream = new FileWriter(xmlOut);
                out = new BufferedWriter(fstream);
                out.write(writer.toString());
                out.close();

            }

            if(cli){
                CliScript cliScript = new CliScriptImpl();

                if(data){
                    if(dsSub.getDatasource() != null){
                        for(DatasourceAS7 datasourceAS7 : dsSub.getDatasource()){
                            System.out.println(cliScript.createDatasourceScript(datasourceAS7));
                        }
                    }

                    if(dsSub.getXaDatasource() != null){
                        for(XaDatasourceAS7 xaDSAS7 : dsSub.getXaDatasource()){
                            System.out.println(cliScript.createXaDatasourceScript(xaDSAS7));
                        }
                    }

                    if(dsSub.getDrivers() != null){
                        for(Driver driver : dsSub.getDrivers()){
                            System.out.println(cliScript.createDriverScript(driver));
                        }
                    }
                }

                if(resource){
                    if(resAdapSub != null){
                        for(ResourceAdapter resourceAdapter : resAdapSub.getResourceAdapters()){
                            System.out.println(cliScript.createResAdapterScript(resourceAdapter));

                        }
                    }

                }

                if(security){
                     if(securityAS7 != null){
                         for(SecurityDomain securityDomain : securityAS7.getSecurityDomains()){
                             System.out.println(cliScript.createSecurityDomainScript(securityDomain));
                         }
                     }
                }

                if(server){
                    if(serverSub != null){
                        for(ConnectorAS7 connectorAS7 : serverSub.getConnectors()){
                            System.out.println(cliScript.createConnectorScript(connectorAS7));
                        }
                    }

                }

                if(log){
                    if(loggingAS7 != null){
                        System.out.println(cliScript.createHandlersScript(loggingAS7));

                        for(Logger logger : loggingAS7.getLoggers()){
                            System.out.println(cliScript.createLoggerScript(logger));
                        }
                    }

                }
            }

            if(merge){
                DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                domFactory.setIgnoringComments(true);
                DocumentBuilder builder = domFactory.newDocumentBuilder();

                File homedir = new File(System.getProperty("user.home"));
                File fileToRead = new File(homedir, "Programing/standalone.xml");

                // at this moment input of migrated XMLs from helping file xmlOut.xml (xmlOut) for testing purposes
                Document xmlDoc = builder.parse(new File(xmlOut));
                Document doc = builder.parse(fileToRead);

                NodeList nodeList = xmlDoc.getDocumentElement().getChildNodes();

                for(int i = 0; i < nodeList.getLength(); i++){
                    if (nodeList.item(i) instanceof Element == false) continue;
                    switch(nodeList.item(i).getNodeName()){
                        case("datasources"): {
                            doc = insertIntoXml(doc, nodeList.item(i),"datasource");
                            NodeList drivers = nodeList.item(i).getChildNodes();
                            for(int j = 0; j < drivers.getLength(); j++){
                                if(drivers.item(j) instanceof Element == false) continue;
                                if(drivers.item(j).getNodeName() == "drivers"){
                                    insertDriver(doc, drivers.item(j));
                                    break;
                                }
                            }
                        } break;
                        case("logging"):  doc = insertIntoXml(doc, nodeList.item(i),"logging");  break;
                        case("security-domains"): doc = insertIntoXml(doc, nodeList.item(i),"security");  break;
                        case("socket-binding-group"):  doc = insertIntoXml(doc, nodeList.item(i),"socket-binding-group");  break;
                        case("server"):    doc = insertIntoXml(doc, nodeList.item(i),"web");  break;
                        case("resource-adapters"): doc = insertIntoXml(doc, nodeList.item(i),"resource-adapter");  break;
                        // TODO: Maybe file was edited or app did something wrong?
                        default: throw new IOException("Error: XML file contains unknown element for merge!");
                    }

                }
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                StreamResult result = new StreamResult(new StringWriter());
                DOMSource source = new DOMSource(doc);
                transformer.transform(source, result);

                // Output to sout at this moment for testing
                String xmlOutput = result.getWriter().toString();
                System.out.println(xmlOutput);
            }

            if(copy){
              Collection<CopyMemory> copyMemories = migration.getCopyMemories();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (CliScriptException e) {
            e.printStackTrace();
        } finally {
            if(temp != null){
                temp.delete();
            }
        }


    }



}
