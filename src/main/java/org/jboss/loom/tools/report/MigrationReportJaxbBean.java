package org.jboss.loom.tools.report;


import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.ctx.MigratorData;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.recog.ServerInfo;
import org.jboss.loom.tools.report.adapters.MigratorDataSubtypesAdapter;
import org.jboss.loom.tools.report.adapters.ToActionBeanAdapter;
import org.jboss.loom.tools.report.adapters.ToStringAdapter;
import org.jboss.loom.utils.compar.ComparisonResult;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@XmlRootElement(name="migrationReport")
@XmlAccessorType( XmlAccessType.NONE )
public class MigrationReportJaxbBean {

    @XmlElement
    public Configuration config;
    
    @XmlElement(name = "sourceServer", required = true)
    public ServerInfo sourceServer;
    
    @XmlElement
    public ComparisonResult comparisonResult;
    
    @XmlElementWrapper(name = "configsData")
    @XmlElement(name = "configData")
    @XmlJavaTypeAdapter( MigratorDataSubtypesAdapter.class )
    public Collection<MigratorData> configData;

    @XmlElementWrapper(name = "actions")
    @XmlElement(name = "action")
    @XmlJavaTypeAdapter( ToActionBeanAdapter.class )
    public List<IMigrationAction> actions;
    
    @XmlElement(name="finalException")
    @XmlJavaTypeAdapter( ToStringAdapter.class )
    public MigrationException finalException;

}// class
