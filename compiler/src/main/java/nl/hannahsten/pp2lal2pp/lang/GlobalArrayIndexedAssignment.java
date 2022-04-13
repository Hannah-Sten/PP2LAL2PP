package nl.hannahsten.pp2lal2pp.lang;

import java.util.Optional;

/**
 * Assigns a certain value to a certain index in an array.
 *
 * @author Hannah Schellekens
 */
public class GlobalArrayIndexedAssignment extends Operation {

    /**
     * Representation of the index.
     */
    private final ArrayAccess access;

    public GlobalArrayIndexedAssignment(GlobalArray array, ArrayAccess access, Element assignedValue) {
        super(array, Operator.ASSIGN, assignedValue);
        this.access = access;
    }

    public GlobalArray getArray() {
        return (GlobalArray)getFirstElement();
    }

    public Value getAssignedValue() {
        Optional<Element> second = getSecondElement();
        if (!second.isPresent()) {
            throw new IllegalStateException("Second element cannot be null");
        }
        return (Value)second.get();
    }

    public ArrayAccess getAccess() {
        return access;
    }

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public String toString() {
        return "ArrayAssignment[" + access.toString() + "]{array=" + getArray() +
                ", assignedValue=" + getAssignedValue() +
                '}';
    }
}
