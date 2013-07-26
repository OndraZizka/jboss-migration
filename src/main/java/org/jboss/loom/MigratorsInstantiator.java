package org.jboss.loom;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jboss.loom.actions.review.BeansXmlReview;
import org.jboss.loom.actions.review.IActionReview;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.InitMigratorsExceptions;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.ex.MigrationExceptions;
import org.jboss.loom.migrators.IMigratorFilter;
import org.jboss.loom.migrators._ext.MigratorDefinition;
import org.jboss.loom.migrators.classloading.ClassloadingMigrator;
import org.jboss.loom.migrators.connectionFactories.ResAdapterMigrator;
import org.jboss.loom.migrators.dataSources.DatasourceMigrator;
import org.jboss.loom.migrators.ejb3.Ejb3Migrator;
import org.jboss.loom.migrators.jaxr.JaxrMigrator;
import org.jboss.loom.migrators.logging.LoggingMigrator;
import org.jboss.loom.migrators.mail.MailMigrator;
import org.jboss.loom.migrators.messaging.MessagingMigrator;
import org.jboss.loom.migrators.remoting.RemotingMigrator;
import org.jboss.loom.migrators.security.SecurityMigrator;
import org.jboss.loom.migrators.server.ServerMigrator;
import org.jboss.loom.spi.IMigrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class MigratorsInstantiator {
    private static final Logger log = LoggerFactory.getLogger( MigratorsInstantiator.class );


    /**
     *  Finds the static java Migrator classes, filters them according to the config, and returns instantiated migrators.
     */
    static Map<Class<? extends IMigrator>, IMigrator> findAndInstantiateStaticMigratorClasses( IMigratorFilter filter, Configuration config ) 
            throws InitMigratorsExceptions, MigrationException 
    {
        // Find IMigrator implementations.
        List<Class<? extends IMigrator>> migratorClasses = MigratorsInstantiator.findMigratorClasses();
        
        
        // Filter based on $config.
        /*List<String> onlyMigrators = config.getGlobal().getOnlyMigrators();
        for( Iterator<Class<? extends IMigrator>> it = migratorClasses.iterator(); it.hasNext(); ) {
            Class<? extends IMigrator> cls = it.next();
            for( String name : onlyMigrators ) {
                if( ! cls.getSimpleName().equals( name ) )
                    it.remove();
            }
        }*/
        
        // Filter out based on given filter.
        for( Iterator<Class<? extends IMigrator>> it = migratorClasses.iterator(); it.hasNext(); ) {
            if( ! filter.filterClass( it.next() ) )
                it.remove();
        }
        
        
        
        // Initialize migrator instances.
        Map<Class<? extends IMigrator>, IMigrator> migratorsMap = createJavaMigrators( migratorClasses, config.getGlobal() );
        return migratorsMap;
    }

    
    
    /**
     *  Instantiate the plugins.
     */
    static Map<Class<? extends IMigrator>, IMigrator> createJavaMigrators(
            List<Class<? extends IMigrator>> migratorClasses,
            GlobalConfiguration globalConfig)
                throws InitMigratorsExceptions, MigrationException 
    {
        
        Map<Class<? extends IMigrator>, IMigrator> migs = new LinkedHashMap();
        List<Exception> exs  = new LinkedList<>();
        
        for( Class<? extends IMigrator> cls : migratorClasses ){
            try {
                Constructor<? extends IMigrator> ctor = cls.getConstructor(GlobalConfiguration.class);
                IMigrator mig = ctor.newInstance(globalConfig);
                migs.put(cls, mig);
            }
            catch( NoSuchMethodException ex ){
                String msg = cls.getName() + " doesn't have constructor ...(GlobalConfiguration globalConfig).";
                log.error( msg );
                exs.add( new MigrationException(msg) );
            }
            catch( InvocationTargetException | InstantiationException | IllegalAccessException ex) {
                log.error("Failed instantiating " + cls.getSimpleName() + ": " + ex.toString());
                log.debug("Stack trace: ", ex);
                exs.add(ex);
            }
        }
        
        MigrationExceptions.wrapExceptions( exs, "Failed processing migrator definitions. ");
        
        return migs;
    }// createMigrators()
    
    
    
    

    /**
     *  Finds the implementations of the IMigrator.
     *  TODO: Implement scanning for classes.
     */
    static List<Class<? extends IMigrator>> findMigratorClasses() {
        
        LinkedList<Class<? extends IMigrator>> migratorClasses = new LinkedList();
        findStaticMigratorClasses( migratorClasses );
        //findExternalMigratorClasses( migratorClasses );
        return migratorClasses;
    }
    
    
    static void findStaticMigratorClasses( LinkedList<Class<? extends IMigrator>> migratorClasses ) {
        migratorClasses.add( SecurityMigrator.class );
        migratorClasses.add( ServerMigrator.class );
        migratorClasses.add( DatasourceMigrator.class );
        migratorClasses.add( ResAdapterMigrator.class );
        migratorClasses.add( LoggingMigrator.class );
        //migratorClasses.add( DeploymentScannerMigrator.class );
        migratorClasses.add( ClassloadingMigrator.class );  // Warn-only impl.
        migratorClasses.add( MailMigrator.class );          // Warn-only impl.
        migratorClasses.add( JaxrMigrator.class );          // Warn-only impl.
        migratorClasses.add( RemotingMigrator.class );      // Warn-only impl.
        migratorClasses.add( Ejb3Migrator.class );          // Warn-only impl.
        migratorClasses.add( MessagingMigrator.class );     // Warn-only impl.
    }

    
    
    static List<Class<? extends IActionReview>> findActionReviewers(){
        LinkedList<Class<? extends IActionReview>> reviewers = new LinkedList();
        reviewers.add( BeansXmlReview.class );
        return reviewers;
    }
    

}// class
