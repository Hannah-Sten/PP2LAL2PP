package nl.rubensten.pp2lal2pp.lang;

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

        if ((first instanceof Number) && (second instanceof  Number)) {

        }
    }

    public Element getFirstElement() {
        return firstElement;
    }

    public Operator getOperator() {
        return operator;
    }

    public Element getSecondElement() {
        return secondElement;
    }

    @Override
    public Value getValue() {
        return null;
    }

}
