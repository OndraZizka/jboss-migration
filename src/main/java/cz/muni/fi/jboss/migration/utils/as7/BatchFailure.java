package cz.muni.fi.jboss.migration.utils.as7;

/**
 *  Failure of CLI Batch. Contains the index of failed operation and the error message from that operation.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class BatchFailure {
    
    private final Integer index;
    private final String message;


    public BatchFailure( Integer index, String message ) {
        this.index = index;
        this.message = message;
    }
    

    public Integer getIndex() { return index; }
    public String getMessage() { return message; }
    
}
