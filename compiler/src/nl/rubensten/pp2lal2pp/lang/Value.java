package nl.rubensten.pp2lal2pp.lang;

/**
 * @author Ruben Schellekens
 */
public class Value implements Element {

    /**
     * No value.
     */
    public static final Value EMPTY = new Value("");

    private Object object;

    public Value(Object object) {
        this.object = object;
    }

    /**
     * Parses a value from a string representation.
     */
    public static Value parse(String string) {
        // Binary
        if (string.matches("0b[0-1]+")) {
            String meat = string.replace("0b", "");
            return new Number(Integer.parseInt(meat, 2));
        }
        // Hexadecimal
        else if (string.matches("0x[0-9a-fA-F]+")) {
            String meat = string.replace("0x", "");
            return new Number(Integer.parseInt(meat, 16));
        }

        try {
            return new Number(Integer.parseInt(string));
        }
        catch (NumberFormatException nfe) {
            return new Value(string);
        }
    }

    /**
     * @return The string representation of the value.
     */
    public String stringRepresentation() {
        return object.toString();
    }

    public Object getObject() {
        return object;
    }

    @Override
    public String toString() {
        return object.toString();
    }

    @Override
    public Value getValue() {
        return this;
    }

}
