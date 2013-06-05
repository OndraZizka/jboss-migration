package org.jboss.loom.tools.report;


import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jboss.loom.actions.IMigrationAction;
import org.jboss.loom.conf.Configuration;
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
    
    //@XmlElement(name = "action")
    private List<IMigrationAction> actions;


    public MigrationReportJaxbBean() { }
    public MigrationReportJaxbBean( Configuration config, ComparisonResult comparisonResult, List<IMigrationAction> actions ) {
        this.config = config;
        this.comparisonResult = comparisonResult;
        this.actions = actions;
    }
    
}// class
