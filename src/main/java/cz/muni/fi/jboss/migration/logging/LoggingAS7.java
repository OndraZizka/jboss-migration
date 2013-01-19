package cz.muni.fi.jboss.migration.logging;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for marshalling and representing logging subsystem (AS7)
 *
 * @author Roman Jakubco
 * Date: 9/21/12
 * Time: 9:06 PM
 */

@XmlRootElement(name = "logging")
@XmlAccessorType(XmlAccessType.NONE)
/*
 * Same problem as in ServerSub
 */
@XmlType(name = "", propOrder = {"loggers", "perRotFileHandlers", "consoleHandlers", "asyncHandlers", "fileHandlers",
        "sizeRotFileHandlers", "customHandlers"})

public class LoggingAS7 {

    @XmlElements(@XmlElement(name = "async-handler", type = AsyncHandler.class))
    private Set<AsyncHandler> asyncHandlers;

    @XmlElements(@XmlElement(name = "console-handler", type = ConsoleHandler.class))
    private Set<ConsoleHandler> consoleHandlers;

    @XmlElements(@XmlElement(name = "file-handler", type = FileHandler.class))
    private Set<FileHandler> fileHandlers;

    @XmlElements(@XmlElement(name = "periodic-rotating-file-handler", type = PerRotFileHandler.class))
    private Set<PerRotFileHandler> perRotFileHandlers;

    @XmlElements(@XmlElement(name = "size-rotating-file-handler", type = SizeRotatingFileHandler.class))
    private Set<SizeRotatingFileHandler> sizeRotFileHandlers;

    @XmlElements(@XmlElement(name = "custom-handler", type = CustomHandler.class))
    private Set<CustomHandler> customHandlers;

    @XmlElements(@XmlElement(name = "logger", type = Logger.class))
    private Set<Logger> loggers;

    @XmlPath("root-logger/level/@name")
    private String rootLoggerLevel;

    @XmlPath("root-logger/handlers/handler/@name")
    private Set<String> rootLoggerHandlers;

    @XmlPath("root-logger/filter/@value")
    private String rootLogFilValue;

    public Set<AsyncHandler> getAsyncHandlers() {
        return asyncHandlers;
    }

    public void setAsyncHandlers(Collection<AsyncHandler> asyncHandlers) {
        Set<AsyncHandler> temp = new HashSet();
        temp.addAll(asyncHandlers);
        this.asyncHandlers = temp;
    }

    public Set<ConsoleHandler> getConsoleHandlers() {
        return consoleHandlers;
    }

    public void setConsoleHandlers(Collection<ConsoleHandler> consoleHandlers) {
        Set<ConsoleHandler> temp = new HashSet();
        temp.addAll(consoleHandlers);
        this.consoleHandlers = temp;
    }

    public Set<FileHandler> getFileHandlers() {
        return fileHandlers;
    }

    public void setFileHandlers(Collection<FileHandler> fileHandlers) {
        Set<FileHandler> temp = new HashSet();
        temp.addAll(fileHandlers);
        this.fileHandlers = temp;
    }

    public Set<PerRotFileHandler> getPerRotFileHandlers() {
        return perRotFileHandlers;
    }

    public void setPerRotFileHandlers(Collection<PerRotFileHandler> perRotFileHandlers) {
        Set<PerRotFileHandler> temp = new HashSet();
        temp.addAll(perRotFileHandlers);
        this.perRotFileHandlers = temp;
    }

    public Set<SizeRotatingFileHandler> getSizeRotFileHandlers() {
        return sizeRotFileHandlers;
    }

    public void setSizeRotFileHandlers(Collection<SizeRotatingFileHandler> sizeRotFileHandlers) {
        Set<SizeRotatingFileHandler> temp = new HashSet();
        temp.addAll(sizeRotFileHandlers);
        this.sizeRotFileHandlers = temp;
    }

    public Set<CustomHandler> getCustomHandlers() {
        return customHandlers;
    }

    public void setCustomHandlers(Collection<CustomHandler> customHandlers) {
        Set<CustomHandler> temp = new HashSet();
        temp.addAll(customHandlers);
        this.customHandlers = temp;
    }

    public Set<Logger> getLoggers() {
        return loggers;
    }

    public void setLoggers(Collection<Logger> loggers) {
        Set<Logger> temp = new HashSet();
        temp.addAll(loggers);
        this.loggers = temp;
    }

    public String getRootLoggerLevel() {
        return rootLoggerLevel;
    }

    public void setRootLoggerLevel(String rootLoggerLevel) {
        this.rootLoggerLevel = rootLoggerLevel;
    }

    public Set<String> getRootLoggerHandlers() {
        return rootLoggerHandlers;
    }

    public void setRootLoggerHandlers(Collection<String> rootLoggerHandlers) {
        Set<String> temp = new HashSet();
        temp.addAll(rootLoggerHandlers);
        this.rootLoggerHandlers = temp;
    }

    public String getRootLogFilValue() {
        return rootLogFilValue;
    }

    public void setRootLogFilValue(String rootLogFilValue) {
        this.rootLogFilValue = rootLogFilValue;
    }










}
