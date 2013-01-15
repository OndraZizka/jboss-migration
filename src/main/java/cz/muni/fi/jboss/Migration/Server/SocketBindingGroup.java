package cz.muni.fi.jboss.Migration.Server;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: Roman Jakubco
 * Date: 9/21/12
 * Time: 3:49 PM
 */

/*
 Provizorne riesenie ako vyrabat porty v AS7. Kedze este neviem ci budem vytvarat cely dokument standalone.
 Alebo len vytvorim dokument, z ktoreho si to skopriju do standalone. Takze potom v buducnosti upravit
 */
@XmlRootElement(name = "socket-binding-group")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "socket-binding-group")

public class SocketBindingGroup {

  @XmlElements(@XmlElement(name = "socket-binding", type =SocketBinding.class ))
   private Set<SocketBinding> socketBindings;

    public Set<SocketBinding> getSocketBindings() {
        return socketBindings;
    }

    public void setSocketBindings(Collection<SocketBinding> socketBindings) {
        Set<SocketBinding> temp = new HashSet();
        temp.addAll(socketBindings);
        this.socketBindings = temp;
    }




}
