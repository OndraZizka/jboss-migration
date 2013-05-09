package org.jboss.loom.migrators.classloading;

import java.io.File;
import org.apache.commons.collections.map.MultiValueMap;
import org.jboss.loom.MigrationContext;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.LoadMigrationException;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.migrators.classloading.beans.AppModuleClassloadingConfig;
import org.jboss.loom.migrators.classloading.beans.JBossClassloadingXml;
import org.jboss.loom.migrators.classloading.beans.JBossWebXml;
import org.jboss.loom.spi.IMigrator;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ClassloadingMigrator extends AbstractMigrator implements IMigrator {


    @Override protected String getConfigPropertyModuleName() { return "classloading"; }


    public ClassloadingMigrator( GlobalConfiguration globalConfig, MultiValueMap config ) {
        super( globalConfig, config );
    }
    


    @Override
    public void loadAS5Data( MigrationContext ctx ) throws LoadMigrationException {
        
        ClassloadingConfData classloadingConfData = new ClassloadingConfData();
        
        // For each app, extract classloading conf data.
        for( String appPath : getGlobalConfig().getAppPaths() ){
            File app = new File(appPath);
            AppModuleClassloadingConfig modCLConf = extractClassloadingConfData( app );
            classloadingConfData.getAppClassloadingConfigs().put(app, modCLConf );
        }
        
        ctx.getMigrationData().put( ClassloadingMigrator.class, classloadingConfData );
    }


    @Override
    public void createActions( MigrationContext ctx ) throws MigrationException {
        
    }


    /**
     *  Extracts classloading config from given Java EE archive.
     * @param app
     * @return 
     */
    private AppModuleClassloadingConfig extractClassloadingConfData( File app ) {
        
        AppModuleClassloadingConfig appCLConf = new AppModuleClassloadingConfig();
        
        JBossClassloadingXml clXml = null;
        JBossWebXml          webXml = null;
        
        appCLConf.setJbossClassloadingConf( clXml );
        appCLConf.setJbossWebConf( webXml );
        
        return appCLConf;
    }
    
    
}// class
