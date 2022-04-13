package nl.hannahsten.pp2lal2pp.lang;

import nl.hannahsten.pp2lal2pp.IDManager;

/**
 * Either array access index, or array size definition.
 *
 * @author Hannah Schellekens
 */
public class ArrayAccess implements Element, Identifyable {

    /**
     * The unique id of the Index.
     */
    private final int id;

    /**
     * Numerical/constant, `null` when it is accessed by a variable.
     */
    private final Value accessingIndex;

    /**
     * The variable that holds the index, or `null` when the index is a fixed value
     */
    private final Variable accessingVariable;

    public static ArrayAccess newValueIndex(Value accessingIndex) {
        return new ArrayAccess(accessingIndex, null);
    }

    public static ArrayAccess newVariableIndex(Variable variableIndex) {
        return new ArrayAccess(null, variableIndex);
    }

    private ArrayAccess(Value accessingIndex, Variable accessingVariable) {
        this.id = IDManager.newId();
        this.accessingIndex = accessingIndex;
        this.accessingVariable = accessingVariable;
    }

    public Value getAccessingIndex() {
        return accessingIndex;
    }

    public Variable getAccessingVariable() {
        return accessingVariable;
    }

    public String getAccessorString() {
        if (accessingIndex != null) {
            return accessingIndex.stringRepresentation();
        }
        else {
            return accessingVariable.getName();
        }
    }

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Index{" + "id=" + id +
                ", accessingIndex=" + accessingIndex +
                ", accessingVariable=" + accessingVariable +
                '}';
    }
}
