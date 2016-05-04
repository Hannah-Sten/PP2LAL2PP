package nl.rubensten.pp2lal2pp.lang;

import nl.rubensten.pp2lal2pp.Constants;
import nl.rubensten.pp2lal2pp.IDManager;

/**
 * @author Ruben Schellekens
 */
public class Variable implements Identifyable, Element {

    /**
     * The unique id of the variable.
     */
    private int id;

    /**
     * The offset from the stack pointer where the value of the variable is stored.
     */
    private int pointer;

    /**
     * The name of the variable.
     */
    private String name;

    /**
     * The default value of the variable.
     */
    private Value defaultValue;

    public Variable(String name) {
        this.id = IDManager.newId();
        this.name = name;
        this.defaultValue = Number.ZERO;
    }

    public Variable(String name, Value defaultValue) {
        this(name);
        this.defaultValue = defaultValue;
    }

    protected void setPointer(int pointer) {
        this.pointer = pointer;
    }

    public Value getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return The memory address where the value of the variable is stored.
     */
    public int getPointer() {
        return pointer;
    }

    /**
     * @return The name of the variable.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if this is a global variable.
     */
    public boolean isGlobal() {
        return this instanceof GlobalVariable;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Value getValue() {
        return new Value(Constants.REG_VARIABLE);
    }

    @Override
    public String toString() {
        if (isGlobal()) {
            return "(global " + name + "=" + defaultValue + " #" + id + " *" + pointer + ")";
        }
        else {
            return "(var " + name + "=" + defaultValue + " #" + id + " *" + pointer + ")";
        }
    }
}
