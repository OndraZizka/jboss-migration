package cz.muni.fi.jboss.migration.migrators.security;

import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.actions.CliCommandAction;
import cz.muni.fi.jboss.migration.actions.CopyFileAction;
import cz.muni.fi.jboss.migration.conf.GlobalConfiguration;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.CopyException;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.migrators.security.jaxb.*;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Migrator of security subsystem implementing IMigrator
 * 
 * Example AS 5 config:
 * 
        <application-policy name="todo">
            <authentication>
                <login-module code="org.jboss.security.auth.spi.LdapLoginModule" flag="required">
                    <module-option name="password-stacking">useFirstPass</module-option>
                </login-module>
            </authentication>
        </application-policy>
 *
 * @author Roman Jakubco
 */
public class SecurityMigrator extends AbstractMigrator {
    private static final Logger log = LoggerFactory.getLogger(SecurityMigrator.class);

    private static final String AS7_CONFIG_DIR_PLACEHOLDER = "${jboss.server.config.dir}";

    
    // Files which must be copied into AS7
    private Set<String> fileNames = new HashSet();
    private Set<CopyFileAction> copyActions;


    @Override
    protected String getConfigPropertyModuleName() {
        return "security";
    }


    public SecurityMigrator(GlobalConfiguration globalConfig, MultiValueMap config) {
        super(globalConfig, config);
    }

    /**
     *  Loads the AS 5 data.
     */
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

    
    /**
     *  Creates the actions.
     */
    @Override
    public void createActions(MigrationContext ctx) throws MigrationException {
        
        // Config fragments
        for( IConfigFragment fragment : ctx.getMigrationData().get(SecurityMigrator.class).getConfigFragments()) {
            if( fragment instanceof ApplicationPolicyBean) {
                try {
                    SecurityDomainBean appPolicy = migrateAppPolicy( (ApplicationPolicyBean) fragment, ctx);
                    ctx.getActions().addAll( createSecurityDomainCliAction(appPolicy));
                } catch (CliScriptException e) {
                    throw new MigrationException("Migration of application-policy failed: " + e.getMessage(), e);
                }
                continue;
            }
            throw new MigrationException("Config fragment unrecognized by " + this.getClass().getSimpleName() + ": " + fragment);
        }

        // Files to copy
        File as5profileDir = getGlobalConfig().getAS5Config().getProfileDir();
        String as7Dir      = getGlobalConfig().getAS7Config().getDir();
        
        for( String fileName : this.fileNames ) {
            File src;
            try {
                // TODO: MIGR-54 The paths in AS 5 config relate to some base dir. Find out which and use that, instead of searching.
                //       Then, create the actions directly in the code creating this "files to copy" collection.
                src = Utils.searchForFile(fileName, as5profileDir).iterator().next();
            } catch( CopyException ex ) {
                //throw new ActionException("Failed copying a security file: " + ex.getMessage(), ex);
                // Some files referenced in security may not exist. (?)
                log.warn("Couldn't find file referenced in AS 5 security config: " + fileName);
                continue;
            }

            File target = Utils.createPath(as7Dir, "standalone", "configuration", src.getName());

            // Default value for overwrite => false
            ctx.getActions().add( new CopyFileAction( this.getClass(), src, target, false));
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
                loginModules.add( createLoginModule( lmAS5, this.copyActions ) );
            }
        }

        securityDomain.setLoginModules(loginModules);

        return securityDomain;
    }

    
    /**
     *  Migrates the given login module.
     */
    private LoginModuleAS7Bean createLoginModule(LoginModuleAS5Bean lmAS5, Collection<CopyFileAction> filesToCopy ) {
        LoginModuleAS7Bean lmAS7 = new LoginModuleAS7Bean();

        // Flag
        lmAS7.setLoginModuleFlag( lmAS5.getLoginModuleFlag() );
        
        // Code
        lmAS7.setLoginModuleCode( deriveLoginModuleName( lmAS5.getLoginModule() ) );

        // Module options
        Set<ModuleOptionAS7Bean> moduleOptions = new HashSet();
        lmAS7.setModuleOptions(moduleOptions);
        if( lmAS5.getModuleOptions() == null )
            return lmAS7;

        for( ModuleOptionAS5Bean moAS5 : lmAS5.getModuleOptions() ){
            String value;
            // Take care of specific module options.
            switch( moAS5.getModuleName() ){
                case "rolesProperties":
                case "usersProperties":
                    String fName = new File( moAS5.getModuleValue() ).getName();
                    value = AS7_CONFIG_DIR_PLACEHOLDER + "/" + fName;
                    this.fileNames.add(fName); // Add to the list of the files to copy.
                    // TODO: Rather directly create CopyActions.
                    // TODO: The paths in AS 5 config relate to some base dir. Find out which and use that, instead of searching.
                    /*filesToCopy.add( new CopyAction( 
                            new File( moAS5.getModuleValue()), 
                            new File( getGlobalConfig().getAS7Config().getConfigPath(), fName), false, false));*/
                    break;
                default:
                    value = moAS5.getModuleValue();
                    break;
            }
            ModuleOptionAS7Bean moAS7 = new ModuleOptionAS7Bean( moAS5.getModuleName(), value );
            moduleOptions.add( moAS7 );
        }
        return lmAS7;
    }

    /**
     *  AS 7 has few aliases for the distributed login modules.
     *  This methods translates them from AS 5.
     */
    private static String deriveLoginModuleName( String as5moduleName ) {
        
        String type = StringUtils.substringAfterLast(as5moduleName, ".");
        switch( type ) {
            case "ClientLoginModule": return "Client";
            case "BaseCertLoginModule": return "Certificate";
            case "CertRolesLoginModule":  return"CertificateRoles";
            case "DatabaseServerLoginModule": return "Database";
            case "DatabaseCertLoginModule": return "DatabaseCertificate";
            case "IdentityLoginModule": return "Identity";
            case "LdapLoginModule": return "Ldap";
            case "LdapExtLoginModule": return "LdapExtended";
            case "RoleMappingLoginModule": return "RoleMapping";
            case "RunAsLoginModule": return "RunAs";
            case "SimpleServerLoginModule": return "Simple";
            case "ConfiguredIdentityLoginModule": return "ConfiguredIdentity";
            case "SecureIdentityLoginModule": return "SecureIdentity";
            case "PropertiesUsersLoginModule": return "PropertiesUsers";
            case "SimpleUsersLoginModule": return "SimpleUsers";
            case "LdapUsersLoginModule": return "LdapUsers";
            case "Krb5loginModule": return "Kerberos";
            case "SPNEGOLoginModule": return "SPNEGOUsers";
            case "AdvancedLdapLoginModule": return "AdvancedLdap";
            case "AdvancedADLoginModule": return "AdvancedADldap";
            case "UsersRolesLoginModule": return "UsersRoles";
            default: return as5moduleName;
        }
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

        actions.add( new CliCommandAction( SecurityMigrator.class, createSecurityDomainScript(domain), domainCmd));

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

        return new CliCommandAction( SecurityMigrator.class, createLoginModuleScript(domain, module), request);
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
    
}// class
