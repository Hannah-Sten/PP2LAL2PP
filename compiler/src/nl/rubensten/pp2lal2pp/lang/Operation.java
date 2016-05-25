package nl.rubensten.pp2lal2pp.lang;

import java.util.Optional;

/**
 * A statement that looks like:
 * <p>
 * [firstElement] [operator] [secondElement]
 *
 * @author Ruben Schellekens
 */
public class Operation implements Element {

    /**
     * The statement left of the operator.
     */
    private Element firstElement;

    /**
     * The operator to execute on both statements.
     */
    private Operator operator;

    /**
     * The statement to the right of the operator.
     */
    private Element secondElement;

    /**
     * The value represented by the statement.
     */
    private Value value;

    public Operation(Element first, Operator operator, Element second) {
        this.firstElement = first;
        this.operator = operator;
        this.secondElement = second;

        if (operator == null) {
            return;
        }

        if (operator.getType() != Operator.OperatorType.ARITHMETIC &&
                operator.getType() != Operator.OperatorType.BITWISE) {
            return;
        }

        boolean firstNum = first instanceof Number;
        boolean secondNum = second instanceof Number;
        boolean firstOp = first instanceof Operation;
        boolean secondOp = second instanceof Operation;

        // Autocalculate if there are two numbers.
        if (firstNum && secondNum) {
            setNumber(operator.calculateNumbers((Number)first, (Number)second));
        }
        else if (firstNum && secondOp) {
            Operation op2 = (Operation)second;
            if (!op2.getSecondElement().isPresent()) {
                setNumber(op2.toNumber().get());
            }
        }
        else if (secondNum && firstOp) {
            Operation op1 = (Operation)first;
            if (!op1.getSecondElement().isPresent()) {
                setNumber(op1.toNumber().get());
            }
        }
        else if (firstOp && secondOp) {
            Operation op1 = (Operation)first;
            Operation op2 = (Operation)second;

            if (!op1.getSecondElement().isPresent() && !op2.getSecondElement().isPresent()) {
                Number num1 = op1.toNumber().get();
                Number num2 = op2.toNumber().get();
                setNumber(operator.calculateNumbers(num1, num2));
            }
        }
    }

    private void setNumber(Number number) {
        this.firstElement = number;
        this.operator = null;
        this.secondElement = null;
    }

    /**
     * @return The number-value of the operation if it represents a number. Returns an empty
     * optional if it is not a number.
     */
    public Optional<Number> toNumber() {
        if (secondElement != null) {
            return Optional.empty();
        }

        return Optional.of(new Number(Integer.parseInt(getFirstElement().getValue()
                .stringRepresentation())));
    }

    public Element getFirstElement() {
        return firstElement;
    }

    public Optional<Operator> getOperator() {
        if (operator == null) {
            return Optional.empty();
        }
        else {
            return Optional.of(operator);
        }
    }

    public Optional<Element> getSecondElement() {
        if (secondElement == null) {
            return Optional.empty();
        }
        else {
            return Optional.of(secondElement);
        }
    }

    public String toHumanReadableString() {
        String first = "";
        String firstOpen = "";
        String firstClose = "";
        String op = (operator == null ? "" : " " + operator.getSign() + " ");
        String second = "";
        String secondOpen = "";
        String secondClose = "";

        if (firstElement instanceof Operation) {
            first = ((Operation)firstElement).toHumanReadableString();
            firstOpen = "(";
            firstClose = ")";
        }
        else if (firstElement instanceof FunctionCall) {
            FunctionCall call = (FunctionCall)firstElement;
            second = call.getCalled() + call.getFormattedArguments();
        }
        else if (firstElement instanceof Value) {
            first = ((Value)firstElement).stringRepresentation();
        }
        else {
            first = ((Variable)firstElement).getName();
        }

        if (secondElement instanceof Operation) {
            second = ((Operation)secondElement).toHumanReadableString();
            secondOpen = "(";
            secondClose = ")";
        }
        else if (secondElement instanceof FunctionCall) {
            FunctionCall call = (FunctionCall)secondElement;
            second = call.getCalled() + call.getFormattedArguments();
        }
        else if (secondElement instanceof Value) {
            second = ((Value)secondElement).stringRepresentation();
        }
        else if (secondElement != null) {
            second = ((Variable)secondElement).getName();
        }

        return firstOpen + first + firstClose + op + secondOpen + second + secondClose;
    }

    /**
     * Swaps the first and second element.
     */
    public void swap() {
        Element dummy = firstElement;
        firstElement = secondElement;
        secondElement = dummy;
    }

    @Override
    public Value getValue() {
        return null;
    }

    @Override
    public String toString() {
        return "{" + firstElement + "} " + operator + " {" + secondElement + "}";
    }

}
