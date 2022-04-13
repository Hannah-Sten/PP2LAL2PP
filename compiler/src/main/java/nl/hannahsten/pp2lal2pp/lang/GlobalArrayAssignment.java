package nl.hannahsten.pp2lal2pp.lang;

import java.util.Optional;

/**
 * Assigns a certain value to all elements in an array.
 *
 * @author Hannah Schellekens
 */
public class GlobalArrayAssignment extends Operation {

    public GlobalArrayAssignment(GlobalArray array, Element assignedValue) {
        super(array, Operator.ASSIGN, assignedValue);
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

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public String toString() {
        return "ArrayAssignment{array=" + getArray() +
                ", assignedValue=" + getAssignedValue() +
                '}';
    }
}
