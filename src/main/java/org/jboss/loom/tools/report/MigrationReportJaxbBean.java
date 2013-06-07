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
import org.jboss.loom.ctx.MigrationData;
import org.jboss.loom.ex.MigrationException;
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
    private Configuration config;
    
    @XmlElement
    private ComparisonResult comparisonResult;
    
    @XmlElementWrapper(name = "configData")
    @XmlElement(name = "configData")
    private Collection<MigrationData> configData;

    @XmlElementWrapper(name = "actions")
    @XmlElement(name = "action")
    @XmlJavaTypeAdapter( ToActionBeanAdapter.class )
    private List<IMigrationAction> actions;
    
    @XmlElement(name="finalException")
    @XmlJavaTypeAdapter( ToStringAdapter.class )
    private MigrationException finalException;


    public MigrationReportJaxbBean() { }
    public MigrationReportJaxbBean( Configuration conf, ComparisonResult compRes, 
            Collection<MigrationData> configData, List<IMigrationAction> actions,
            MigrationException ex
    ) {
        this.config = conf;
        this.comparisonResult = compRes;
        this.configData = configData;
        this.actions = actions;
        this.finalException = ex;
    }
    
}// class
