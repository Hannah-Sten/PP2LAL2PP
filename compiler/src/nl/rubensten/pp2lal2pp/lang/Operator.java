package nl.rubensten.pp2lal2pp.lang;

import nl.rubensten.pp2lal2pp.IDManager;
import nl.rubensten.pp2lal2pp.PP2LAL2PPException;

import java.util.Optional;

/**
 * @author Ruben Schellekens
 */
public enum Operator implements Identifyable {

    ADD_ASSIGN("+=", OperatorType.ASSIGNMENT, (n1, n2) -> {
        throw new PP2LAL2PPException("Operator += not supported on two numbers.");
    }),

    SUBSTRACT_ASSIGN("-=", OperatorType.ASSIGNMENT, (n1, n2) -> {
        throw new PP2LAL2PPException("Operator -= not supported on two numbers.");
    }),

    POWER_ASSIGN("**=", OperatorType.ASSIGNMENT, (n1, n2) -> {
        throw new PP2LAL2PPException("Operator **= not supported on two numbers.");
    }),

    MULTIPLY_ASSIGN("*=", OperatorType.ASSIGNMENT, (n1, n2) -> {
        throw new PP2LAL2PPException("Operator *= not supported on two numbers.");
    }),

    DIVIDE_ASSIGN("/=", OperatorType.ASSIGNMENT, (n1, n2) -> {
        throw new PP2LAL2PPException("Operator /= not supported on two numbers.");
    }),

    REMAINDER_ASSIGN("%=", OperatorType.ASSIGNMENT, (n1, n2) -> {
        throw new PP2LAL2PPException("Operator %= not supported on two numbers.");
    }),

    BITWISE_AND_ASSIGN("&=", OperatorType.ASSIGNMENT, (n1, n2) -> {
        throw new PP2LAL2PPException("Operator &= not supported on two numbers.");
    }),

    BITWISE_OR_ASSIGN("|=", OperatorType.ASSIGNMENT, (n1, n2) -> {
        throw new PP2LAL2PPException("Operator |= not supported on two numbers.");
    }),

    BITWISE_XOR_ASSIGN("^=", OperatorType.ASSIGNMENT, (n1, n2) -> {
        throw new PP2LAL2PPException("Operator ^= not supported on two numbers.");
    }),

    ////

    ASSIGN_ALT_LEFT(":=", OperatorType.ASSIGNMENT, (n1, n2) -> {
        throw new PP2LAL2PPException("Assignment operator := not supported on two numbers.");
    }),

    ASSIGN_ALT_RIGHT("=:", OperatorType.ASSIGNMENT, (n1, n2) -> {
        throw new PP2LAL2PPException("Assignment operator =: not supported on two numbers.");
    }),

    ADDITION("+", OperatorType.ARITHMETIC, (n1, n2) -> {
        return new Number(n1.getIntValue() + n2.getIntValue());
    }),

    SUBSTRACTION("-", OperatorType.ARITHMETIC, (n1, n2) -> {
        return new Number(n1.getIntValue() - n2.getIntValue());
    }),

    POWER("**", OperatorType.ARITHMETIC, (n1, n2) -> {
        return new Number((int)Math.pow(n1.getIntValue(), n2.getIntValue()));
    }),

    MULTIPLICATION("*", OperatorType.ARITHMETIC, (n1, n2) -> {
        return new Number(n1.getIntValue() * n2.getIntValue());
    }),

    INTEGER_DIVISION("/", OperatorType.ARITHMETIC, (n1, n2) -> {
        return new Number(n1.getIntValue() / n2.getIntValue());
    }),

    REMAINDER("%", OperatorType.ARITHMETIC, (n1, n2) -> {
        return new Number(n1.getIntValue() % n2.getIntValue());
    }),

    BITWISE_OR("|", OperatorType.BITWISE, (n1, n2) -> {
        return new Number(n1.getIntValue() | n2.getIntValue());
    }),

    BITWISE_AND("&", OperatorType.BITWISE, (n1, n2) -> {
        return new Number(n1.getIntValue() & n2.getIntValue());
    }),

    BITWISE_XOR("^", OperatorType.BITWISE, (n1, n2) -> {
        return new Number(n1.getIntValue() ^ n2.getIntValue());
    }),

