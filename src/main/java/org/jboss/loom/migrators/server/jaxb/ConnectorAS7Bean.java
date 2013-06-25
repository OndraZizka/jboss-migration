/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.server.jaxb;

import javax.xml.bind.annotation.*;
import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * JAXB bean for connector in AS7 (AS7)
 *
 * @author Roman Jakubco
 * @deprecated  Should be read over Management API.
 */
@XmlRootElement(name = "connector")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "connector")
public class ConnectorAS7Bean {

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

    // SSL attributes
    @XmlPath("/ssl/@name")
    private String sslName;

    @XmlPath("/ssl/@verify-client")
    private String verifyClient;

    @XmlPath("/ssl/@verify-depth")
    private String verifyDepth;

    @XmlPath("/ssl/@certificate-key-file")
    private String certifKeyFile;

    @XmlPath("/ssl/@certificate-file")
    private String certifFile;

    @XmlPath("/ssl/@password")
    private String password;

    @XmlPath("/ssl/@protocol")
    private String sslProtocol;

    @XmlPath("/ssl/@ciphers")
    private String ciphers;

    @XmlPath("/ssl/@key-alias")
    private String keyAlias;

    @XmlPath("/ssl/@ca-certificate-file")
    private String caCertifFile;

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

    public String getCertifKeyFile() {
        return certifKeyFile;
    }

    public void setCertifKeyFile(String certifKeyFile) {
        this.certifKeyFile = certifKeyFile;
    }

    public String getCertifFile() {
        return certifFile;
    }

    public void setCertifFile(String certifFile) {
        this.certifFile = certifFile;
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

    public String getCaCertifFile() {
        return caCertifFile;
    }

    public void setCaCertifFile(String caCertifFile) {
        this.caCertifFile = caCertifFile;
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

