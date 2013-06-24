package org.jboss.loom.ex;

import java.util.LinkedList;
import java.util.List;

/**
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class MigrationExceptions extends MigrationException {

    protected List<Exception> exs = new LinkedList();


    public MigrationExceptions( String message, List<Exception> list ) {
        super( message );
        this.exs = list;
    }


    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder( super.getMessage() );
        for( Exception ex : exs ) {
            sb.append("\n  ").append( ex.getClass().getSimpleName() ).append(": ").append( ex.getMessage() );
        }
        return sb.toString();
    }

    public List<Exception> getExs() {
        return exs;
    }

    

    /**
     *  Shorthand method to handle one or multiple exceptions, if they occur, at the end of a method.
     */
    public static void wrapExceptions( List<Exception> problems, String msg ) throws MigrationException {
        if( !problems.isEmpty() ) {
            if( problems.size() == 1 ) {
                throw new MigrationException( msg, problems.get( 0 ) );
            } else {
                throw new MigrationExceptions( msg, problems );
            }
        }
    }

    
}// class