    BITWISE_NOT("~", OperatorType.BITWISE, (n1, n2) -> {
        throw new PP2LAL2PPException("Operator ~ not supported on two numbers.");
    }),

    GREATHER_THAN_EQUAL(">=", OperatorType.RELATIONAL, (n1, n2) -> {
        return (n1.getIntValue() >= n2.getIntValue() ? Number.ONE : Number.ZERO);
    }),

    LESSER_THAN_EQUAL("<=", OperatorType.RELATIONAL, (n1, n2) -> {
        return (n1.getIntValue() <= n2.getIntValue() ? Number.ONE : Number.ZERO);
    }),

    EQUALS("==", OperatorType.RELATIONAL, (n1, n2) -> {
        return (n1.getIntValue() == n2.getIntValue() ? Number.ONE : Number.ZERO);
    }),

    NOT_EQUALS("!=", OperatorType.RELATIONAL, (n1, n2) -> {
        return (n1.getIntValue() != n2.getIntValue() ? Number.ONE : Number.ZERO);
    }),

    GREATER_THAN(">", OperatorType.RELATIONAL, (n1, n2) -> {
        return (n1.getIntValue() > n2.getIntValue() ? Number.ONE : Number.ZERO);
    }),

    LESSER_THAN("<", OperatorType.RELATIONAL, (n1, n2) -> {
        return (n1.getIntValue() < n2.getIntValue() ? Number.ONE : Number.ZERO);
    }),

    ASSIGN("=", OperatorType.ASSIGNMENT, (n1, n2) -> {
        throw new PP2LAL2PPException("Assignment operator = not supported on two numbers.");
    }),

    LOGICAL_OR(" or ", OperatorType.LOGICAL, (n1, n2) -> {
        return (n1.getIntValue() == 1 || n2.getIntValue() == 1 ? Number.ONE : Number.ZERO);
    }),

    LOGICAL_AND(" and ", OperatorType.LOGICAL, (n1, n2) -> {
        return (n1.getIntValue() == 1 && n2.getIntValue() == 1 ? Number.ONE : Number.ZERO);
    }),

    NUMBER_NEGATION("-", OperatorType.UNARY, (n1, n2) -> {
        throw new PP2LAL2PPException("Unary operator - not supported on two numbers.");
    }) {
        @Override
        public Number calculateNumber(Number number) {
            return new Number(-number.getIntValue());
        }
    },

    BOOLEAN_NEGATION("!", OperatorType.UNARY, (n1, n2) -> {
        throw new PP2LAL2PPException("Unary operator ! not supported on two numbers.");
    }) {
        @Override
        public Number calculateNumber(Number number) {
            if (number.getIntValue() == 0) {
                return Number.ONE;
            }
            else if (number.getIntValue() == 1) {
                return Number.ZERO;
            }
            else {
                return number;
            }
        }
    };

    /**
     * The unique ID of the operator.
     */
    private int id;

    /**
     * The string characterisation of the operator.
     */
    private String sign;

    /**
     * The type of operator.
     */
    private OperatorType type;

    /**
     * Function to execute when executing the operator on two numbers.
     */
    private OperationFunction<Number> numbersFunction;

    Operator(String sign, OperatorType type, OperationFunction<Number> numbersFunction) {
        this.id = IDManager.newId();
        this.sign = sign;
        this.type = type;
        this.numbersFunction = numbersFunction;
    }

    /**
     * Looks up the operator with the given sign.
     *
     * @param sign
     *         The sign to get the operator of.
     * @return The operator object corresponding to the given sign.
     */
    public static Optional<Operator> getBySign(String sign) {
        for (Operator op : values()) {
            if (op.getSign().equals(sign)) {
                return Optional.of(op);
            }
        }

        return Optional.empty();
    }

    /**
     * Apply the operator to two numbers.
     */
    public Number calculateNumbers(Number first, Number second) {
        return numbersFunction.operate(first, second);
    }

    /**
     * Apply the operator to one number.
     */
    public Number calculateNumber(Number number) {
        return number;
    }

    /**
     * @return The string characterisation of the operator.
     */
    public String getSign() {
        return sign;
    }

    /**
     * @return The type of operator.
     */
    public OperatorType getType() {
        return type;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return sign;
    }

    enum OperatorType {
        ASSIGNMENT,
        ARITHMETIC,
        BITWISE,
        RELATIONAL,
        LOGICAL,
        UNARY;
    }

}
