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
     *
     * @param string
     *         The string to parse.
     * @param program
     *         The program-instance where the parsed value will be used.
     */
    public static Value parse(String string, Program program) {
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
        // Octal
        else if (string.matches("0[0-1]+")) {
            return new Number(Integer.parseInt(string, 8));
        }
        // Other base
        else if (string.matches("0_[0-9]+_[0-9]+")) {
            String base = string.split("_")[1];
            int baseInt = Integer.parseInt(base);
            return new Number(Integer.parseInt(string.split("_")[2].replaceAll("^0+", ""), baseInt));
        }

        try {
            return new Number(Integer.parseInt(string));
        }
        catch (NumberFormatException nfe) {
            if (program.getDefinitions().parallelStream().anyMatch(d -> d.getName().equals(string))) {
                return new NumberConstant(string);
            }
            else {
                return new Value(string);
            }
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
