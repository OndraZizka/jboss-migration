package cz.muni.fi.jboss.migration.migrators.server;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:42 AM
 */
public class ServerMigrator implements IMigrator {

    private GlobalConfiguration globalConfig;

    private List<Pair<String,String>> config;

    private SocketBindingGroup socketBindingGroup;

    private Set<SocketBinding> socketTemp = new HashSet();

    private Integer randomSocket = 1;

    private Integer randomConnector =1 ;

    public ServerMigrator(GlobalConfiguration globalConfig, List<Pair<String,String>> config){
        this.globalConfig = globalConfig;
        this.config =  config;
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException, FileNotFoundException{
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(ServerAS5.class ).createUnmarshaller();

            // Or maybe use FileUtils and list all files with that name?
            File file = new File(globalConfig.getDirAS5() + File.separator + "deploy" + File.separator
                    + "jbossweb.sar" + File.separator + "server.xml");

            if(file.canRead()){
                ServerAS5 serverAS5 = (ServerAS5)unmarshaller.unmarshal(file);

                MigrationData mData = new MigrationData();
                for(Service s : serverAS5.getServices()){
                    mData.getLoadedData().add(s);
                    mData.getLoadedData().addAll(s.getConnectorAS5s());
                }

                ctx.getMigrationData().put(ServerMigrator.class, mData);

            } else{
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

    public SocketBindingGroup getSocketBindingGroup() {
        return socketBindingGroup;
    }

    public void setSocketBindingGroup(SocketBindingGroup socketBindingGroup) {
        this.socketBindingGroup = socketBindingGroup;
    }

    public Set<SocketBinding> getSocketTemp() {
        return socketTemp;
    }

    public void setSocketTemp(Set<SocketBinding> socketTemp) {
        this.socketTemp = socketTemp;
    }

    public GlobalConfiguration getGlobalConfig() {
        return globalConfig;
    }

    public void setGlobalConfig(GlobalConfiguration globalConfig) {
        this.globalConfig = globalConfig;
    }

    public List<Pair<String, String>> getConfig() {
        return config;
    }

    public void setConfig(List<Pair<String, String>> config) {
        this.config = config;
    }

    private void createDefaultSockets(){
        /*
        <socket-binding name="ajp" port="8009"/>
     <socket-binding name="http" port="8080"/>
     <socket-binding name="https" port="8443"/>
     <socket-binding name="remoting" port="4447"/>
     <socket-binding name="txn-recovery-environment" port="4712"/>
     <socket-binding name="txn-status-manager" port="4713"/>
         */

        SocketBinding sb1 = new SocketBinding();
        sb1.setSocketName("ajp");
        sb1.setSocketPort("8009");
        socketTemp.add(sb1);

        SocketBinding sb2 = new SocketBinding();
        sb2.setSocketName("http");
        sb2.setSocketPort("8080");
        socketTemp.add(sb2);

        SocketBinding sb3 = new SocketBinding();
        sb3.setSocketName("https");
        sb3.setSocketPort("8443");
        socketTemp.add(sb3);

        SocketBinding sb4 = new SocketBinding();
        sb4.setSocketName("remoting");
        sb4.setSocketPort("4712");
        socketTemp.add(sb4);
    }

    private String createSocketBinding(String port, String name) {
        if(socketTemp.isEmpty()){
            createDefaultSockets();
        }
        SocketBinding socketBinding = new SocketBinding();
        Set<SocketBinding> socketBindings = new HashSet();

        for (SocketBinding sb : socketTemp) {
            if (sb.getSocketPort().equals(port)) {
                return sb.getSocketName();
            }
        }

        if (socketBindingGroup.getSocketBindings() == null) {
            socketBinding.setSocketName(name);
            socketBinding.setSocketPort(port);
            socketBindings.add(socketBinding);
            socketBindingGroup.setSocketBindings(socketBindings);
            return name;
        }

        for (SocketBinding sb : socketBindingGroup.getSocketBindings()) {
            if (sb.getSocketPort().equals(port)) {
                return sb.getSocketName();
            }
        }

        socketBinding.setSocketPort(port);

        for (SocketBinding sb : socketBindingGroup.getSocketBindings()) {
            if (sb.getSocketName().equals(name)) {
                name = name.concat(randomSocket.toString());
                randomSocket++;
            }
        }

        socketBinding.setSocketName(name);
        socketBindingGroup.getSocketBindings().add(socketBinding);

        return name;
    }
}
