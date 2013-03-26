package cz.muni.fi.jboss.migration.migrators.logging.jaxb;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;

/**
 * Class for marshalling and representing size-rotating-file-handler (AS7)
 *
 * @author Roman Jakubco
 */

@XmlRootElement(name = "size-rotating-handler")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "size-rotating-handler")

public class SizeRotFileHandlerBean {

    @XmlAttribute(name = "name")
    private String name;

    @XmlPath("encoding/@value")
    private String encoding;

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

    @XmlPath("relativeTo/@relative-to")
    private String relativeTo;

    @XmlPath("relativeTo/@path")
    private String path;

    @XmlPath("rotate-size/@value")
    private String rotateSize;

    @XmlPath("max-backup-index/@value")
    private String maxBackupIndex;

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

    public String getRelativeTo() {
        return relativeTo;
    }

    public void setRelativeTo(String relativeTo) {
        this.relativeTo = relativeTo;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRotateSize() {
        return rotateSize;
    }

    public void setRotateSize(String rotateSize) {
        this.rotateSize = rotateSize;
    }

    public String getMaxBackupIndex() {
        return maxBackupIndex;
    }

    public void setMaxBackupIndex(String maxBackupIndex) {
        this.maxBackupIndex = maxBackupIndex;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
