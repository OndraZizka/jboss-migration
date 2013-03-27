package cz.muni.fi.jboss.migration.migrators.security;

import cz.muni.fi.jboss.migration.conf.GlobalConfiguration;
import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.ex.ApplyMigrationException;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.NodeGenerationException;
import cz.muni.fi.jboss.migration.migrators.security.jaxb.*;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Migrator of security subsystem implementing IMigrator
 *
 * @author Roman Jakubco
 */
public class SecurityMigrator extends AbstractMigrator {
    
    @Override protected String getConfigPropertyModuleName() { return "security"; }
    

    public SecurityMigrator(GlobalConfiguration globalConfig, MultiValueMap config) {
        super(globalConfig, config);
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException {
        try {
            File file = new File( getGlobalConfig().getAS5Config().getConfDir(), "login-config.xml");
            if( ! file.canRead() ) {
                throw new LoadMigrationException("Can't read: " + file.getAbsolutePath());
            }

            Unmarshaller unmarshaller = JAXBContext.newInstance(SecurityAS5Bean.class).createUnmarshaller();
            SecurityAS5Bean securityAS5 = (SecurityAS5Bean) unmarshaller.unmarshal(file);

            MigrationData mData = new MigrationData();
            mData.getConfigFragments().addAll(securityAS5.getApplicationPolicies());

            ctx.getMigrationData().put(SecurityMigrator.class, mData);
        } catch (JAXBException e) {
            throw new LoadMigrationException(e);
        }
    }

    @Override
    public void apply(MigrationContext ctx) throws ApplyMigrationException {
        try {
            Document doc = ctx.getAS7ConfigXmlDoc();
            NodeList subsystems = doc.getElementsByTagName("subsystem");
            for (int i = 0; i < subsystems.getLength(); i++) {
                if (!(subsystems.item(i) instanceof Element)) {
                    continue;
                }
                if (((Element) subsystems.item(i)).getAttribute("xmlns").contains("security")) {
                    Node parent = subsystems.item(i).getFirstChild();
                    while (!(parent instanceof Element)) {
                        parent = parent.getNextSibling();
                    }

                    for (Node node : generateDomElements(ctx)) {
                        Node adopted = doc.adoptNode(node.cloneNode(true));
                        parent.appendChild(adopted);
                    }
                    break;
                }
            }
        } catch (NodeGenerationException e) {
            throw new ApplyMigrationException(e);
        }
    }

    @Override
    public List<Node> generateDomElements(MigrationContext ctx) throws NodeGenerationException {
        try {
            JAXBContext secDomainCtx = JAXBContext.newInstance(SecurityDomainBean.class);
            List<Node> nodeList = new ArrayList();
            Marshaller secDomMarshaller = secDomainCtx.createMarshaller();

            for (IConfigFragment fragment : ctx.getMigrationData().get(SecurityMigrator.class).getConfigFragments()) {
                if (!(fragment instanceof ApplicationPolicyBean)) {
                    throw new NodeGenerationException("Object is not part of Security migration!");
                }

                Document doc = Utils.createXmlDocumentBuilder().newDocument();
                secDomMarshaller.marshal(appPolicyMigration((ApplicationPolicyBean) fragment, ctx), doc);
                nodeList.add(doc.getDocumentElement());
            }

            return nodeList;
        } catch (JAXBException e) {
            throw new NodeGenerationException(e);
        }
    }

    @Override
    public List<String> generateCliScripts(MigrationContext ctx) throws CliScriptException {
        try {
            List<String> list = new ArrayList();
            Unmarshaller secUnmarshaller = JAXBContext.newInstance(SecurityDomainBean.class).createUnmarshaller();

            for (Node node : generateDomElements(ctx)) {
                SecurityDomainBean securityDomain = (SecurityDomainBean) secUnmarshaller.unmarshal(node);
                list.add(createSecurityDomainScript(securityDomain));
            }

            return list;
        } catch (NodeGenerationException | JAXBException e) {
            throw new CliScriptException(e);
        }
    }

    /**
     * Method for migrating application-policy from AS5 to AS7
     *
     * @param appPolicy object representing application-policy
     * @param ctx  migration context
     * @return  created security-domain
     */
    public static SecurityDomainBean appPolicyMigration(ApplicationPolicyBean appPolicy, MigrationContext ctx){
        Set<LoginModuleAS7Bean> loginModules = new HashSet();
        SecurityDomainBean securityDomain = new SecurityDomainBean();

        securityDomain.setSecurityDomainName(appPolicy.getApplicationPolicyName());
        securityDomain.setCacheType("default");

        for (LoginModuleAS5Bean lmAS5 : appPolicy.getLoginModules()) {
            Set<ModuleOptionAS7Bean> moduleOptions = new HashSet();
            LoginModuleAS7Bean lmAS7 = new LoginModuleAS7Bean();
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
                for (ModuleOptionAS5Bean moAS5 : lmAS5.getModuleOptions()) {
                    ModuleOptionAS7Bean moAS7 = new ModuleOptionAS7Bean();
                    moAS7.setModuleOptionName(moAS5.getModuleName());

                    // TODO: Module-option using file can only use .properties?
                    if (moAS5.getModuleValue().contains("properties")) {
                        String value;
                        if (moAS5.getModuleValue().contains("/")) {
                            value = StringUtils.substringAfterLast(moAS5.getModuleValue(), "/");
                        } else {
                            value = moAS5.getModuleValue();
                        }
                        moAS7.setModuleOptionValue("${jboss.server.config.dir}/" + value);

                        FileTransferInfo rd = new FileTransferInfo();
                        rd.setName(value);
                        rd.setType(FileTransferInfo.Type.SECURITY);
                        ctx.getRollbackData().add(rd);
                    } else {
                        moAS7.setModuleOptionValue(moAS5.getModuleValue());
                    }

                    moduleOptions.add(moAS7);
                }
            }

            lmAS7.setModuleOptions(moduleOptions);
            loginModules.add(lmAS7);
        }

        securityDomain.setLoginModules(loginModules);

        return securityDomain;
    }

