package cz.muni.fi.jboss.migration.ex;

import java.util.List;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 * 
 * Generic class can't extend Throwable.
 */
public class InitMigratorsExceptions extends MigrationException {
    
    List<Exception> causes;

    public InitMigratorsExceptions(List<Exception> causes) {
        super("Multiple causes");
        this.causes = causes;
    }
    
    public InitMigratorsExceptions( String message, List<Exception> causes) {
        super(message);
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        sb.append("{ \n");
        for( Exception cause : causes ){
            sb.append(cause.toString()).append('\n');
        }
        sb.append("}");
        return sb.toString();
    }
    
    
}// class
