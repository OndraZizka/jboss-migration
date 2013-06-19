/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.server.jaxb;

import javax.xml.bind.annotation.*;

/**
 * JAXB bean for socket-binding (AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "socket-binding")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "socket-binding")

public class SocketBindingBean {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SocketBindingBean)) return false;

        SocketBindingBean that = (SocketBindingBean) o;

        if (socketName != null ? !socketName.equals(that.socketName) : that.socketName != null) return false;
        if (socketPort != null ? !socketPort.equals(that.socketPort) : that.socketPort != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = socketName != null ? socketName.hashCode() : 0;
        result = 31 * result + (socketPort != null ? socketPort.hashCode() : 0);
        return result;
    }
}
