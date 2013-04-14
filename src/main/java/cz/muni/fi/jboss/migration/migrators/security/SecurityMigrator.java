package cz.muni.fi.jboss.migration.migrators.security;

import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.actions.CliCommandAction;
import cz.muni.fi.jboss.migration.actions.CopyAction;
import cz.muni.fi.jboss.migration.conf.GlobalConfiguration;
import cz.muni.fi.jboss.migration.ex.*;
import cz.muni.fi.jboss.migration.migrators.security.jaxb.*;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
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

    // Files which must be copied into AS7
    private Set<String> fileNames = new HashSet<>();

    @Override
    protected String getConfigPropertyModuleName() {
        return "security";
    }


    public SecurityMigrator(GlobalConfiguration globalConfig, MultiValueMap config) {
        super(globalConfig, config);
    }

    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException {
        try {
            File file = new File(getGlobalConfig().getAS5Config().getConfDir(), "login-config.xml");
            if (!file.canRead()) {
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
    public void createActions(MigrationContext ctx) throws ActionException {
        for (IConfigFragment fragment : ctx.getMigrationData().get(SecurityMigrator.class).getConfigFragments()) {
            if (fragment instanceof ApplicationPolicyBean) {
                try {
                    ctx.getActions().addAll(createSecurityDomainCliAction(
                            migrateAppPolicy((ApplicationPolicyBean) fragment, ctx)));
                    continue;
                } catch (CliScriptException e) {
                    throw new ActionException("Migration of application-policy failed: " + e.getMessage(), e);
                }
            }
            throw new ActionException("Config fragment unrecognized by " + this.getClass().getSimpleName() + ": " + fragment);
        }

        for (String fileName : this.fileNames) {
            File src;
            try {
                src = Utils.searchForFile(fileName, getGlobalConfig().getAS5Config().getProfileDir()).iterator().next();
            } catch (CopyException e) {
                throw new ActionException("Copying of file for security failed: " + e.getMessage(), e);
            }

            File target = Utils.createPath(getGlobalConfig().getAS7Config().getDir(), "standalone", "configuration",
                    src.getName());

            // Default value for overwrite => false
            ctx.getActions().add(new CopyAction(src, target, false));
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
                    throw new NodeGenerationException("Config fragment unrecognized by " + this.getClass().getSimpleName() + ": " + fragment);
                }

                Document doc = Utils.createXmlDocumentBuilder().newDocument();
                secDomMarshaller.marshal(migrateAppPolicy((ApplicationPolicyBean) fragment, ctx), doc);
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
     * Migrates application-policy from AS5 to AS7
     *
     * @param appPolicy object representing application-policy
     * @param ctx       migration context
     * @return created security-domain
     */
    public SecurityDomainBean migrateAppPolicy(ApplicationPolicyBean appPolicy, MigrationContext ctx) {
        Set<LoginModuleAS7Bean> loginModules = new HashSet();
        SecurityDomainBean securityDomain = new SecurityDomainBean();

        securityDomain.setSecurityDomainName(appPolicy.getApplicationPolicyName());
        securityDomain.setCacheType("default");
        if (appPolicy.getLoginModules() != null) {
            for (LoginModuleAS5Bean lmAS5 : appPolicy.getLoginModules()) {
                loginModules.add(createLoginModule(lmAS5));
            }
        }

        securityDomain.setLoginModules(loginModules);

        return securityDomain;
    }

    private LoginModuleAS7Bean createLoginModule(LoginModuleAS5Bean lmAS5) {
        Set<ModuleOptionAS7Bean> moduleOptions = new HashSet();
        LoginModuleAS7Bean lmAS7 = new LoginModuleAS7Bean();
        lmAS7.setLoginModuleFlag(lmAS5.getLoginModuleFlag());

        String type = StringUtils.substringAfterLast(lmAS5.getLoginModule(), ".");
        switch (type) {
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
                moduleOptions.add(createModuleOption(moAS5));
            }
        }

        lmAS7.setModuleOptions(moduleOptions);

        return lmAS7;
    }

    private ModuleOptionAS7Bean createModuleOption(ModuleOptionAS5Bean moAS5) {
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

            this.fileNames.add(value);
        } else {
            moAS7.setModuleOptionValue(moAS5.getModuleValue());
        }

        return moAS7;
    }

    /**
     * Creates a list of CliCommandActions for adding a Security-Domain
     *
     * @param domain Security-Domain
     * @return created list containing CliCommandActions for adding the Security-Domain
     * @throws CliScriptException if required attributes for a creation of the CLI command of the Security-Domain
     *                            are missing or are empty (security-domain-name)
     */
    public static List<CliCommandAction> createSecurityDomainCliAction(SecurityDomainBean domain)
            throws CliScriptException {
        String errMsg = " in security-domain must be set.";
        Utils.throwIfBlank(domain.getSecurityDomainName(), errMsg, "Security name");

        List<CliCommandAction> actions = new ArrayList();

        ModelNode domainCmd = new ModelNode();
        domainCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        domainCmd.get(ClientConstants.OP_ADDR).add("subsystem", "security");
        domainCmd.get(ClientConstants.OP_ADDR).add("security-domain", domain.getSecurityDomainName());

        actions.add(new CliCommandAction(createSecurityDomainScript(domain), domainCmd));

        if (domain.getLoginModules() != null) {
            for (LoginModuleAS7Bean module : domain.getLoginModules()) {
                actions.add(createLoginModuleCliAction(domain, module));
            }
        }

        return actions;
    }

    /**
     * Creates CliCommandAction for adding a Login-Module of the specific Security-Domain
     *
     * @param domain Security-Domain containing Login-Module
     * @param module Login-Module
     * @return created CliCommandAction for adding the Login-Module
     */
    public static CliCommandAction createLoginModuleCliAction(SecurityDomainBean domain, LoginModuleAS7Bean module) {
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);
        request.get(ClientConstants.OP_ADDR).add("subsystem", "security");
        request.get(ClientConstants.OP_ADDR).add("security-domain", domain.getSecurityDomainName());
        request.get(ClientConstants.OP_ADDR).add("authentication", "classic");

        ModelNode moduleNode = new ModelNode();
        ModelNode list = new ModelNode();

        if (module.getModuleOptions() != null) {
            ModelNode optionNode = new ModelNode();
            for (ModuleOptionAS7Bean option : module.getModuleOptions()) {
                optionNode.get(option.getModuleOptionName()).set(option.getModuleOptionValue());
            }
            moduleNode.get("module-options").set(optionNode);
        }

        CliApiCommandBuilder builder = new CliApiCommandBuilder(moduleNode);
        builder.addProperty("flag", module.getLoginModuleFlag());
        builder.addProperty("code", module.getLoginModuleCode());

        // Needed for CLI because parameter login-modules requires LIST
        list.add(builder.getCommand());

        request.get("login-modules").set(list);

        return new CliCommandAction(createLoginModuleScript(domain, module), request);
    }

    /**
     * Creates a CLI script for adding Security-Domain to AS7
     *
     * @param securityDomain object representing migrated security-domain
     * @return created string containing the CLI script for adding the Security-Domain
     * @throws CliScriptException if required attributes are missing
     */
    private static String createSecurityDomainScript(SecurityDomainBean securityDomain)
            throws CliScriptException {
        String errMsg = " in security-domain must be set.";
        Utils.throwIfBlank(securityDomain.getSecurityDomainName(), errMsg, "Security name");

        CliAddScriptBuilder builder = new CliAddScriptBuilder();
        StringBuilder resultScript = new StringBuilder("/subsystem=security/security-domain=");

        resultScript.append(securityDomain.getSecurityDomainName()).append(":add(");
        builder.addProperty("cache-type", securityDomain.getCacheType());

        resultScript.append(builder.asString()).append(")");

        return resultScript.toString();
    }

    /**
     * Creates a CLI script for adding a Login-Module of the specific Security-Domain
     *
     * @param domain Security-Domain containing Login-Module
     * @param module Login-Module
     * @return created string containing the CLI script for adding the Login-Module
     */
    private static String createLoginModuleScript(SecurityDomainBean domain, LoginModuleAS7Bean module) {
        StringBuilder resultScript = new StringBuilder("/subsystem=security/security-domain=" +
                domain.getSecurityDomainName());
        resultScript.append("/authentication=classic:add(login-modules=[{");

        if ((module.getLoginModuleCode() != null) || !(module.getLoginModuleCode().isEmpty())) {
            resultScript.append("\"code\"=>\"").append(module.getLoginModuleCode()).append("\"");
        }
        if ((module.getLoginModuleFlag() != null) || !(module.getLoginModuleFlag().isEmpty())) {
            resultScript.append(", \"flag\"=>\"").append(module.getLoginModuleFlag()).append("\"");
        }

        if ((module.getModuleOptions() != null) || (!module.getModuleOptions().isEmpty())) {
            StringBuilder modulesBuilder = new StringBuilder();
            for (ModuleOptionAS7Bean moduleOptionAS7 : module.getModuleOptions()) {
                modulesBuilder.append(", (\"").append(moduleOptionAS7.getModuleOptionName()).append("\"=>");
                modulesBuilder.append("\"").append(moduleOptionAS7.getModuleOptionValue()).append("\")");
            }

            String modules = modulesBuilder.toString().replaceFirst(",", "");
            modules = modules.replaceFirst(" ", "");

            if (!modules.isEmpty()) {
                resultScript.append(", \"module-option\"=>[").append(modules).append("]");
            }
        }

        return resultScript.toString();
    }
}
