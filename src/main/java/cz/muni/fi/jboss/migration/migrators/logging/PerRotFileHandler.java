package cz.muni.fi.jboss.migration.migrators.logging;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;

/**
 * Class for marshalling and representing periodic-rotating-file-handler (AS7)
 *
 * @author Roman Jakubco
 * Date: 10/2/12
 * Time: 8:21 PM
 */

@XmlRootElement(name = "periodic-rotating-file-handler")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "periodic-rotating-file-handler")

public  class PerRotFileHandler {

    @XmlAttribute(name = "name")
    private String name;

    // TODO: encoding...

    @XmlPath("level/@name")
    private String level;

    @XmlPath("filter/@value")
    private String filter;

    @XmlPath("formatter/pattern-formatter/@pattern")
    private String formatter;

    @XmlAttribute(name = "autoflush")
    private String autoflush;

    @XmlPath("append/@value")
    private String append;

    @XmlPath("file/@relative-to")
    private String fileRelativeTo;

    @XmlPath("file/@path")
    private String path ;

    @XmlPath("suffix/@value")
    private String suffix;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFormatter() {
        return formatter;
    }

    public void setFormatter(String formatter) {
        this.formatter = formatter;
    }

    public String getAutoflush() {
        return autoflush;
    }

    public void setAutoflush(String autoflush) {
        this.autoflush = autoflush;
    }

    public String getAppend() {
        return append;
    }

    public void setAppend(String append) {
        this.append = append;
    }

    public String getFileRelativeTo() {
        return fileRelativeTo;
    }

    public void setFileRelativeTo(String fileRelativeTo) {
        this.fileRelativeTo = fileRelativeTo;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
