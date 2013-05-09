/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.server;

import org.jboss.loom.migrators.server.jaxb.SocketBindingBean;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Class containing all helping information for successful migration of Server (Web)
 *
 * User: Roman Jakubco
 * Date: 4/29/13
 */
public class ServerMigratorResource {
    // Use for creating and determining if socket should be created
    private Set<SocketBindingBean> socketTemp = new HashSet();
    private Set<SocketBindingBean> socketBindings = new HashSet();

    // all keystore files, which should be copied into AS7
    private Set<File> keystores = new HashSet();

    // incrementing number for created sockets
    private Integer randomSocket = 1;

    // incrementing number for created connectors
    private Integer randomConnector = 1;

    public Integer getRandomConnector() {
        return randomConnector++;
    }

    public Integer getRandomSocket() {
        return randomSocket++;
    }

    public Set<SocketBindingBean> getSocketBindings() {
        return socketBindings;
    }

    public void setSocketBindings(Set<SocketBindingBean> socketBindings) {
        this.socketBindings = socketBindings;
    }

    public Set<SocketBindingBean> getSocketTemp() {
        return socketTemp;
    }

    public void setSocketTemp(Set<SocketBindingBean> socketTemp) {
        this.socketTemp = socketTemp;
    }

    public Set<File> getKeystores() {
        return keystores;
    }
}
