package nl.rubensten.pp2lal2pp.compiler;

import nl.rubensten.pp2lal2pp.Constants;
import nl.rubensten.pp2lal2pp.PP2LAL2PPException;
import nl.rubensten.pp2lal2pp.lang.*;
import nl.rubensten.pp2lal2pp.util.Regex;
import nl.rubensten.pp2lal2pp.util.Template;

import java.util.*;

/**
 * @author Ruben Schellekens
 */
public class LinearOperation implements Iterable<Operand> {

    /**
     * All the operands that have to be processed in order.
     */
    private List<Operand> operands = new ArrayList<>();

    /**
     * Tracks how much the stack pointer has decreased in order to get the pointers to local
     * variables right.
     */
    private int pointerOffset = 0;

    /**
     * The original operation string when made from an operation.
     */
    private String originalOperation = "";

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

        LinearOperation linop = new LinearOperation(operands);
        linop.originalOperation = operation.toHumanReadableString();
        return linop;
    }

    /**
     * Compiles the linear operation. The result of the operation is stored in R0.
     *
     * @param label
     *         The label to put in front of the first line.
     */
    public void compile(Compiler compiler, String label) {
        Program program = compiler.getInput();
        StringBuilder assembly = compiler.getAssembly();
        String comment = originalOperation;

        int count = 0;
        for (Operand op : this) {
            // Execute operation.
            if (op instanceof Operator) {
                Operator operator = (Operator)op;

                assembly.append(Template.fillStatement(label, "PULL", "R1", "", ">\n"));
                assembly.append(Template.fillStatement("", "PULL", "R0", "", ">\n"));
                String instr = operator.getInstruction().get();
                assembly.append(Template.fillStatement("", instr, "R0", "R1", "> \n"));
                pointerOffset -= 2;

                if (count != operands.size() - 1) {
                    assembly.append(Template.fillStatement("", "PUSH", "R0", "", ">\n"));
                    pointerOffset++;
                }
            }
            // Push operands to the stack.
            else {
                String location = op.getValueLocation();

                if (op instanceof FunctionCall) {
                    compiler.compileFunctionCall((FunctionCall)op, label);
                    label = "";
                }

                if (op instanceof Variable) {
                    Variable var = (Variable)op;
                    if (program.getGlobalVariable(var.getName()).isPresent()) {
                        location = Regex.replace("{$REGISTER}", location, Constants.REG_GLOBAL_BASE);
                        location = Regex.replace("{$POINTER}", location, var.getName());
                    }
                    else {
                        location = Regex.replace("{$REGISTER}", location, Constants.REG_STACK_POINTER);
                        location = Regex.replace("{$POINTER}", location, (var.getPointer() +
                            pointerOffset) + "");
                    }
                }

                assembly.append(Template.fillStatement(label, "LOAD", "R0", location, comment
                        + "\n"));
                assembly.append(Template.fillStatement(label, "PUSH", "R0", "", ">\n"));
                pointerOffset++;
            }

            label = "";
            comment = ">";
            count++;
        }
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
