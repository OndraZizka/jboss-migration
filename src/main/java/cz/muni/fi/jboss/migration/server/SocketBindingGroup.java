package cz.muni.fi.jboss.migration.server;

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

  @XmlElements(@XmlElement(name = "socket-binding", type = SocketBinding.class ))
   private Set<SocketBinding> socketBindings;

    public Set<SocketBinding> getSocketBindings() {
        return socketBindings;
    }

    public void setSocketBindings(Collection<SocketBinding> socketBindings) {
        Set<SocketBinding> temp = new HashSet();
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
