package nl.rubensten.pp2lal2pp;

/**
 * @author Ruben Schellekens
 */
public class ParseException extends RuntimeException {

    public ParseException() {
        super();
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }

}
