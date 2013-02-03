package cz.muni.fi.jboss.migration.migrators.trash;

import cz.muni.fi.jboss.migration.migrators.logging.jaxb.*;

import javax.xml.bind.annotation.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for marshalling and representing logging subsystem (AS7)
 *
 * @author Roman Jakubco
 *         Date: 9/21/12
 *         Time: 9:06 PM
 */

@XmlRootElement(name = "logging")
@XmlAccessorType(XmlAccessType.NONE)
/*
 * Same problem as in ServerSub
 */
@XmlType(name = "", propOrder = {"loggers", "perRotFileHandlers", "consoleHandlers", "asyncHandlers", "fileHandlers",
        "sizeRotFileHandlers", "customHandlers", "rootLogger"})

public class LoggingAS7 {

    @XmlElements(@XmlElement(name = "async-handler", type = AsyncHandlerBean.class))
    private Set<AsyncHandlerBean> asyncHandlers;

    @XmlElements(@XmlElement(name = "console-handler", type = ConsoleHandlerBean.class))
    private Set<ConsoleHandlerBean> consoleHandlers;

    @XmlElements(@XmlElement(name = "file-handler", type = FileHandlerBean.class))
    private Set<FileHandlerBean> fileHandlers;

    @XmlElements(@XmlElement(name = "periodic-rotating-file-handler", type = PerRotFileHandlerBean.class))
    private Set<PerRotFileHandlerBean> perRotFileHandlers;

    @XmlElements(@XmlElement(name = "size-rotating-file-handler", type = SizeRotFileHandlerBean.class))
    private Set<SizeRotFileHandlerBean> sizeRotFileHandlers;

    @XmlElements(@XmlElement(name = "custom-handler", type = CustomHandlerBean.class))
    private Set<CustomHandlerBean> customHandlers;

    @XmlElements(@XmlElement(name = "logger", type = LoggerBean.class))
    private Set<LoggerBean> loggers;

    @XmlElement(name = "root-logger")
    private RootLoggerAS7Bean rootLogger;

    public Set<AsyncHandlerBean> getAsyncHandlers() {
        return asyncHandlers;
    }

    public void setAsyncHandlers(Collection<AsyncHandlerBean> asyncHandlers) {
        Set<AsyncHandlerBean> temp = new HashSet();
        temp.addAll(asyncHandlers);
        this.asyncHandlers = temp;
    }

    public Set<ConsoleHandlerBean> getConsoleHandlers() {
        return consoleHandlers;
    }

    public void setConsoleHandlers(Collection<ConsoleHandlerBean> consoleHandlers) {
        Set<ConsoleHandlerBean> temp = new HashSet();
        temp.addAll(consoleHandlers);
        this.consoleHandlers = temp;
    }

    public Set<FileHandlerBean> getFileHandlers() {
        return fileHandlers;
    }

    public void setFileHandlers(Collection<FileHandlerBean> fileHandlers) {
        Set<FileHandlerBean> temp = new HashSet();
        temp.addAll(fileHandlers);
        this.fileHandlers = temp;
    }

    public Set<PerRotFileHandlerBean> getPerRotFileHandlers() {
        return perRotFileHandlers;
    }

    public void setPerRotFileHandlers(Collection<PerRotFileHandlerBean> perRotFileHandlers) {
        Set<PerRotFileHandlerBean> temp = new HashSet();
        temp.addAll(perRotFileHandlers);
        this.perRotFileHandlers = temp;
    }

    public Set<SizeRotFileHandlerBean> getSizeRotFileHandlers() {
        return sizeRotFileHandlers;
    }

    public void setSizeRotFileHandlers(Collection<SizeRotFileHandlerBean> sizeRotFileHandlers) {
        Set<SizeRotFileHandlerBean> temp = new HashSet();
        temp.addAll(sizeRotFileHandlers);
        this.sizeRotFileHandlers = temp;
    }

    public Set<CustomHandlerBean> getCustomHandlers() {
        return customHandlers;
    }

    public void setCustomHandlers(Collection<CustomHandlerBean> customHandlers) {
        Set<CustomHandlerBean> temp = new HashSet();
        temp.addAll(customHandlers);
        this.customHandlers = temp;
    }

    public Set<LoggerBean> getLoggers() {
        return loggers;
    }

    public void setLoggers(Collection<LoggerBean> loggers) {
        Set<LoggerBean> temp = new HashSet();
        temp.addAll(loggers);
        this.loggers = temp;
    }

    public RootLoggerAS7Bean getRootLogger() {
        return rootLogger;
    }

    public void setRootLogger(RootLoggerAS7Bean rootLogger) {
        this.rootLogger = rootLogger;
    }
}
