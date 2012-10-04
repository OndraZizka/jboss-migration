package cz.fi.muni.jboss.Migration.Logging;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 9/21/12
 * Time: 9:06 PM
 */
  //len provizorne kopa nejasnosti
@XmlRootElement(name = "subsystem")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "subsystem")
public class LoggingAS7 {
    @XmlAttribute(name = "xmlns")
    private String xmlns = "urn:jboss:domain:logging:1.1";
    @XmlElements(@XmlElement(name = "async-handler", type = AsyncHandler.class))
    private Collection<AsyncHandler> asyncHandlers;
    @XmlElements(@XmlElement(name = "console-handler",type = ConsoleHandler.class))
    private Collection<ConsoleHandler> consoleHandlers;
    @XmlElements(@XmlElement(name = "file-handler", type = FileHandler.class))
    private Collection<FileHandler> fileHandlers;
    @XmlElements(@XmlElement(name = "periodic-rotating-file-handler", type = PeriodicRotatingFileHandler.class))
    private Collection<PeriodicRotatingFileHandler> periodicRotatingFileHandlers;
    @XmlElements(@XmlElement(name = "size-rotating-file-handler", type = SizeRotatingFileHandler.class))
    private Collection<SizeRotatingFileHandler> sizeRotatingFileHandlers;
    @XmlElements(@XmlElement(name = "custom-handler", type = CustomHandler.class))
    private Collection<CustomHandler> customHandlers;
    @XmlElements(@XmlElement(name = "logger", type = Logger.class))
    private Collection<Logger> loggers;

    @XmlPath("root-logger/level/@name")
    private String rootLoggerLevel;
    @XmlPath("root-logger/handlers/handler/@name")
    private Collection<String> rootLoggerHandlers;
    @XmlPath("root-logger/filter/@value")
    private String rootLoggerFilterValue;

    public String getXmlns() {
        return xmlns;
    }

    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    public Collection<AsyncHandler> getAsyncHandlers() {
        return asyncHandlers;
    }

    public void setAsyncHandlers(Collection<AsyncHandler> asyncHandlers) {
        this.asyncHandlers = asyncHandlers;
    }

    public Collection<ConsoleHandler> getConsoleHandlers() {
        return consoleHandlers;
    }

    public void setConsoleHandlers(Collection<ConsoleHandler> consoleHandlers) {
        this.consoleHandlers = consoleHandlers;
    }

    public Collection<FileHandler> getFileHandlers() {
        return fileHandlers;
    }

    public void setFileHandlers(Collection<FileHandler> fileHandlers) {
        this.fileHandlers = fileHandlers;
    }

    public Collection<PeriodicRotatingFileHandler> getPeriodicRotatingFileHandlers() {
        return periodicRotatingFileHandlers;
    }

    public void setPeriodicRotatingFileHandlers(Collection<PeriodicRotatingFileHandler> periodicRotatingFileHandlers) {
        this.periodicRotatingFileHandlers = periodicRotatingFileHandlers;
    }

    public Collection<SizeRotatingFileHandler> getSizeRotatingFileHandlers() {
        return sizeRotatingFileHandlers;
    }

    public void setSizeRotatingFileHandlers(Collection<SizeRotatingFileHandler> sizeRotatingFileHandlers) {
        this.sizeRotatingFileHandlers = sizeRotatingFileHandlers;
    }

    public Collection<CustomHandler> getCustomHandlers() {
        return customHandlers;
    }

    public void setCustomHandlers(Collection<CustomHandler> customHandlers) {
        this.customHandlers = customHandlers;
    }

    public Collection<Logger> getLoggers() {
        return loggers;
    }

    public void setLoggers(Collection<Logger> loggers) {
        this.loggers = loggers;
    }

    public String getRootLoggerLevel() {
        return rootLoggerLevel;
    }

    public void setRootLoggerLevel(String rootLoggerLevel) {
        this.rootLoggerLevel = rootLoggerLevel;
    }

    public Collection<String> getRootLoggerHandlers() {
        return rootLoggerHandlers;
    }

    public void setRootLoggerHandlers(Collection<String> rootLoggerHandlers) {
        this.rootLoggerHandlers = rootLoggerHandlers;
    }

    public String getRootLoggerFilterValue() {
        return rootLoggerFilterValue;
    }

    public void setRootLoggerFilterValue(String rootLoggerFilterValue) {
        this.rootLoggerFilterValue = rootLoggerFilterValue;
    }










}
