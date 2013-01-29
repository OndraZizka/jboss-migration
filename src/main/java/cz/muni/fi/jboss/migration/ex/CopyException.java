package cz.muni.fi.jboss.migration.ex;

/**
 * Exception representing error in copying required files from AS5 to AS7
 *
 * @author Roman Jakubco
 *         Date: 1/28/13
 *         Time: 7:11 PM
 */
public class CopyException extends Exception {
    public CopyException(String message) {
        super(message);
    }

    public CopyException(String message, Throwable cause) {
        super(message, cause);
    }

    public CopyException(Throwable cause) {
        super(cause);
    }
}
