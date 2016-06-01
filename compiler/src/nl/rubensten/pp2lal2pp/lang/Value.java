package nl.rubensten.pp2lal2pp.lang;

import nl.rubensten.pp2lal2pp.ParseException;
import nl.rubensten.pp2lal2pp.util.Regex;

/**
 * @author Ruben Schellekens
 */
public class Value implements Element, Comparable<Value> {

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
        // Character
        if (string.equals("'''")) {
            return new Number((int)'\'');
        }
        if (Regex.matches("'.+'", string)) {
            String meat = string.replace("'", "");
            char[] chars = meat.toCharArray();

            if (chars.length > 1 || chars.length == 0) {
                throw new ParseException("Invalid character " + string + ".");
            }

            return new Number((int)chars[0]);
        }
        // Binary
        if (Regex.matches("0b[0-1]+", string)) {
            String meat = string.replace("0b", "");
            return new Number(Integer.parseInt(meat, 2));
        }
        // Hexadecimal
        else if (Regex.matches("0x[0-9a-fA-F]+", string)) {
            String meat = string.replace("0x", "");
            return new Number(Integer.parseInt(meat, 16));
        }
        // Octal
        else if (Regex.matches("0[0-1]+", string)) {
            return new Number(Integer.parseInt(string, 8));
        }
        // Other base
        else if (Regex.matches("0_[0-9]+_[0-9]+", string)) {
            String[] split = Regex.split("_", string);
            String base = split[1];
            int baseInt = Integer.parseInt(base);
            return new Number(Integer.parseInt(Regex.replaceAll("^0+", split[2], ""), baseInt));
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

    @Override
    public int compareTo(Value o) {
        return stringRepresentation().compareTo(o.stringRepresentation());
    }

    @Override
    public boolean equals(Object obj) {
        return object.equals(obj);
    }
}
