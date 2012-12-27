package cz.fi.muni.jboss.migration.server;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 10/2/12
 * Time: 9:13 PM
 */
@XmlRootElement(name = "connector")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "connector")
public class ConnectorAS7{
    @XmlAttribute(name = "name")
    private String connectorName;
    @XmlAttribute(name = "socket-binding")
    private String socketBinding;
    @XmlAttribute(name = "enable-lookups")
    private String enableLookups;
    @XmlAttribute(name = "max-post-size")
    private String maxPostSize;
    @XmlAttribute(name = "max-save-post-size")
    private String maxSavePostSize;
    @XmlAttribute(name = "max-connections")
    private String maxConnections;
    @XmlAttribute(name = "protocol")
    private String protocol;
    @XmlAttribute(name = "proxy-name")
    private String proxyName;
    @XmlAttribute(name = "proxy-port")
    private String proxyPort;
    @XmlAttribute(name = "redirect-port")
    private String redirectPort;
    @XmlAttribute(name = "scheme")
    private String scheme;
    @XmlAttribute(name = "secure")
    private String secure;
    @XmlAttribute(name = "enabled")
    private String enabled;
    @XmlAttribute(name = "executor")
    private String executor;

    //SSL attributes
    @XmlPath("/ssl/@name")
    private String sslName;
    @XmlPath("/ssl/@verify-client")
    private  String verifyClient;
    @XmlPath("/ssl/@verify-depth")
    private String verifyDepth;
    @XmlPath("/ssl/@certificate-key-file")
    private String certificateKeyFile;
    @XmlPath("/ssl/@certificate-file")
    private String  certificateFile;
    @XmlPath("/ssl/@password")
    private String password;
    @XmlPath("/ssl/@protocol")
    private String sslProtocol;
    @XmlPath("/ssl/@ciphers")
    private String ciphers;
    @XmlPath("/ssl/@key-alias")
    private String keyAlias;
    @XmlPath("/ssl/@ca-certificate-file")
    private String caCertificateFile;
    @XmlPath("/ssl/@session-cache-size")
    private String sessionCacheSize;
    @XmlPath("/ssl/@session-timeout")
    private String sessionTimeout;

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public String getSocketBinding() {
        return socketBinding;
    }

    public void setSocketBinding(String socketBinding) {
        this.socketBinding = socketBinding;
    }

    public String getEnableLookups() {
        return enableLookups;
    }

    public void setEnableLookups(String enableLookups) {
        this.enableLookups = enableLookups;
    }

    public String getMaxPostSize() {
        return maxPostSize;
    }

    public void setMaxPostSize(String maxPostSize) {
        this.maxPostSize = maxPostSize;
    }

    public String getMaxSavePostSize() {
        return maxSavePostSize;
    }

    public void setMaxSavePostSize(String maxSavePostSize) {
        this.maxSavePostSize = maxSavePostSize;
    }

    public String getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(String maxConnections) {
        this.maxConnections = maxConnections;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProxyName() {
        return proxyName;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getRedirectPort() {
        return redirectPort;
    }

    public void setRedirectPort(String redirectPort) {
        this.redirectPort = redirectPort;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getSecure() {
        return secure;
    }

    public void setSecure(String secure) {
        this.secure = secure;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getSslName() {
        return sslName;
    }

    public void setSslName(String sslName) {
        this.sslName = sslName;
    }

    public String getVerifyClient() {
        return verifyClient;
    }

    public void setVerifyClient(String verifyClient) {
        this.verifyClient = verifyClient;
    }

    public String getVerifyDepth() {
        return verifyDepth;
    }

    public void setVerifyDepth(String verifyDepth) {
        this.verifyDepth = verifyDepth;
    }

    public String getCertificateKeyFile() {
        return certificateKeyFile;
    }

    public void setCertificateKeyFile(String certificateKeyFile) {
        this.certificateKeyFile = certificateKeyFile;
    }

    public String getCertificateFile() {
        return certificateFile;
    }

    public void setCertificateFile(String certificateFile) {
        this.certificateFile = certificateFile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSslProtocol() {
        return sslProtocol;
    }

    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    public String getCiphers() {
        return ciphers;
    }

    public void setCiphers(String ciphers) {
        this.ciphers = ciphers;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getCaCertificateFile() {
        return caCertificateFile;
    }

    public void setCaCertificateFile(String caCertificateFile) {
        this.caCertificateFile = caCertificateFile;
    }

    public String getSessionCacheSize() {
        return sessionCacheSize;
    }

    public void setSessionCacheSize(String sessionCacheSize) {
        this.sessionCacheSize = sessionCacheSize;
    }

    public String getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(String sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}