    /**
     * Creating CLI script for adding security-domain to AS7
     *
     * @param securityDomain object representing migrated security-domain
     * @return string containing created CLI script
     * @throws CliScriptException if required attributes are missing
     */
    public static String createSecurityDomainScript(SecurityDomainBean securityDomain)
            throws CliScriptException {
        String errMsg = " in security-domain must be set.";
        Utils.throwIfBlank(securityDomain.getSecurityDomainName(), errMsg, "Security name");

        CliAddCommandBuilder builder = new CliAddCommandBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=security/security-domain=");

        resultScript.append(securityDomain.getSecurityDomainName()).append(":add(");
        builder.addProperty("cache-type", securityDomain.getCacheType());

        resultScript.append(builder.asString()).append(")\n");

        if (securityDomain.getLoginModules() != null) {
            for (LoginModuleAS7Bean loginModAS7 : securityDomain.getLoginModules()) {
                resultScript.append("/subsystem=security/security-domain=").append(securityDomain.getSecurityDomainName());
                resultScript.append("/authentication=classic:add(login-modules=[{");

                if ((loginModAS7.getLoginModuleCode() != null) || !(loginModAS7.getLoginModuleCode().isEmpty())) {
                    resultScript.append("\"code\"=>\"").append(loginModAS7.getLoginModuleCode()).append("\"");
                }
                if ((loginModAS7.getLoginModuleFlag() != null) || !(loginModAS7.getLoginModuleFlag().isEmpty())) {
                    resultScript.append(", \"flag\"=>\"").append(loginModAS7.getLoginModuleFlag()).append("\"");
                }

                resultScript.append(builder.asString());

                if (loginModAS7.getModuleOptions() != null) {
                    if (!loginModAS7.getModuleOptions().isEmpty()) {
                        StringBuilder modulesBuilder = new StringBuilder();
                        for (ModuleOptionAS7Bean moduleOptionAS7 : loginModAS7.getModuleOptions()) {
                            modulesBuilder.append(", (\"").append(moduleOptionAS7.getModuleOptionName()).append("\"=>");
                            modulesBuilder.append("\"").append(moduleOptionAS7.getModuleOptionValue()).append("\")");
                        }

                        String modules = modulesBuilder.toString().replaceFirst(",", "");
                        modules = modules.replaceFirst(" ", "");

                        if (!modules.isEmpty()) {
                            resultScript.append(", \"module-option\"=>[").append(modules).append("]");
                        }
                    }

                }
            }
        }

        resultScript.append("}])");

        return resultScript.toString();
    }
}
