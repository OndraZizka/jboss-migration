/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.security;

import org.apache.commons.lang.StringUtils;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.loom.utils.as7.CliAddScriptBuilder;
import org.jboss.loom.utils.as7.CliApiCommandBuilder;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ctx.MigratorData;
import org.jboss.loom.actions.CliCommandAction;
import org.jboss.loom.actions.CopyFileAction;
import org.jboss.loom.actions.ModuleCreationAction;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.CliScriptException;
import org.jboss.loom.ex.CopyException;
import org.jboss.loom.ex.LoadMigrationException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.migrators.security.jaxb.*;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.jboss.loom.spi.ann.ConfigPartDescriptor;
import org.jboss.loom.utils.UtilsAS5;

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
@ConfigPartDescriptor(
    name = "Security (JAAS) configuration",
    docLink = "https://access.redhat.com/site/documentation//en-US/JBoss_Enterprise_Application_Platform/5/html/Security_Guide/index.html"
)
public class SecurityMigrator extends AbstractMigrator {
    private static final Logger log = LoggerFactory.getLogger(SecurityMigrator.class);

    private static final String AS7_CONFIG_DIR_PLACEHOLDER = "${jboss.server.config.dir}";
    
    
    // Files which must be copied into AS7

    //private Set<CopyFileAction> copyActions; // not used, TODO


    @Override
    protected String getConfigPropertyModuleName() {
        return "security";
    }


    public SecurityMigrator(GlobalConfiguration globalConfig) {
        super(globalConfig);
    }

    /**
     *  Loads the AS 5 data.
     */
    @Override
    public void loadSourceServerConfig(MigrationContext ctx) throws LoadMigrationException {
        try {
            File file = new File(getGlobalConfig().getAS5Config().getConfDir(), "login-config.xml");
            if (!file.canRead()) {
                throw new LoadMigrationException("Can't read: " + file.getAbsolutePath());
            }

            Unmarshaller unmarshaller = JAXBContext.newInstance(SecurityAS5Bean.class).createUnmarshaller();
            SecurityAS5Bean securityAS5 = (SecurityAS5Bean) unmarshaller.unmarshal(file);

            MigratorData mData = new MigratorData();
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
        SecurityMigResource resource = new SecurityMigResource();

        // Config fragments
        for( IConfigFragment fragment : ctx.getMigrationData().get(SecurityMigrator.class).getConfigFragments()) {
            if( fragment instanceof ApplicationPolicyBean) {
                try {
                    SecurityDomainBean appPolicy = migrateAppPolicy( (ApplicationPolicyBean) fragment, ctx, resource);
                    ctx.getActions().addAll( createSecurityDomainCliAction(appPolicy));
                } catch (CliScriptException e) {
                    throw new MigrationException("Migration of application-policy failed: " + e.getMessage(), e);
                }
                continue;
            }
            throw new MigrationException("Config fragment unrecognized by " + this.getClass().getSimpleName() + ": " + fragment);
        }

    }

    /**
     * Migrates application-policy from AS5 to AS7
     *
     * @param appPolicy object representing application-policy
     * @param ctx       migration context
     * @return created security-domain
     */
    public SecurityDomainBean migrateAppPolicy(ApplicationPolicyBean appPolicy, MigrationContext ctx,
                                               SecurityMigResource resource) throws MigrationException{
        Set<LoginModuleAS7Bean> loginModules = new HashSet();
        SecurityDomainBean securityDomain = new SecurityDomainBean();

        securityDomain.setSecurityDomainName(appPolicy.getApplicationPolicyName());
        securityDomain.setCacheType("default");
        if (appPolicy.getLoginModules() != null) {
            for (LoginModuleAS5Bean lmAS5 : appPolicy.getLoginModules()) {
                loginModules.add( createLoginModule( lmAS5, resource, ctx ) );
            }
        }

        securityDomain.setLoginModules(loginModules);

        return securityDomain;
    }

    
    /**
     *  Migrates the given login module.
     */
    private LoginModuleAS7Bean createLoginModule(LoginModuleAS5Bean lmAS5, SecurityMigResource resource, MigrationContext ctx )
            throws MigrationException{
        LoginModuleAS7Bean lmAS7 = new LoginModuleAS7Bean();

        // Flag
        lmAS7.setLoginModuleFlag( lmAS5.getLoginModuleFlag() );
        
        // Code
        String lmName = deriveLoginModuleName( lmAS5.getLoginModule() );
        lmAS7.setLoginModuleCode( lmName );
        if( lmName.equals( lmAS5.getLoginModule() ) ){
            ModuleCreationAction action = createModuleActionForLogMod(lmAS7, lmName, resource);
            if(action != null) ctx.getActions().add( action );
        }

        // Module options
        Set<ModuleOptionAS7Bean> moduleOptions = new HashSet();

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
                    if(resource.getFileNames().add(fName)){
                        CopyFileAction action = createCopyActionForFile(resource, fName);
                        if( action != null) ctx.getActions().add( action );
                    }
                    break;
                default:
                    value = moAS5.getModuleValue();
                    break;
            }
            ModuleOptionAS7Bean moAS7 = new ModuleOptionAS7Bean( moAS5.getModuleName(), value );
            moduleOptions.add( moAS7 );
        }
        lmAS7.setModuleOptions(moduleOptions);

