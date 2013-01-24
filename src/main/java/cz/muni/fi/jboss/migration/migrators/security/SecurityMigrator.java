package cz.muni.fi.jboss.migration.migrators.security;

import cz.muni.fi.jboss.migration.*;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.MigrationException;
import cz.muni.fi.jboss.migration.spi.IConfigFragment;
import cz.muni.fi.jboss.migration.spi.IMigrator;
import javafx.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
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

            File file = new File(globalConfig.getDirAS5() + File.separator + "conf" + File.separator + "login-config.xml");

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
    public void apply(MigrationContext ctx) {

    }

    @Override
    public List<Node> generateDomElements(MigrationContext ctx) {
        return null;
    }

    @Override
    public List<String> generateCliScripts(MigrationContext ctx) {
        return null;
    }

//    public void migrate(MigrationContext ctx) throws MigrationException{
//        SecurityAS7 securityAS7 = new SecurityAS7();
//        Set<SecurityDomain> securityDomains = new HashSet();
//
//        for (IConfigFragment data : ctx.getMigrationData().get(SecurityMigrator.class).getConfigFragment()) {
//            if(!(data instanceof ApplicationPolicy)){
//                throw new MigrationException("Error: Object is not part of Security migration!");
//            }
//            ApplicationPolicy appPolicy = (ApplicationPolicy) data;
//
//            Set<LoginModuleAS7> loginModules = new HashSet();
//            SecurityDomain securityDomain = new SecurityDomain();
//            securityDomain.setSecurityDomainName(appPolicy.getApplicationPolicyName());
//            securityDomain.setCacheType("default");
//
//            for (LoginModuleAS5 lmAS5 : appPolicy.getLoginModules()) {
//                Set<ModuleOptionAS7> moduleOptions = new HashSet();
//                LoginModuleAS7 lmAS7 = new LoginModuleAS7();
//                lmAS7.setLoginModuleFlag(lmAS5.getLoginModuleFlag());
//
//                switch (StringUtils.substringAfterLast(lmAS5.getLoginModule(), ".")) {
//                    case "ClientLoginModule":
//                        lmAS7.setLoginModuleCode("Client");
//                        break;
//                    //*
//                    case "BaseCertLoginModule":
//                        lmAS7.setLoginModuleCode("Certificate");
//                        break;
//                    case "CertRolesLoginModule":
//                        lmAS7.setLoginModuleCode("CertificateRoles");
//                        break;
//                    //*
//                    case "DatabaseServerLoginModule":
//                        lmAS7.setLoginModuleCode("Database");
//                        break;
//                    case "DatabaseCertLoginModule":
//                        lmAS7.setLoginModuleCode("DatabaseCertificate");
//                        break;
//                    case "IdentityLoginModule":
//                        lmAS7.setLoginModuleCode("Identity");
//                        break;
//                    case "LdapLoginModule":
//                        lmAS7.setLoginModuleCode("Ldap");
//                        break;
//                    case "LdapExtLoginModule":
//                        lmAS7.setLoginModuleCode("LdapExtended");
//                        break;
//                    case "RoleMappingLoginModule":
//                        lmAS7.setLoginModuleCode("RoleMapping");
//                        break;
//                    case "RunAsLoginModule":
//                        lmAS7.setLoginModuleCode("RunAs");
//                        break;
//                    case "SimpleServerLoginModule":
//                        lmAS7.setLoginModuleCode("Simple");
//                        break;
//                    case "ConfiguredIdentityLoginModule":
//                        lmAS7.setLoginModuleCode("ConfiguredIdentity");
//                        break;
//                    case "SecureIdentityLoginModule":
//                        lmAS7.setLoginModuleCode("SecureIdentity");
//                        break;
//                    case "PropertiesUsersLoginModule":
//                        lmAS7.setLoginModuleCode("PropertiesUsers");
//                        break;
//                    case "SimpleUsersLoginModule":
//                        lmAS7.setLoginModuleCode("SimpleUsers");
//                        break;
//                    case "LdapUsersLoginModule":
//                        lmAS7.setLoginModuleCode("LdapUsers");
//                        break;
//                    case "Krb5loginModule":
//                        lmAS7.setLoginModuleCode("Kerberos");
//                        break;
//                    case "SPNEGOLoginModule":
//                        lmAS7.setLoginModuleCode("SPNEGOUsers");
//                        break;
//                    case "AdvancedLdapLoginModule":
//                        lmAS7.setLoginModuleCode("AdvancedLdap");
//                        break;
//                    case "AdvancedADLoginModule":
//                        lmAS7.setLoginModuleCode("AdvancedADldap");
//                        break;
//                    case "UsersRolesLoginModule":
//                        lmAS7.setLoginModuleCode("UsersRoles");
//                        break;
//                    default:
//                        lmAS7.setLoginModuleCode(lmAS5.getLoginModule());
//                }
//
//                if (lmAS5.getModuleOptions() != null) {
//                    for (ModuleOptionAS5 moAS5 : lmAS5.getModuleOptions()) {
//                        ModuleOptionAS7 moAS7 = new ModuleOptionAS7();
//                        moAS7.setModuleOptionName(moAS5.getModuleName());
//
//                        // TODO: Module-option using file can only use .properties?
//                        if(moAS5.getModuleValue().contains("properties")){
//                            String value;
//                            if(moAS5.getModuleValue().contains("/")){
//                                value = StringUtils.substringAfterLast(moAS5.getModuleValue(), "/");
//                            } else{
//                                value = moAS5.getModuleValue();
//                            }
//                            moAS7.setModuleOptionValue("${jboss.server.config.dir}/" + value);
//
//                            CopyMemory cp = new CopyMemory();
//                            cp.setName(value);
//                            cp.setType("security");
//                            ctx.getCopyMemories().add(cp);
//                        } else{
//                            moAS7.setModuleOptionValue(moAS5.getModuleValue());
//                        }
//
//                        moduleOptions.add(moAS7);
//                    }
//                }
//
//                lmAS7.setModuleOptions(moduleOptions);
//                loginModules.add(lmAS7);
//            }
//
//            securityDomain.setLoginModules(loginModules);
//            migratedData.getMigratedData().add(securityDomain);
//        }
//
//        ctx.getMigratedData().put(SecurityMigrator.class, migratedData);
//    }
}
