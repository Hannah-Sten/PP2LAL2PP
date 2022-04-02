package nl.hannahsten.pp2lal2pp.lang;

import nl.hannahsten.pp2lal2pp.IDManager;

/**
 * @author Hannah Schellekens
 */
public class Return implements Element, Identifyable {

    /**
     * The unique ID of the return statement.
     */
    private final int id;

    /**
     * The value that must be returned.
     */
    private Value returnValue;

    public Return() {
        this.id = IDManager.newId();
        this.returnValue = null;
    }

    public Return(Value returnValue) {
        this();
        this.returnValue = returnValue;
    }

    public Value getReturnValue() {
        return returnValue;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Value getValue() {
        return Value.EMPTY;
    }

    @Override
    public String toString() {
        return "{return " + returnValue + "}";
    }
}