        return lmAS7;
    }

    /**
     * Creates CopyFileAction for File referenced in migrated Module-Options
     *
     * @param resource helping class containing all resources of the SecurityMigrator
     * @param fileName  file, which should be copied into AS7
     * @return  If the file is already set for copying then null else the created CopyFileAction
     */
    private  CopyFileAction createCopyActionForFile(SecurityMigResource resource, String fileName ) {

        if( ! resource.getFileNames().add(fileName) ) return null;

        File src;
        try {
            // TODO: MIGR-54 The paths in AS 5 config relate to some base dir. Find out which and use that, instead of searching.
            //       Then, create the actions directly in the code creating this "files to copy" collection.
            src = Utils.searchForFile(fileName, getGlobalConfig().getAS5Config().getProfileDir()).iterator().next();
        } catch( CopyException ex ) {
            //throw new ActionException("Failed copying a security file: " + ex.getMessage(), ex);
            // Some files referenced in security may not exist. (?)
            log.warn("Couldn't find file referenced in AS 5 security config: " + fileName);
            return null;
        }

        File target = Utils.createPath(getGlobalConfig().getAS7Config().getConfigDir(), src.getName());
        CopyFileAction action = new CopyFileAction( this.getClass(), src, target, CopyFileAction.IfExists.WARN );

        return action;
    }

    /**
     * Creates ModuleCreationAction for the custom made class for the Login-Module, which should be deployed as module
     *
     * @param lmAS7  Login-Module containing this class
     * @param className custom made class, which should be deployed into AS7
     * @param resource helping class containing all resources of the SecurityMigrator
     * @return  null if the JAR file containing the given class is already set for the creation of the module else
     *          created ModuleCreationAction.
     * @throws MigrationException
     */
    private ModuleCreationAction createModuleActionForLogMod(LoginModuleAS7Bean lmAS7, String className,
                                                                    SecurityMigResource resource)
            throws MigrationException{
        File fileJar;
        try {
            fileJar = UtilsAS5.findJarFileWithClass(className, getGlobalConfig().getAS5Config().getDir(),
                    getGlobalConfig().getAS5Config().getProfileName());
        } catch (IOException ex) {
            throw new MigrationException("Failed finding jar with class " + className + ": " + ex.getMessage(), ex);
        }

        if ( resource.getModules().containsKey(fileJar) ) {
            lmAS7.setModule( resource.getModules().get(fileJar) );
            return null;
        }

        String moduleName = "security.loginModule" + resource.getIncrement();

        // Handler jar is new => create ModuleCreationAction, new module and CLI script
        lmAS7.setModule( moduleName );
        resource.getModules().put(fileJar, moduleName);

        // TODO: Dependencies are little unknown. This two are the best possibilities from example of custom login module
        String[] deps = new String[]{"javax.api", "org.picketbox", null}; // null = next is optional.

        ModuleCreationAction moduleAction = new ModuleCreationAction( this.getClass(), moduleName, deps, fileJar, Configuration.IfExists.OVERWRITE);

        return moduleAction;
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
    public List<CliCommandAction> createSecurityDomainCliAction(SecurityDomainBean domain)
            throws CliScriptException {
        String errMsg = " in security-domain must be set.";
        Utils.throwIfBlank(domain.getSecurityDomainName(), errMsg, "Security name");

        List<CliCommandAction> actions = new LinkedList();
        
        // CLI ADD command
        ModelNode domainCmd = new ModelNode();
        domainCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        domainCmd.get(ClientConstants.OP_ADDR).add("subsystem", "security");
        domainCmd.get(ClientConstants.OP_ADDR).add("security-domain", domain.getSecurityDomainName());
        // Action
        CliCommandAction action = new CliCommandAction( SecurityMigrator.class, createSecurityDomainScript(domain), domainCmd);
        action.setIfExists( this.getIfExists() );
        actions.add( action );

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
        builder.addPropertyIfSet("flag", module.getLoginModuleFlag());
        builder.addPropertyIfSet("code", module.getLoginModuleCode());

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

        resultScript.append(builder.formatAndClearProps()).append(")");

        return resultScript.toString();
    }

    /**
     * Creates a CLI script for adding a Login-Module of the specific Security-Domain
     *
     * @param domain Security-Domain containing Login-Module
     * @param module Login-Module
     * @return created string containing the CLI script for adding the Login-Module
     * 
     * TODO: Rewrite using ModuleNode.
     */
    private static String createLoginModuleScript(SecurityDomainBean domain, LoginModuleAS7Bean module) {
        StringBuilder resultScript = new StringBuilder("/subsystem=security/security-domain=" +
                domain.getSecurityDomainName());
        resultScript.append("/authentication=classic:add(login-modules=[{");

        if ((module.getLoginModuleCode() != null) && !(module.getLoginModuleCode().isEmpty())) {
            resultScript.append("\"code\"=>\"").append(module.getLoginModuleCode()).append("\"");
        }
        if ((module.getLoginModuleFlag() != null) && !(module.getLoginModuleFlag().isEmpty())) {
            resultScript.append(", \"flag\"=>\"").append(module.getLoginModuleFlag()).append("\"");
        }

        if ((module.getModuleOptions() != null) && !(module.getModuleOptions().isEmpty())) {
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
