package cz.muni.fi.jboss.migration.migrators.trash;

import cz.muni.fi.jboss.migration.migrators.server.jaxb.SocketBindingBean;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for marshalling and representing socket-binding-group (AS7)
 *
 * @author Roman Jakubco
 * Date: 9/21/12
 * Time: 3:49 PM
 */


@XmlRootElement(name = "socket-binding-group")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "socket-binding-group")

public class SocketBindingGroup {

  @XmlElements(@XmlElement(name = "socket-binding", type = SocketBindingBean.class ))
   private Set<SocketBindingBean> socketBindings;

    public Set<SocketBindingBean> getSocketBindings() {
        return socketBindings;
    }

    public void setSocketBindings(Collection<SocketBindingBean> socketBindings) {
        Set<SocketBindingBean> temp = new HashSet();
        temp.addAll(socketBindings);
        this.socketBindings = temp;
    }

    public boolean isEmpty(){
        if(socketBindings == null){
            return true;
        }
        return socketBindings.isEmpty();
    }




}
