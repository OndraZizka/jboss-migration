package cz.fi.muni.jboss.migration.server;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 9/21/12
 * Time: 3:49 PM
 */

/*
 Provizorne riesenie ako vyrabat porty v AS7. Kedze este neviem ci budem vytvarat cely dokuemnt standalone.
 Alebo len vytvorim dokument, z ktoreho si to skopriju do standalone. Takze potom v buducnosti upravit
 */
@XmlRootElement(name = "socket-binding-group")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "socket-binding-group")
public class SocketBindingGroup {
  @XmlElements(@XmlElement(name = "socket-binding", type =SocketBinding.class ))
   private Collection<SocketBinding> socketBindings;

    public Collection<SocketBinding> getSocketBindings() {
        return socketBindings;
    }

    public void setSocketBindings(Collection<SocketBinding> socketBindings) {
        this.socketBindings = socketBindings;
    }




}
