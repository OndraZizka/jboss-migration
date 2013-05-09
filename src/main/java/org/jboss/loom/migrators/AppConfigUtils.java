package org.jboss.loom.migrators;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class AppConfigUtils {
    
    public enum AppType {
        
        EAR("META-INF"), WAR("META-INF"), JAR("META-INF");
        
        private final String infDir;


        private AppType( String infDir ) {
            this.infDir = infDir;
        }
        
        
        public static AppType from( String str ){
            try {
                return valueOf( str.toUpperCase() );
            } catch (IllegalArgumentException ex ){
                return null;
            }
        }
    }// enum AppType
    
    
}// class
