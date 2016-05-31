package nl.rubensten.pp2lal2pp.compiler;

import nl.rubensten.pp2lal2pp.PP2LAL2PPException;
import nl.rubensten.pp2lal2pp.lang.Element;
import nl.rubensten.pp2lal2pp.lang.Operation;
import nl.rubensten.pp2lal2pp.lang.Operator;

import java.util.*;

/**
 * @author Ruben Schellekens
 */
public class LinearOperation implements Iterable<Operand> {

    /**
     * All the operands that have to be processed in order.
     */
    private List<Operand> operands = new ArrayList<>();

    public LinearOperation(Operand... operands) {
        this.operands.addAll(Arrays.asList(operands));
    }

    public LinearOperation(Collection<Operand> operands) {
        this.operands.addAll(operands);
    }

    /**
     * Converts a regular operation to a linear operation.
     * <p>
     * Assumed is that there is a second element and an operator and that all elements are {@link
     * Operand}s.
     *
     * @throws PP2LAL2PPException
     *         if one of the assumptions doesn't hold.
     */
    public static LinearOperation fromOperation(Operation operation) throws PP2LAL2PPException {
        List<Operand> operands = new ArrayList<>();

        // Fetch information from the operation.
        Element first = operation.getFirstElement();
        Element second;
        try {
            second = operation.getSecondElement().get();
        }
        catch (NoSuchElementException nsee) {
            throw new PP2LAL2PPException("there is no second element in operation " + operation);
        }
        Operator operator;
        try {
            operator = operation.getOperator().get();
        }
        catch (NoSuchElementException nsee) {
            throw new PP2LAL2PPException("there is no operator in operation " + operation);
        }

        // Handle the first element of the operation.
        if (first instanceof Operation) {
            Element firstReal = ((Operation)first).toRealElement();
            if (firstReal instanceof Operation) {
                operands.addAll(fromOperation((Operation)first).operands());
            }
            else if (firstReal instanceof Operand) {
                operands.add((Operand)firstReal);
            }
            else {
                throw new PP2LAL2PPException("the first operation element '" + first + "' is no " +
                        "Operand");
            }
        }
        else {
            if (first instanceof Operand) {
                operands.add((Operand)first);
            }
            else {
                throw new PP2LAL2PPException("the first operation element '" + first + "' is no " +
                        "Operand");
            }
        }

        // Handle the second element of the operation.
        if (second instanceof Operation) {
            Element secondReal = ((Operation)second).toRealElement();
            if (secondReal instanceof Operation) {
                operands.addAll(fromOperation((Operation)second).operands());
            }
            else if (secondReal instanceof Operand) {
                operands.add((Operand)secondReal);
            }
            else {
                throw new PP2LAL2PPException("the first operation element '" + first + "' is no " +
                        "Operand");
            }
        }
        else {
            if (second instanceof Operand) {
                operands.add((Operand)second);
            }
            else {
                throw new PP2LAL2PPException("the second operation element '" + first + "' is no " +
                        "Operand");
            }
        }

        operands.add(operator);

        return new LinearOperation(operands);
    }

    public List<Operand> operands() {
        return new ArrayList<>(operands);
    }

    @Override
    public Iterator<Operand> iterator() {
        return operands.listIterator();
    }

    @Override
    public String toString() {
        return "LinearOperation:" + operands.toString();
    }
}
