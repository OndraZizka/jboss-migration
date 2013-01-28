package cz.muni.fi.jboss.migration.migrators.security;

import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.ex.ApplyMigrationException;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import javafx.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Roman Jakubco
 *         Date: 1/24/13
 *         Time: 10:42 AM
 */
public class SecurityMigrator implements IMigrator {

    private GlobalConfiguration globalConfig;

    private List<Pair<String,String>> config;

    public SecurityMigrator(GlobalConfiguration globalConfig, List<Pair<String, String>> config){
        this.globalConfig = globalConfig;
        this.config =  config;
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException, FileNotFoundException{
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(SecurityAS5.class).createUnmarshaller();

            File file = new File(globalConfig.getDirAS5() + globalConfig.getProfileAS5() + File.separator +
                    "conf" + File.separator + "login-config.xml");

            if(file.canRead()){
                SecurityAS5 securityAS5 = (SecurityAS5)unmarshaller.unmarshal(file);

                MigrationData mData = new MigrationData();
                mData.getConfigFragment().addAll(securityAS5.getApplicationPolicies());

                ctx.getMigrationData().put(SecurityMigrator.class, mData);

            } else {
                throw new FileNotFoundException("Cannot find/open file: " + file.getAbsolutePath());
            }

        } catch (JAXBException e) {
            throw new LoadMigrationException(e);
        }
    }

    @Override
    public void apply(MigrationContext ctx) throws ApplyMigrationException{
        try {
            File standalone = new File(globalConfig.getStandaloneFilePath());
            Document doc = ctx.getDocBuilder().parse(standalone);
            NodeList subsystems = doc.getElementsByTagName("subsystem");
            for(int i = 0; i < subsystems.getLength(); i++){
                if(!(subsystems.item(i) instanceof Element)){
                    continue;
                }
                if(((Element) subsystems.item(i)).getAttribute("xmlns").contains("security")){
                    Node parent = subsystems.item(i).getFirstChild();
                    while(!(parent instanceof Element)){
                        parent = parent.getNextSibling();
                    }

                    for(Node node : generateDomElements(ctx)){
                        Node adopted = doc.adoptNode(node.cloneNode(true));
                        parent.appendChild(adopted);
                    }
                    break;
                }
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult result = new StreamResult(standalone);
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);

        } catch (SAXException | IOException | MigrationException | TransformerException e) {
            throw new ApplyMigrationException(e);
        }

    }

    @Override
    public List<Node> generateDomElements(MigrationContext ctx) throws MigrationException{
        try {
            JAXBContext secDomainCtx = JAXBContext.newInstance(SecurityDomain.class);
            List<Node> nodeList = new ArrayList();
            Marshaller secDomMarshaller = secDomainCtx.createMarshaller();

            for (IConfigFragment data : ctx.getMigrationData().get(SecurityMigrator.class).getConfigFragment()) {
                if(!(data instanceof ApplicationPolicy)){
                    throw new MigrationException("Error: Object is not part of Security migration!");
                }
                ApplicationPolicy appPolicy = (ApplicationPolicy) data;

                Set<LoginModuleAS7> loginModules = new HashSet();
                SecurityDomain securityDomain = new SecurityDomain();
                securityDomain.setSecurityDomainName(appPolicy.getApplicationPolicyName());
                securityDomain.setCacheType("default");

                for (LoginModuleAS5 lmAS5 : appPolicy.getLoginModules()) {
                    Set<ModuleOptionAS7> moduleOptions = new HashSet();
                    LoginModuleAS7 lmAS7 = new LoginModuleAS7();
                    lmAS7.setLoginModuleFlag(lmAS5.getLoginModuleFlag());

                    switch (StringUtils.substringAfterLast(lmAS5.getLoginModule(), ".")) {
                        case "ClientLoginModule":
                            lmAS7.setLoginModuleCode("Client");
                            break;
                        //*
                        case "BaseCertLoginModule":
                            lmAS7.setLoginModuleCode("Certificate");
                            break;
                        case "CertRolesLoginModule":
                            lmAS7.setLoginModuleCode("CertificateRoles");
                            break;
                        //*
                        case "DatabaseServerLoginModule":
                            lmAS7.setLoginModuleCode("Database");
                            break;
                        case "DatabaseCertLoginModule":
                            lmAS7.setLoginModuleCode("DatabaseCertificate");
                            break;
                        case "IdentityLoginModule":
                            lmAS7.setLoginModuleCode("Identity");
                            break;
                        case "LdapLoginModule":
                            lmAS7.setLoginModuleCode("Ldap");
                            break;
                        case "LdapExtLoginModule":
                            lmAS7.setLoginModuleCode("LdapExtended");
                            break;
                        case "RoleMappingLoginModule":
                            lmAS7.setLoginModuleCode("RoleMapping");
                            break;
                        case "RunAsLoginModule":
                            lmAS7.setLoginModuleCode("RunAs");
                            break;
                        case "SimpleServerLoginModule":
                            lmAS7.setLoginModuleCode("Simple");
                            break;
                        case "ConfiguredIdentityLoginModule":
                            lmAS7.setLoginModuleCode("ConfiguredIdentity");
                            break;
                        case "SecureIdentityLoginModule":
                            lmAS7.setLoginModuleCode("SecureIdentity");
                            break;
                        case "PropertiesUsersLoginModule":
                            lmAS7.setLoginModuleCode("PropertiesUsers");
                            break;
                        case "SimpleUsersLoginModule":
                            lmAS7.setLoginModuleCode("SimpleUsers");
                            break;
                        case "LdapUsersLoginModule":
                            lmAS7.setLoginModuleCode("LdapUsers");
                            break;
                        case "Krb5loginModule":
                            lmAS7.setLoginModuleCode("Kerberos");
                            break;
                        case "SPNEGOLoginModule":
                            lmAS7.setLoginModuleCode("SPNEGOUsers");
                            break;
                        case "AdvancedLdapLoginModule":
                            lmAS7.setLoginModuleCode("AdvancedLdap");
                            break;
                        case "AdvancedADLoginModule":
                            lmAS7.setLoginModuleCode("AdvancedADldap");
                            break;
                        case "UsersRolesLoginModule":
                            lmAS7.setLoginModuleCode("UsersRoles");
                            break;
                        default:
                            lmAS7.setLoginModuleCode(lmAS5.getLoginModule());
                    }

                    if (lmAS5.getModuleOptions() != null) {
                        for (ModuleOptionAS5 moAS5 : lmAS5.getModuleOptions()) {
                            ModuleOptionAS7 moAS7 = new ModuleOptionAS7();
                            moAS7.setModuleOptionName(moAS5.getModuleName());

                            // TODO: Module-option using file can only use .properties?
                            if(moAS5.getModuleValue().contains("properties")){
                                String value;
                                if(moAS5.getModuleValue().contains("/")){
                                    value = StringUtils.substringAfterLast(moAS5.getModuleValue(), "/");
                                } else{
                                    value = moAS5.getModuleValue();
                                }
                                moAS7.setModuleOptionValue("${jboss.server.config.dir}/" + value);

                                CopyMemory cp = new CopyMemory();
                                cp.setName(value);
                                cp.setType("security");
                                ctx.getCopyMemories().add(cp);
                            } else{
                                moAS7.setModuleOptionValue(moAS5.getModuleValue());
                            }

                            moduleOptions.add(moAS7);
                        }
                    }

                    lmAS7.setModuleOptions(moduleOptions);
                    loginModules.add(lmAS7);
                }

                securityDomain.setLoginModules(loginModules);

                Document doc = ctx.getDocBuilder().newDocument();
                secDomMarshaller.marshal(securityDomain, doc);
                nodeList.add(doc.getDocumentElement());
            }

            return nodeList;
        } catch (JAXBException e) {
            throw new MigrationException(e);
        }
    }

