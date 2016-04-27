package nl.rubensten.pp2lal2pp.lang;

/**
 * @author Ruben Schellekens
 */
public class Value {

    /**
     * No value.
     */
    public static final Value EMPTY = new Value("");

    private Object object;

    public Value(Object object) {
        this.object = object;
    }

    /**
     * @return The string representation of the value.
     */
    public String stringRepresentation() {
        return object.toString();
    }

}
