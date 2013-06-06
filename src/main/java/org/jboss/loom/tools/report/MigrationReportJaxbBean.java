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
import org.jboss.loom.tools.report.adapters.ToActionBeanAdapter;
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


    public MigrationReportJaxbBean() { }
    public MigrationReportJaxbBean( Configuration config, ComparisonResult comparisonResult, Collection<MigrationData> configData, List<IMigrationAction> actions ) {
        this.config = config;
        this.comparisonResult = comparisonResult;
        this.configData = configData;
        this.actions = actions;
    }
    
}// class