    @Override
    public List<String> generateCliScripts(MigrationContext ctx) throws CliScriptException{
        try {
            List<String> list = new ArrayList();
            Unmarshaller secUnmarshaller = JAXBContext.newInstance(SecurityDomain.class).createUnmarshaller();

            for(Node node : generateDomElements(ctx)){
                SecurityDomain securityDomain = (SecurityDomain) secUnmarshaller.unmarshal(node);
                list.add(createSecurityDomainScript(securityDomain, ctx));
            }

            return list;
        } catch (MigrationException | JAXBException e) {
            throw new CliScriptException(e);
        }
    }

    public String createSecurityDomainScript(SecurityDomain securityDomain, MigrationContext ctx) throws CliScriptException{
        if((securityDomain.getSecurityDomainName() == null) || (securityDomain.getSecurityDomainName().isEmpty())){
            throw new CliScriptException("Error: name of the security domain cannot be null or empty",
                    new NullPointerException());
        }

        String script = "/subsystem=security/security-domain=";
        script = script.concat(securityDomain.getSecurityDomainName() + ":add(");
        script = ctx.checkingMethod(script, "cache-type", securityDomain.getCacheType() + ")\n");

        if(securityDomain.getLoginModules() != null){
            for(LoginModuleAS7 loginModuleAS7 : securityDomain.getLoginModules()){
                script = script.concat("/subsystem=security/security-domain=" + securityDomain.getSecurityDomainName());
                script = script.concat("/authentication=classic:add(login-modules=[{");
                script = ctx.checkingMethod(script, "\"code\"", ">\"" + loginModuleAS7.getLoginModuleCode() + "\"");
                script = ctx.checkingMethod(script, ", \"flag\"", ">\"" + loginModuleAS7.getLoginModuleFlag() + "\"");

                if(loginModuleAS7.getModuleOptions() != null){
                    if(!loginModuleAS7.getModuleOptions().isEmpty()) {
                        String modules= "";
                        for(ModuleOptionAS7 moduleOptionAS7 : loginModuleAS7.getModuleOptions()){
                            modules = modules.concat(", (\"" + moduleOptionAS7.getModuleOptionName() + "\"=>");
                            modules = modules.concat("\"" + moduleOptionAS7.getModuleOptionValue() + "\")");
                        }

                        modules = modules.replaceFirst("\\,", "");
                        modules = modules.replaceFirst(" ", "");

                        if(!modules.isEmpty()){
                            script = script.concat(", \"module-option\"=>[" + modules + "]");
                        }
                    }

                }
            }
        }

        script = script.concat("}])");

        return script;
    }
}
