package cz.fi.muni.jboss.migration.server;

import javax.xml.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 10/2/12
 * Time: 9:14 PM
 */
@XmlRootElement(name = "socket-binding")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "socket-binding")
public class SocketBinding{
    @XmlAttribute(name = "name")
    private String socketName;
    @XmlAttribute(name = "interface")
    private String socketInterface;
    @XmlAttribute(name = "port")
    private String socketPort;

    public String getSocketName() {
        return socketName;
    }

    public void setSocketName(String socketName) {
        this.socketName = socketName;
    }

    public String getSocketInterface() {
        return socketInterface;
    }

    public void setSocketInterface(String socketInterface) {
        this.socketInterface = socketInterface;
    }

    public String getSocketPort() {
        return socketPort;
    }

    public void setSocketPort(String socketPort) {
        this.socketPort = socketPort;
    }
}
