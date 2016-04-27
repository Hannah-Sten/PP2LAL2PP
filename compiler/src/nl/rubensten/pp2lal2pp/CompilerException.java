package nl.rubensten.pp2lal2pp;

/**
 * @author Ruben Schellekens
 */
public class CompilerException extends RuntimeException {

    public CompilerException() {
        super();
    }

    public CompilerException(String message) {
        super(message);
    }

    public CompilerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompilerException(Throwable cause) {
        super(cause);
    }

}
