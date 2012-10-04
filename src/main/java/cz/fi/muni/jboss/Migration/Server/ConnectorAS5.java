package cz.fi.muni.jboss.Migration.Server;

import javax.xml.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * User: Roman Jakubco
 * Date: 10/2/12
 * Time: 9:12 PM
 */
@XmlRootElement(name = "connector")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "connector")
public  class ConnectorAS5 {
    // only attributes which can be migrated to AS7
    //AJP+ HTTP ... similar attributes

    //Common atributy
    @XmlAttribute(name = "enableLookups")
    private Boolean enableLookups;
    @XmlAttribute(name = "maxPostSize")
    private Integer maxPostSize;
    @XmlAttribute(name = "maxSavePostSize")
    private Integer maxSavePostSize;
    @XmlAttribute(name = "protocol")
    private String protocol;
    @XmlAttribute(name = "proxyName")
    private String proxyName;
    @XmlAttribute(name = "proxyPort")
    private Integer proxyPort;
    @XmlAttribute(name = "redirectPort")
    private Integer redirectPort;
    @XmlAttribute(name = "SSLEnabled")
    private Boolean sslEnabled;
    @XmlAttribute(name = "scheme")
    private String scheme;
    @XmlAttribute(name = "secure")
    private Boolean secure;

    //standard attributes for HTTP connector

    @XmlAttribute(name = "executor")
    private String executor;
    @XmlAttribute(name = "port")
    private Integer port;

    //SSL attributes
    @XmlAttribute(name = "clientAuth")
    private Boolean clientAuth;
    @XmlAttribute(name = "keystoreFile")
    private String keystoreFile;
    @XmlAttribute(name = "sslProtocol")
    private String sslProtocol;
    @XmlAttribute(name = "ciphers")
    private String ciphers;
    @XmlAttribute(name = "keyAlias")
    private  String keyAlias;
    @XmlAttribute(name = "truststoreFile")
    private String trustStoreFile;
    @XmlAttribute(name = "keystorePass")
    private String keysotrePass;
    @XmlAttribute(name = "truststorePass")
    private String truststorePass;

    public Boolean getEnableLookups() {
        return enableLookups;
    }

    public void setEnableLookups(Boolean enableLookups) {
        this.enableLookups = enableLookups;
    }

    public Integer getMaxPostSize() {
        return maxPostSize;
    }

    public void setMaxPostSize(Integer maxPostSize) {
        this.maxPostSize = maxPostSize;
    }

    public Integer getMaxSavePostSize() {
        return maxSavePostSize;
    }

    public void setMaxSavePostSize(Integer maxSavePostSize) {
        this.maxSavePostSize = maxSavePostSize;
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

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public Integer getRedirectPort() {
        return redirectPort;
    }

    public void setRedirectPort(Integer redirectPort) {
        this.redirectPort = redirectPort;
    }

    public Boolean getSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(Boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getClientAuth() {
        return clientAuth;
    }

    public void setClientAuth(Boolean clientAuth) {
        this.clientAuth = clientAuth;
    }

    public String getKeystoreFile() {
        return keystoreFile;
    }

    public void setKeystoreFile(String keystoreFile) {
        this.keystoreFile = keystoreFile;
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

    public String getTrustStoreFile() {
        return trustStoreFile;
    }

    public void setTrustStoreFile(String trustStoreFile) {
        this.trustStoreFile = trustStoreFile;
    }

    public String getKeysotrePass() {
        return keysotrePass;
    }

    public void setKeysotrePass(String keysotrePass) {
        this.keysotrePass = keysotrePass;
    }

    public String getTruststorePass() {
        return truststorePass;
    }

    public void setTruststorePass(String truststorePass) {
        this.truststorePass = truststorePass;
    }

    @Override
    public String toString() {
        return "ConnectorAS5{" +
                "enableLookups=" + enableLookups +
                ", maxPostSize=" + maxPostSize +
                ", maxSavePostSize=" + maxSavePostSize +
                ", protocol='" + protocol + '\'' +
                ", proxyName='" + proxyName + '\'' +
                ", proxyPort=" + proxyPort +
                ", redirectPort=" + redirectPort +
                ", sslEnabled=" + sslEnabled +
                ", scheme='" + scheme + '\'' +
                ", secure=" + secure +
                ", executor='" + executor + '\'' +
                ", port=" + port +
                ", clientAuth=" + clientAuth +
                ", keystoreFile='" + keystoreFile + '\'' +
                ", sslProtocol='" + sslProtocol + '\'' +
                ", ciphers='" + ciphers + '\'' +
                ", keyAlias='" + keyAlias + '\'' +
                ", trustStoreFile='" + trustStoreFile + '\'' +
                ", keysotrePass='" + keysotrePass + '\'' +
                ", truststorePass='" + truststorePass + '\'' +
                '}';
    }
}
