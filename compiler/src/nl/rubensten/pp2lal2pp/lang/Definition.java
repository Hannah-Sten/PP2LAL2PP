package nl.rubensten.pp2lal2pp.lang;

import java.util.Optional;

/**
 * @author Ruben Schellekens
 */
public class Definition implements Element {

    /**
     * The name of the definition.
     */
    private String name;

    /**
     * The value of the definition.
     */
    private Value value;

    /**
     * The documentation of the definition.
     */
    private String docString;

    /**
     * @param docString <code>null</code> for no docstring.
     */
    public Definition(String name, Value value, String docString) {
        this.name = name;
        this.value = value;
        this.docString = docString;
    }

    /**
     * Get the documentation of the definition.
     */
    public Optional<String> getDocString() {
        if (docString == null) {
            return Optional.empty();
        }

        return Optional.of(docString);
    }

    public String getName() {
        return name;
    }

    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{" + name + " := " + value + "}";
    }
}
