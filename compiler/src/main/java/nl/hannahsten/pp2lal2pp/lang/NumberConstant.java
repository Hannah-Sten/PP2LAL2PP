package nl.hannahsten.pp2lal2pp.lang;

import nl.hannahsten.pp2lal2pp.PP2LAL2PPException;

/**
 * A number that is tied to a given (constant) name.
 *
 * @author Hannah Schellekens
 */
public class NumberConstant extends Number {

    /**
     * The name of the constant.
     */
    private final String name;

    public NumberConstant(String name) {
        super(-1);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String stringRepresentation() {
        return name;
    }

    @Override
    public int getIntValue() {
        throw new PP2LAL2PPException("cannot retrieve an int value from a numberconstant '" +
                name + "'");
    }

}
